use winit::{
    event::*,
    event_loop::{ControlFlow, EventLoop},
    window::WindowBuilder,
};

use pollster::FutureExt as _;
use imgui::*;

pub mod imgui_wgpu;
pub mod game;

fn main() {
    env_logger::init();

    let event_loop = EventLoop::new();

    let mut window_builder = WindowBuilder::new();

    window_builder = window_builder.with_title("Alien Inviders");
    window_builder = window_builder.with_min_inner_size(winit::dpi::LogicalSize::new(1024, 768));

    let window = window_builder.build(&event_loop).unwrap();

    let size = window.inner_size();

    // The instance is a handle to our GPU
    // Backends::all => Vulkan + Metal + DX12 + Browser WebGPU
    let instance = wgpu::Instance::new(wgpu::Backends::all());

    let surface = unsafe { instance.create_surface(&window) };

    let adapter = instance.request_adapter(
        &wgpu::RequestAdapterOptions {
            power_preference: wgpu::PowerPreference::default(),
            compatible_surface: Some(&surface),
            force_fallback_adapter: false,
        },
    ).block_on().unwrap();

    let (device, queue) = adapter.request_device(
        &wgpu::DeviceDescriptor {
            features: wgpu::Features::empty(),
            limits: wgpu::Limits::default(),
            label: None,
        },
        None, // Trace path
    ).block_on().unwrap();

    let mut surface_config = wgpu::SurfaceConfiguration {
        usage: wgpu::TextureUsages::RENDER_ATTACHMENT,
        format: surface.get_supported_formats(&adapter)[0],
        width: size.width,
        height: size.height,
        present_mode: wgpu::PresentMode::Fifo,
        alpha_mode: wgpu::CompositeAlphaMode::Auto,
    };

    surface.configure(&device, &surface_config);

    let mut imgui = imgui::Context::create();
    let mut platform = imgui_winit_support::WinitPlatform::init(&mut imgui);

    platform.attach_window(
        imgui.io_mut(),
        &window,
        imgui_winit_support::HiDpiMode::Default,
    );

    imgui.set_ini_filename(None);

    let hidpi_factor = window.scale_factor();
    let font_size = (13.0 * hidpi_factor) as f32;
    imgui.io_mut().font_global_scale = (1.0 / hidpi_factor) as f32;

    imgui.fonts().add_font(&[imgui::FontSource::DefaultFontData {
        config: Some(imgui::FontConfig {
            oversample_h: 1,
            pixel_snap_h: true,
            size_pixels: font_size,
            ..Default::default()
        }),
    }]);

    let clear_color = wgpu::Color {
        r: 0.1,
        g: 0.2,
        b: 0.3,
        a: 1.0,
    };

    let renderer_config = imgui_wgpu::RendererConfig {
        texture_format: surface_config.format,
        ..Default::default()
    };

    let mut renderer = imgui_wgpu::Renderer::new(&mut imgui, &device, &queue, renderer_config);

    let mut last_frame = std::time::Instant::now();
    let mut last_cursor = None;
    let mut demo_open = true;

    let mut game = game::Game::new(&device, &surface_config);

    {
        // TODO: temporary
        game.set_state(game::SceneState::SceneGameplay);
    }

    event_loop.run(move |event, _, control_flow| {
        match event {
            Event::WindowEvent {
                ref event,
                window_id,
            } if window_id == window.id() => match event {
                WindowEvent::CloseRequested => *control_flow = ControlFlow::Exit,

                WindowEvent::Resized(physical_size) => {
                    let new_size = *physical_size;

                    if new_size.width > 0 && new_size.height > 0 {
                        surface_config.width = new_size.width;
                        surface_config.height = new_size.height;
                        surface.configure(&device, &surface_config);
                    }
                },

                WindowEvent::ScaleFactorChanged { new_inner_size, .. } => {
                    let new_size = **new_inner_size;

                    if new_size.width > 0 && new_size.height > 0 {
                        surface_config.width = new_size.width;
                        surface_config.height = new_size.height;
                        surface.configure(&device, &surface_config);
                    }
                },

                _ => {}
            },

            Event::RedrawRequested(window_id) if window_id == window.id() => {
                // update GUI
                let now = std::time::Instant::now();
                let delta_s = now - last_frame.elapsed();
                imgui.io_mut().update_delta_time(now - last_frame);
                last_frame = now;

                // render
                let output = surface.get_current_texture().unwrap();

                platform
                    .prepare_frame(imgui.io_mut(), &window)
                    .expect("Failed to prepare frame");

                let ui = imgui.frame();

                {
                    let window = ui.window("Hello world");

                    window
                        .size([300.0, 100.0], Condition::FirstUseEver)
                        .build(|| {
                            ui.text("Hello world!");
                            ui.text("This...is...imgui-rs on WGPU!");
                            ui.separator();
                            let mouse_pos = ui.io().mouse_pos;
                            ui.text(format!(
                                "Mouse Position: ({:.1},{:.1})",
                                mouse_pos[0], mouse_pos[1]
                            ));
                        });

                    let window = ui.window("Hello too");

                    window
                        .size([400.0, 200.0], Condition::FirstUseEver)
                        .position([400.0, 200.0], Condition::FirstUseEver)
                        .build(|| {
                            ui.text(format!("Frametime: {:?}", delta_s));
                        });

                    ui.show_demo_window(&mut demo_open);
                }

                if last_cursor != Some(ui.mouse_cursor()) {
                    last_cursor = Some(ui.mouse_cursor());
                    platform.prepare_render(&ui, &window);
                }

                let view = output.texture.create_view(&wgpu::TextureViewDescriptor::default());

                let mut command_encoder = device.create_command_encoder(&wgpu::CommandEncoderDescriptor {
                    label: Some("Render Encoder"),
                });

                {
                    game.render(&mut command_encoder, &view);
                }

                {
                    let mut gui_render_pass = command_encoder.begin_render_pass(&wgpu::RenderPassDescriptor {
                        label: None,
                        color_attachments: &[Some(wgpu::RenderPassColorAttachment {
                            view: &view,
                            resolve_target: None,
                            ops: wgpu::Operations {
                                load: wgpu::LoadOp::Clear(clear_color),
                                store: true,
                            },
                        })],
                        depth_stencil_attachment: None,
                    });

                    renderer
                        .render(imgui.render(), &queue, &device, &mut gui_render_pass)
                        .expect("Rendering failed");
                }

                queue.submit(std::iter::once(command_encoder.finish()));

                output.present();
            },

            Event::MainEventsCleared => {
                // RedrawRequested will only trigger once, unless manually requested
                window.request_redraw();
            },

            _ => {}
        }

        platform.handle_event(imgui.io_mut(), &window, &event);
    });
}
