use crate::imgui_wgpu;

pub struct SceneMainMenu {
    demo_open: bool,
    last_frame_timestamp: std::time::Instant,
    last_cursor: Option<imgui::MouseCursor>,
}

impl SceneMainMenu {
    pub fn new() -> Self {
        Self {
            demo_open: true,
            last_frame_timestamp: std::time::Instant::now(),
            last_cursor: None,
        }
    }

    pub fn render(
            self: &mut Self,
            wgpu_device: &wgpu::Device,
            wgpu_queue: &wgpu::Queue,
            window: &winit::window::Window,
            imgui_renderer: &mut imgui_wgpu::Renderer,
            winit_platform: &mut imgui_winit_support::WinitPlatform,
            imgui: &mut imgui::Context,
            command_encoder: &mut wgpu::CommandEncoder,
            color_attachment_view: &wgpu::TextureView) -> &Self {

        let now = std::time::Instant::now();
        let delta_s = now - self.last_frame_timestamp.elapsed();
        imgui.io_mut().update_delta_time(now - self.last_frame_timestamp);
        self.last_frame_timestamp = now;

        winit_platform
            .prepare_frame(imgui.io_mut(), &window)
            .expect("Failed to prepare frame");

        let ui = imgui.frame();

        let window1 = ui.window("Hello world");

        window1
            .size([300.0, 100.0], imgui::Condition::FirstUseEver)
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

        let window2 = ui.window("Hello too");

        window2
            .size([400.0, 200.0], imgui::Condition::FirstUseEver)
            .position([400.0, 200.0], imgui::Condition::FirstUseEver)
            .build(|| {
                ui.text(format!("Frametime: {:?}", delta_s));
            });

        ui.show_demo_window(&mut self.demo_open);

        if self.last_cursor != ui.mouse_cursor() {
            self.last_cursor = ui.mouse_cursor();
            winit_platform.prepare_render(&ui, &window);
        }

        let mut gui_render_pass = command_encoder.begin_render_pass(&wgpu::RenderPassDescriptor {
            label: None,
            color_attachments: &[Some(wgpu::RenderPassColorAttachment {
                view: &color_attachment_view,
                resolve_target: None,
                ops: wgpu::Operations {
                    load: wgpu::LoadOp::Clear(wgpu::Color {
                        r: 0.1,
                        g: 0.2,
                        b: 0.3,
                        a: 1.0,
                    }),
                    store: true,
                },
            })],
            depth_stencil_attachment: None,
        });

        imgui_renderer
            .render(imgui.render(), &wgpu_queue, &wgpu_device, &mut gui_render_pass)
            .expect("Rendering failed");

        self
    }
}
