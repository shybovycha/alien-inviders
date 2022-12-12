use wgpu::util::DeviceExt;

use super::{RendererState, RendererOutputState, GameState};

#[repr(C)]
#[derive(Copy, Clone, Debug, bytemuck::Pod, bytemuck::Zeroable)]
struct Vertex {
    position: [f32; 3],
}

impl Vertex {
    fn desc<'a>() -> wgpu::VertexBufferLayout<'a> {
        wgpu::VertexBufferLayout {
            array_stride: std::mem::size_of::<Vertex>() as wgpu::BufferAddress,
            step_mode: wgpu::VertexStepMode::Vertex,
            attributes: &[
                wgpu::VertexAttribute {
                    offset: 0,
                    shader_location: 0,
                    format: wgpu::VertexFormat::Float32x3,
                }
                // wgpu::VertexAttribute {
                //     offset: std::mem::size_of::<[f32; 3]>() as wgpu::BufferAddress,
                //     shader_location: 1,
                //     format: wgpu::VertexFormat::Float32x3,
                // }
            ]
        }
    }
}

pub struct SceneGameplay {
    render_pipeline: wgpu::RenderPipeline,
    vertex_buffer: wgpu::Buffer,
    index_buffer: wgpu::Buffer,
    num_primitives: u32,
}

impl SceneGameplay {
    pub fn new(renderer_state: &mut RendererState) -> Self {
        let shader = renderer_state.wgpu_device.create_shader_module(wgpu::ShaderModuleDescriptor {
            label: Some("Shader"),
            source: wgpu::ShaderSource::Wgsl(include_str!("shader.wgsl").into()),
        });

        let render_pipeline_layout = renderer_state.wgpu_device.create_pipeline_layout(&wgpu::PipelineLayoutDescriptor {
            label: Some("Render Pipeline Layout"),
            bind_group_layouts: &[],
            push_constant_ranges: &[],
        });

        let render_pipeline = renderer_state.wgpu_device.create_render_pipeline(&wgpu::RenderPipelineDescriptor {
            label: Some("Render Pipeline"),
            layout: Some(&render_pipeline_layout),
            vertex: wgpu::VertexState {
                module: &shader,
                entry_point: "vs_main",
                buffers: &[
                    Vertex::desc(),
                ],
            },
            fragment: Some(wgpu::FragmentState {
                module: &shader,
                entry_point: "fs_main",
                targets: &[Some(wgpu::ColorTargetState {
                    format: renderer_state.surface_config.format,
                    blend: Some(wgpu::BlendState::REPLACE),
                    write_mask: wgpu::ColorWrites::ALL,
                })],
            }),
            primitive: wgpu::PrimitiveState {
                topology: wgpu::PrimitiveTopology::TriangleList,
                strip_index_format: None,
                front_face: wgpu::FrontFace::Ccw,
                cull_mode: Some(wgpu::Face::Back),
                // Setting this to anything other than Fill requires Features::NON_FILL_POLYGON_MODE
                polygon_mode: wgpu::PolygonMode::Fill,
                // Requires Features::DEPTH_CLIP_CONTROL
                unclipped_depth: false,
                // Requires Features::CONSERVATIVE_RASTERIZATION
                conservative: false,
            },
            depth_stencil: None,
            multisample: wgpu::MultisampleState {
                count: 1,
                mask: !0,
                alpha_to_coverage_enabled: false,
            },
            multiview: None,
        });

        const VERTICES: &[Vertex] = &[
            Vertex { position: [ 0.0, 0.5, 0.0 ] },
            Vertex { position: [ -0.5, -0.5, 0.0 ] },
            Vertex { position: [ 0.5, -0.5, 0.0 ] },
        ];

        const INDICES: &[u16] = &[
            0, 1, 2,
        ];

        let vertex_buffer = renderer_state.wgpu_device.create_buffer_init(
            &wgpu::util::BufferInitDescriptor {
                label: Some("Vertex Buffer"),
                contents: bytemuck::cast_slice(VERTICES),
                usage: wgpu::BufferUsages::VERTEX,
            }
        );

        let index_buffer = renderer_state.wgpu_device.create_buffer_init(
            &wgpu::util::BufferInitDescriptor {
                label: Some("Vertex Buffer"),
                contents: bytemuck::cast_slice(INDICES),
                usage: wgpu::BufferUsages::INDEX,
            }
        );

        Self {
            render_pipeline,
            vertex_buffer,
            index_buffer,
            num_primitives: (INDICES.len() as u32),
        }
    }

    pub fn render(
        self: &Self,
        renderer_state: &mut RendererState,
        output_state: &mut RendererOutputState,
        game_state: &mut GameState,
    ) -> &Self {

        let mut render_pass = output_state.command_encoder.begin_render_pass(&wgpu::RenderPassDescriptor {
            label: Some("Render Pass"),

            color_attachments: &[
                // This is what @location(0) in the fragment shader targets
                Some(wgpu::RenderPassColorAttachment {
                    view: output_state.color_attachment_view,
                    resolve_target: None,
                    ops: wgpu::Operations {
                        load: wgpu::LoadOp::Clear(
                            wgpu::Color {
                                r: 0.1,
                                g: 0.2,
                                b: 0.3,
                                a: 1.0,
                            }
                        ),
                        store: true,
                    },
                })
            ],

            depth_stencil_attachment: None,
        });

        render_pass.set_pipeline(&self.render_pipeline);

        render_pass.set_vertex_buffer(0, self.vertex_buffer.slice(..));
        render_pass.set_index_buffer(self.index_buffer.slice(..), wgpu::IndexFormat::Uint16);

        render_pass.draw_indexed(0..self.num_primitives, 0, 0..1);

        self
    }

    pub fn post_process_event<T>(
        &mut self,
        event: winit::event::Event<T>,
        renderer_state: &mut RendererState,
        game_state: &mut GameState,
    ) -> &Self {

        match event {
            winit::event::Event::WindowEvent {
                event: winit::event::WindowEvent::KeyboardInput {
                    input: winit::event::KeyboardInput {
                        state: winit::event::ElementState::Pressed,
                        virtual_keycode: Some(winit::event::VirtualKeyCode::Escape),
                        ..
                    },
                    ..
                },
                ..
            } => {
                game_state.go_to_main_menu();
            },

            _ => (),
        }

        self
    }

    pub fn on_enter_scene(&mut self, renderer_state: &mut RendererState) -> &Self {
        // capture mouse cursor
        renderer_state.window.set_cursor_grab(winit::window::CursorGrabMode::Confined)
            .or_else(|_e| renderer_state.window.set_cursor_grab(winit::window::CursorGrabMode::Locked))
            .unwrap();

        let window_size = renderer_state.window.inner_size();

        renderer_state.window.set_cursor_position(winit::dpi::PhysicalPosition::new(window_size.width as f32 / 2.0, window_size.height as f32 / 2.0)).unwrap();

        renderer_state.window.set_cursor_visible(false);

        self
    }

    pub fn on_leave_scene(&mut self, renderer_state: &mut RendererState) -> &Self {
        // release mouse cursor
        renderer_state.window.set_cursor_grab(winit::window::CursorGrabMode::None)
            .or_else(|_e| renderer_state.window.set_cursor_grab(winit::window::CursorGrabMode::None))
            .unwrap();

        let window_size = renderer_state.window.inner_size();

        renderer_state.window.set_cursor_position(winit::dpi::PhysicalPosition::new(window_size.width as f32 / 2.0, window_size.height as f32 / 2.0)).unwrap();

        renderer_state.window.set_cursor_visible(true);

        self
    }
}
