#version 330

uniform sampler2D DiffuseSampler;
uniform sampler2D MaskSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Radius;
uniform vec4 BlurColor;

out vec4 fragColor;

void main() {
    float progRadius = Radius*texture(MaskSampler, texCoord).a;
    if (progRadius < 1) {
        fragColor = texture(DiffuseSampler, texCoord);
    } else {
        vec4 blurred = vec4(0);
        int i = 0;
        for(float r = -progRadius;r<=progRadius;r++) {
            blurred += texture(DiffuseSampler, texCoord + oneTexel * r * BlurDir);
            i++;
        }
        blurred = blurred / i;
        fragColor = vec4(blurred.rgb * BlurColor.rgb, blurred.a * BlurColor.a);
    }
}