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

    pub fn set_scene_state(self: &mut Self, new_state: SceneState, window: &winit::window::Window) -> &mut Self {
        // before switching the scene
        match self.current_state {
            SceneState::Gameplay => {
                self.scene_gameplay.on_leave_scene(window);
            },

            SceneState::MainMenu => {
                self.scene_main_menu.on_leave_scene(window);
            },

            _ => (),
        }

        self.current_state = new_state;

        // after switching the scene
        match new_state {
            SceneState::Gameplay => {
                self.scene_gameplay.on_enter_scene(window);
            },

            SceneState::MainMenu => {
                self.scene_main_menu.on_enter_scene(window);
            },

            _ => (),
        }

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

        let mut next_state = self.current_state;

        match self.current_state {
            SceneState::Gameplay => {
                self.scene_gameplay.render(
                    command_encoder,
                    color_attachment_view,
                    &mut || { next_state = SceneState::MainMenu },
                );
            },

            SceneState::MainMenu => {
                self.scene_main_menu.render(
                    wgpu_device,
                    wgpu_queue,
                    window,
                    imgui_renderer,
                    winit_platform,
                    imgui,
                    command_encoder,
                    color_attachment_view,
                    &mut || { next_state = SceneState::Gameplay; }
                );
            },

            _ => (),
        }

        if next_state != self.current_state {
            self.set_scene_state(next_state, window);
        }

        self
    }

    pub fn post_process_event<T>(
        &mut self,
        event: winit::event::Event<T>,
        window: &winit::window::Window,
        winit_platform: &mut imgui_winit_support::WinitPlatform,
        imgui: &mut imgui::Context) -> &Self {

        let mut next_state = self.current_state;

        match self.current_state {
            SceneState::MainMenu => {
                self.scene_main_menu.post_process_event(event, window, winit_platform, imgui);
            },

            SceneState::Gameplay => {
                self.scene_gameplay.post_process_event(event, window, winit_platform, imgui, &mut || { next_state = SceneState::MainMenu });
            },

            _ => (),
        }

        if next_state != self.current_state {
            self.set_scene_state(next_state, window);
        }

        self
    }
}
