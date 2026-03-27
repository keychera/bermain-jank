#version 460

layout (location = 0) in vec4 v_color;
layout (location = 1) in vec2 v_uv;

layout (location = 0) out vec4 FragColor;

layout (set = 2, binding = 0) uniform sampler2D texSampler; 

layout(std140, set = 3, binding = 0) uniform UniformBlock {
    float time;
};

void main()
{
    float pulse = sin(time * 2.0) * 0.5 + 0.5; // range [0, 1]
    vec4 fromVertex = vec4(v_color.rgb * (0.8 + pulse * 0.5), v_color.a);
    vec4 fromTex = texture(texSampler, v_uv);
    FragColor = 0.5 * fromTex + 0.5 * fromVertex;
}