use super::{RendererState, RendererOutputState, GameState};

pub struct SceneMainMenu {
    last_frame_timestamp: std::time::Instant,
    last_cursor: Option<imgui::MouseCursor>,
}

impl SceneMainMenu {
    pub fn new() -> Self {
        Self {
            last_frame_timestamp: std::time::Instant::now(),
            last_cursor: None,
        }
    }

    pub fn render(
        self: &mut Self,
        renderer_state: &mut RendererState,
        output_state: &mut RendererOutputState,
        game_state: &mut GameState,
    ) -> &Self {

        let now = std::time::Instant::now();
        let delta_s = now - self.last_frame_timestamp.elapsed();
        renderer_state.imgui.io_mut().update_delta_time(now - self.last_frame_timestamp);
        self.last_frame_timestamp = now;

        renderer_state.winit_platform
            .prepare_frame(renderer_state.imgui.io_mut(), &renderer_state.window)
            .expect("Failed to prepare frame");

        let ui = renderer_state.imgui.frame();

        let window1 = ui.window("Alien Inviders");

        window1
            .position([15.0, 15.0], imgui::Condition::FirstUseEver)
            .size([980.0, 365.0], imgui::Condition::FirstUseEver)
            .build(|| {
                ui.text("<plot placeholder>");

                let size = ui.window_size();
                ui.text(format!("window size: {} x {}", size[0], size[1]));

                ui.separator();

                if ui.button("PLAY!") {
                    game_state.go_to_gameplay();
                }

                ui.separator();
                let mouse_pos = ui.io().mouse_pos;
                ui.text(format!(
                    "Mouse Position: ({:.1},{:.1})",
                    mouse_pos[0], mouse_pos[1]
                ));
            });

        if self.last_cursor != ui.mouse_cursor() {
            self.last_cursor = ui.mouse_cursor();
            renderer_state.winit_platform.prepare_render(&ui, &renderer_state.window);
        }

        let mut gui_render_pass = output_state.command_encoder.begin_render_pass(&wgpu::RenderPassDescriptor {
            label: None,
            color_attachments: &[Some(wgpu::RenderPassColorAttachment {
                view: &output_state.color_attachment_view,
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

        renderer_state.imgui_renderer
            .render(renderer_state.imgui.render(), &renderer_state.wgpu_queue, &renderer_state.wgpu_device, &mut gui_render_pass)
            .expect("Rendering failed");

        self
    }

    pub fn post_process_event<T>(
        &mut self,
        event: winit::event::Event<T>,
        renderer_state: &mut RendererState) -> &Self {

        renderer_state.winit_platform.handle_event(renderer_state.imgui.io_mut(), &renderer_state.window, &event);

        self
    }

    pub fn on_enter_scene(&mut self, renderer_state: &mut RendererState) -> &Self {
        // release mouse cursor
        renderer_state.window.set_cursor_grab(winit::window::CursorGrabMode::None)
            .or_else(|_e| renderer_state.window.set_cursor_grab(winit::window::CursorGrabMode::None))
            .unwrap();

        let window_size = renderer_state.window.inner_size();

        renderer_state.window.set_cursor_position(winit::dpi::PhysicalPosition::new(window_size.width as f32 / 2.0, window_size.height as f32 / 2.0)).unwrap();

        renderer_state.window.set_cursor_visible(true);

        self
    }

    pub fn on_leave_scene(&mut self, renderer_state: &mut RendererState) -> &Self {
        // capture mouse cursor
        renderer_state.window.set_cursor_grab(winit::window::CursorGrabMode::Confined)
            .or_else(|_e| renderer_state.window.set_cursor_grab(winit::window::CursorGrabMode::Locked))
            .unwrap();

        let window_size = renderer_state.window.inner_size();

        renderer_state.window.set_cursor_position(winit::dpi::PhysicalPosition::new(window_size.width as f32 / 2.0, window_size.height as f32 / 2.0)).unwrap();

        renderer_state.window.set_cursor_visible(false);

        self
    }
}
