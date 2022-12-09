use super::imgui_wgpu;

pub mod scene_main_menu;
pub mod scene_new_game_intro;
pub mod scene_gameplay;

pub enum SceneState {
    Loading,
    SceneMainMenu,
    SceneNewGame,
    SceneGameplay,
}

pub struct Game {
    current_state: SceneState,

    scene_gameplay: scene_gameplay::SceneGameplay,
    scene_main_menu: scene_main_menu::SceneMainMenu,
}

impl Game {
    pub fn new(wgpu_device: &wgpu::Device, surface_config: &wgpu::SurfaceConfiguration) -> Self {
        Self {
            current_state: SceneState::Loading,

            scene_gameplay: scene_gameplay::SceneGameplay::new(wgpu_device, surface_config),
            scene_main_menu: scene_main_menu::SceneMainMenu::new(),
        }
    }

    pub fn set_state(self: &mut Self, new_state: SceneState) -> &mut Self {
        self.current_state = new_state;
        self
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

        match self.current_state {
            SceneState::SceneGameplay => {
                self.scene_gameplay.render(
                    command_encoder,
                    color_attachment_view,
                );
            },

            SceneState::SceneMainMenu => {
                self.scene_main_menu.render(
                    wgpu_device,
                    wgpu_queue,
                    window,
                    imgui_renderer,
                    winit_platform,
                    imgui,
                    command_encoder,
                    color_attachment_view,
                );
            },

            _ => (),
        }

        self
    }
}
