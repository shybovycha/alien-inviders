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
}

impl Game {
    pub fn new(device: &wgpu::Device, surface_config: &wgpu::SurfaceConfiguration) -> Self {
        Self {
            current_state: SceneState::Loading,

            scene_gameplay: scene_gameplay::SceneGameplay::new(device, surface_config),
        }
    }

    pub fn set_state(self: &mut Self, new_state: SceneState) -> &mut Self {
        self.current_state = new_state;
        self
    }

    pub fn render(self: &Self, command_encoder: &mut wgpu::CommandEncoder, color_attachment_view: &wgpu::TextureView) -> &Self {
        match self.current_state {
            SceneState::SceneGameplay => {
                self.scene_gameplay.render(command_encoder, color_attachment_view);
            },

            _ => (),
        }

        self
    }
}
