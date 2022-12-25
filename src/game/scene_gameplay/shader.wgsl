const PI: f32 = 3.14159265359;
const PI2: f32 = 6.28318530718;

struct Light {
    direction: vec3<f32>;
    color: vec3<f32>;
};

struct Material {
    albedo: vec3<f32>;
    metallic: f32;
    roughness: f32;
    emissive: vec3<f32>;
};

struct Surface {
    position: vec3<f32>;
    normal: vec3<f32>;
    uv: vec2<f32>;
};

fn fresnel_schlick(specularColor: vec3<f32>, dotNV: f32) -> vec3<f32> {
    var fresnel = (1.0 - dotNV);
    fresnel = pow(fresnel, 5.0);
    return specularColor + (1.0 - specularColor) * fresnel;
}

/**
 * # Example
 *
 * ```
 * Material material = ...;
 * Light light = ...;
 * Surface surface = ...;
 * vec3<f32> finalColor = calculate_lighting(surface, material, light);
 * gl_FragColor = vec4(finalColor, 1.0);
 * ```
 */
fn calculate_lighting(surface: Surface, material: Material, light: Light) -> vec3<f32> {
    var light_direction = normalize(light.direction);
    var view_direction = normalize(-surface.position);
    var halfway_direction = normalize(light_direction + view_direction);

    var dot_NL = dot(surface.normal, light_direction);
    var dot_NV = dot(surface.normal, view_direction);
    var dot_NH = dot(surface.normal, halfway_direction);
    var dot_LH = dot(light_direction, halfway_direction);

    var diffuse = material.albedo * light.color * max(dot_NL, 0.0);
    var specular = vec3<f32>(0.0);

    if (dot_NL > 0.0) {
        var specular_color = fresnel_schlick(material.albedo, dot_NV);
        var roughness = material.roughness * material.roughness;
        var specular_term = dot_NH * dot_NH * (roughness - 1.0) + 1.0;
        specular = light.color * specular_color * specular_term * pow(max(dot_LH, 0.0), 64.0);
    }

    return diffuse + specular + material.emissive;
}

struct LightsUniform
{
    lights: array<Light>;
};

struct CameraUniform {
    view: mat4x4<f32>,
    projection: mat4x4<f32>,
};

struct VertexInput {
    @location(0) position: vec3<f32>,
    @location(1) normal: vec3<f32>,
    @location(2) uv: vec2<f32>,
};

struct VertexOutput {
    @builtin(position) clip_position: vec4<f32>,
    @location(0) uv: vec2<f32>,
    @location(1) normal: vec3<f32>,
};

@group(0) @binding(0) var<storage, read> lights: LightsUniform;

@group(0) @binding(1) var<uniform> camera: CameraUniform;

@group(1) @binding(0) var albedo_texture: texture_2d<f32>;
@group(1) @binding(1) var albedo_sampler: sampler;

@group(1) @binding(2) var metallic_texture: texture_2d<f32>;
@group(1) @binding(3) var metallic_sampler: sampler;

@group(1) @binding(4) var roughness_texture: texture_2d<f32>;
@group(1) @binding(5) var roughness_sampler: sampler;

@group(1) @binding(6) var emissive_texture: texture_2d<f32>;
@group(1) @binding(7) var emissive_sampler: sampler;

@vertex
fn vs_main(
    vs_in: VertexInput,
) -> VertexOutput {
    var out: VertexOutput;

    out.clip_position = camera.projection * camera.view * vec4<f32>(vs_in.position, 1.0);
    out.uv = vs_in.uv;
    out.normal = vs_in.normal;

    return out;
}

@fragment
fn fs_main(fs_in: VertexOutput) -> @location(0) vec4<f32> {
    var surface = Surface(vs_in.position, vs_in.normal, vs_in.uv);

    var albedo_color = textureSample(albedo_texture, albedo_sampler, fs_in.uv);
    var roughness_color = textureSample(roughness_texture, roughness_sampler, fs_in.uv);
    var metallic_color = textureSample(metallic_texture, metallic_sampler, fs_in.uv);
    var emissive_color = textureSample(emissive_texture, emissive_sampler, fs_in.uv);

    var material = Material(albedo_color, metallic_color.r, roughness_color.r, emissive_volor);

    var final_color = textureSample(texture_diffuse, sampler_diffuse, fs_in.uv);

    var color = albedo_color;
    var n_lights = arrayLength(lights);

    for (var i: i32 = 0; i < n_lights; i++) {
        var light_dir = normalize(-lights[i].direction);
        var weight = max(dot(fs_in.normal, light_dir), 0.0);

        color += weight * calculate_lighting(surface, material, light);
    }

    return vec4<f32>(color, 1.0);
}
