#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D ColorTexture;
uniform sampler2D DepthTexture;
uniform float time;
uniform vec3 customColor;

uniform float effectAlpha;

void main() {
    vec4 originalColor = texture(ColorTexture, uv);
    vec2 texelSize = 1.0 / textureSize(DepthTexture, 0);

    float minDepth = 1.0;

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 offset = vec2(x, y) * texelSize;
            float neighborDepth = texture(DepthTexture, uv + offset).r;
            minDepth = min(minDepth, neighborDepth);
        }
    }

    float mask = smoothstep(0.99, 0.98, minDepth);

    if (mask < 0.01) {
        discard;
    }

    float brightness = 0.7 + (sin(time * 3.14159 * 2.0) + 1.0) / 2.0 * 0.3;
    vec4 effectColor = vec4(customColor * brightness, 1.0);

    vec4 finalHandColor = mix(originalColor, effectColor, effectAlpha);

    outColor = vec4(finalHandColor.rgb, mask);
}