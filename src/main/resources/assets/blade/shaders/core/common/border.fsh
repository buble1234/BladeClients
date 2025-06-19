// Author NoCap
#version 150

#moj_import <blade:common.glsl>

in vec2 FragCoord;
in vec4 FragColor;

uniform vec2 Size;
uniform vec4 Radius;
uniform float Thickness;
uniform vec2 Smoothness;
uniform vec4 OutlineColor[4];

out vec4 OutColor;

void main() {
    vec2 center = Size * 0.5;
    vec2 coord = FragCoord.xy * Size;
    float dist = rdist(center - coord, center - 1.0, Radius);

    float innerAlpha = smoothstep(1.0 - Thickness - Smoothness.x - Smoothness.y,
        1.0 - Thickness - Smoothness.y, dist);

    float outerAlpha = 1.0 - smoothstep(1.0 - Smoothness.y, 1.0, dist);

    float borderAlpha = outerAlpha - innerAlpha;

    vec2 normalizedCoord = FragCoord.xy;

    vec4 topColor = mix(OutlineColor[0], OutlineColor[3], normalizedCoord.x);
    vec4 bottomColor = mix(OutlineColor[1], OutlineColor[2], normalizedCoord.x);
    vec4 outlineColor = mix(topColor, bottomColor, normalizedCoord.y);

    vec4 finalColor;

    if (borderAlpha > 0.0) {
        finalColor = mix(FragColor, outlineColor, borderAlpha / outerAlpha);
    } else {
        finalColor = FragColor;
    }

    finalColor.a *= outerAlpha;

    if (finalColor.a == 0.0) { // alpha test
        discard;
    }

    OutColor = finalColor;
}