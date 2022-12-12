use super::imgui_wgpu;

pub mod scene_main_menu;
pub mod scene_new_game_intro;
pub mod scene_gameplay;

#[derive(Clone, Copy, PartialEq)]
pub enum Scene {
    Loading,
    MainMenu,
    NewGame,
    Gameplay,
}

#[derive(Clone, Copy, PartialEq)]
pub struct GameState {
    current_scene: Scene,
}

impl GameState {
    pub fn new() -> Self {
        Self {
            current_scene: Scene::MainMenu,
        }
    }

    pub fn go_to_main_menu(&mut self) -> &mut Self {
        self.current_scene = Scene::MainMenu;

        self
    }

    pub fn go_to_gameplay(&mut self) -> &mut Self {
        self.current_scene = Scene::Gameplay;

        self
    }
}

pub struct RendererState<'a> {
    pub wgpu_device: &'a wgpu::Device,
    pub wgpu_queue: &'a wgpu::Queue,
    pub window: &'a winit::window::Window,
    pub surface_config: &'a wgpu::SurfaceConfiguration,
    pub imgui_renderer: &'a mut imgui_wgpu::Renderer,
    pub winit_platform: &'a mut imgui_winit_support::WinitPlatform,
    pub imgui: &'a mut imgui::Context,
}

pub struct RendererOutputState<'a> {
    pub command_encoder: &'a mut wgpu::CommandEncoder,
    pub color_attachment_view: &'a wgpu::TextureView,
}

pub struct Game {
    pub state: GameState,

    previous_state: GameState,

    scene_gameplay: scene_gameplay::SceneGameplay,
    scene_main_menu: scene_main_menu::SceneMainMenu,
}

impl Game {
    pub fn new(renderer_state: &mut RendererState) -> Self {
        Self {
            state: GameState::new(),
            previous_state: GameState::new(),

            scene_gameplay: scene_gameplay::SceneGameplay::new(renderer_state),
            scene_main_menu: scene_main_menu::SceneMainMenu::new(),
        }
    }

    fn before_scene_switched(self: &mut Self, renderer_state: &mut RendererState) -> &mut Self {
        match self.previous_state.current_scene {
            Scene::Gameplay => {
                self.scene_gameplay.on_leave_scene(renderer_state);
            },

            Scene::MainMenu => {
                self.scene_main_menu.on_leave_scene(renderer_state);
            },

            _ => (),
        }

        self
    }

    fn after_scene_switched(self: &mut Self, renderer_state: &mut RendererState) -> &mut Self {
        match self.state.current_scene {
            Scene::Gameplay => {
                self.scene_gameplay.on_enter_scene(renderer_state);
            },

            Scene::MainMenu => {
                self.scene_main_menu.on_enter_scene(renderer_state);
            },

            _ => (),
        }

        self
    }

    fn update_scene(self: &mut Self, renderer_state: &mut RendererState) -> &mut Self {
        if self.previous_state == self.state {
            return self
        }

        // before switching the scene
        self.before_scene_switched(renderer_state);

        self.previous_state = self.state;

        // after switching the scene
        self.after_scene_switched(renderer_state);

        self
    }

    pub fn render(self: &mut Self, renderer_state: &mut RendererState, output_state: &mut RendererOutputState) -> &Self {
        match self.state.current_scene {
            Scene::Gameplay => {
                self.scene_gameplay.render(
                    renderer_state,
                    output_state,
                    &mut self.state,
                );
            },

            Scene::MainMenu => {
                self.scene_main_menu.render(
                    renderer_state,
                    output_state,
                    &mut self.state,
                );
            },

            _ => (),
        }

        self
    }

    pub fn post_process_event<T>(&mut self, event: winit::event::Event<T>, renderer_state: &mut RendererState) -> &Self {

        match self.state.current_scene {
            Scene::MainMenu => {
                self.scene_main_menu.post_process_event(event, renderer_state);
            },

            Scene::Gameplay => {
                self.scene_gameplay.post_process_event(event, renderer_state, &mut self.state);
            },

            _ => (),
        }

        self.update_scene(renderer_state);

        self
    }
}
