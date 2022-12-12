use super::imgui_wgpu;

pub mod scene_main_menu;
pub mod scene_new_game_intro;
pub mod scene_gameplay;

#[derive(Clone, Copy, PartialEq)]
pub enum SceneState {
    Loading,
    MainMenu,
    NewGame,
    Gameplay,
}

pub struct RendererState<'a> {
    pub wgpu_device: &'a wgpu::Device,
    pub wgpu_queue: &'a wgpu::Queue,
    pub window: &'a winit::window::Window,
    pub imgui_renderer: &'a mut imgui_wgpu::Renderer,
    pub winit_platform: &'a mut imgui_winit_support::WinitPlatform,
    pub imgui: &'a mut imgui::Context,
}

pub struct RendererOutputState<'a> {
    pub command_encoder: &'a mut wgpu::CommandEncoder,
    pub color_attachment_view: &'a wgpu::TextureView,
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

    fn before_scene_switched(self: &mut Self, renderer_state: &mut RendererState) -> &mut Self {
        match self.current_state {
            SceneState::Gameplay => {
                self.scene_gameplay.on_leave_scene(renderer_state);
            },

            SceneState::MainMenu => {
                self.scene_main_menu.on_leave_scene(renderer_state);
            },

            _ => (),
        }

        self
    }

    fn after_scene_switched(self: &mut Self, renderer_state: &mut RendererState) -> &mut Self {
        match self.current_state {
            SceneState::Gameplay => {
                self.scene_gameplay.on_enter_scene(renderer_state);
            },

            SceneState::MainMenu => {
                self.scene_main_menu.on_enter_scene(renderer_state);
            },

            _ => (),
        }

        self
    }

    pub fn set_scene_state(self: &mut Self, new_state: SceneState, renderer_state: &mut RendererState) -> &mut Self {
        // before switching the scene
        self.before_scene_switched(renderer_state);

        self.current_state = new_state;

        // after switching the scene
        self.after_scene_switched(renderer_state);

        self
    }

    pub fn render(self: &mut Self, renderer_state: &mut RendererState, output_state: &mut RendererOutputState) -> &Self {
        let mut next_state = self.current_state;

        match self.current_state {
            SceneState::Gameplay => {
                self.scene_gameplay.render(
                    renderer_state,
                    output_state,
                    &mut || { next_state = SceneState::MainMenu },
                );
            },

            SceneState::MainMenu => {
                self.scene_main_menu.render(
                    renderer_state,
                    output_state,
                    &mut || { next_state = SceneState::Gameplay; }
                );
            },

            _ => (),
        }

        if next_state != self.current_state {
            self.set_scene_state(next_state, renderer_state);
        }

        self
    }

    pub fn post_process_event<T>(
        &mut self,
        event: winit::event::Event<T>,
        renderer_state: &mut RendererState) -> &Self {

        let mut next_state = self.current_state;

        match self.current_state {
            SceneState::MainMenu => {
                self.scene_main_menu.post_process_event(event, renderer_state);
            },

            SceneState::Gameplay => {
                self.scene_gameplay.post_process_event(event, renderer_state, &mut || { next_state = SceneState::MainMenu });
            },

            _ => (),
        }

        if next_state != self.current_state {
            self.set_scene_state(next_state, renderer_state);
        }

        self
    }
}
