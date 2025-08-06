#version 330 core

in vec2 v_UV;
out vec4 outColor;

uniform float u_Progress;
uniform vec3 u_Color;
uniform float u_Radius;
uniform float u_Softness;

void main() {
    vec2 coord = v_UV * 2.0 - 1.0;

    float dist = length(coord);

    float ring = 1.0 - smoothstep(u_Radius, u_Radius + u_Softness, dist);

    ring *= smoothstep(u_Radius - u_Softness, u_Radius, dist);

    float fadeAlpha = (1.0 - u_Progress) * (1.0 - u_Progress);

    outColor = vec4(u_Color, ring * fadeAlpha);
}