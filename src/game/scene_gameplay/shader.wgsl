struct VertexInput {
    @location(0) position: vec3<f32>,
};

struct VertexOutput {
    @builtin(position) clip_position: vec4<f32>,
    @location(0) color: vec3<f32>,
};

@vertex
fn vs_main(
    vs_in: VertexInput,
) -> VertexOutput {
    var out: VertexOutput;

    out.clip_position = vec4<f32>(vs_in.position, 1.0);
    out.color = vec3<f32>(vs_in.position);

    return out;
}

@fragment
fn fs_main(fs_in: VertexOutput) -> @location(0) vec4<f32> {
    return vec4<f32>(fs_in.color, 1.0);
}
