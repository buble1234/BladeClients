#version 330

uniform sampler2D DiffuseSampler;
uniform sampler2D vanilla;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform float thickness;
uniform vec4 outlineColor;

out vec4 fragColor;

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);

    if (center.a > 0.01) {
        fragColor = center;
        return;
    }

    float outline = 0.0;
    float steps = thickness * 2.0;

    for (float x = -thickness; x <= thickness; x += 1.0) {
        for (float y = -thickness; y <= thickness; y += 1.0) {
            vec2 offset = vec2(x, y) * oneTexel;
            vec4 sample = texture(DiffuseSampler, texCoord + offset);
            if (sample.a > 0.01) {
                outline = 1.0;
                break;
            }
        }
        if (outline > 0.0) break;
    }

    vec4 vanillaColor = texture(vanilla, texCoord);

    if (outline > 0.0) {
        fragColor = vec4(mix(vanillaColor.rgb, outlineColor.rgb, outlineColor.a), 1.0);
    } else {
        fragColor = vanillaColor;
    }
}