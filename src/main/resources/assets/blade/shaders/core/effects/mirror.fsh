#version 150

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

float noise(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    float a = rand(i + vec2(0.0, 0.0));
    float b = rand(i + vec2(1.0, 0.0));
    float c = rand(i + vec2(0.0, 1.0));
    float d = rand(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

vec3 permute(vec3 x) {
    return mod(((x * 34.0) + 1.0) * x, 289.0);
}

float snoise(vec2 v) {
    const vec4 C = vec4(0.211324865405187, 0.366025403784439, -0.577350269189626, 0.024390243902439);
    vec2 i = floor(v + dot(v, C.yy));
    vec2 x0 = v - i + dot(i, C.xx);
    vec2 i1;
    i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;
    i = mod(i, 289.0);
    vec3 p = permute(permute(i.y + vec3(0.0, i1.y, 1.0)) + i.x + vec3(0.0, i1.x, 1.0));
    vec3 m = max(0.5 - vec3(dot(x0, x0), dot(x12.xy, x12.xy), dot(x12.zw, x12.zw)), 0.0);
    m = m * m;
    m = m * m;
    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;
    m *= 1.79284291400159 - 0.85373472095314 * (a0 * a0 + h * h);
    vec3 g;
    g.x = a0.x * x0.x + h.x * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
}

float rdist(vec2 pos, vec2 size, vec4 radius) {
    radius.xy = (pos.x > 0.0) ? radius.xy : radius.wz;
    radius.x  = (pos.y > 0.0) ? radius.x : radius.y;

    vec2 v = abs(pos) - size + radius.x;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - radius.x;
}

float ralpha(vec2 size, vec2 coord, vec4 radius, float smoothness) {
    vec2 center = size * 0.5;
    float dist = rdist(center - (coord * size), center - 1.0, radius);
    return 1.0 - smoothstep(1.0 - smoothness, 1.0, dist);
}

in vec2 FragCoord;
in vec2 TexCoord;

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform float Brightness;
uniform float ReflectionIntensity;
uniform float DistortionStrength;
uniform float Time;
uniform vec3 CameraPos;
uniform vec4 color1;

out vec4 fragColor;

vec4 getReflectedColor() {
    vec2 coord = TexCoord;
    vec2 distortion = vec2(0.0);

    if (DistortionStrength > 0.0) {
        float wave1 = sin(coord.x * 10.0 + Time * 2.0) * 0.01;
        float wave2 = cos(coord.y * 8.0 + Time * 1.5) * 0.01;
        float noiseDistort = snoise(coord * 5.0 + Time * 0.5) * 0.02;
        vec2 center = vec2(0.5);
        vec2 toCenter = coord - center;
        float dist = length(toCenter);
        float radialDistort = sin(dist * 15.0 + Time * 3.0) * 0.005;
        distortion = vec2(wave1 + noiseDistort, wave2 + noiseDistort) * DistortionStrength;
        distortion += normalize(toCenter) * radialDistort * DistortionStrength;
    }

    vec2 mirrorCoord = vec2(coord.x, 1.0 - coord.y) + distortion;
    mirrorCoord = clamp(mirrorCoord, 0.0, 1.0);

    vec4 originalColor = texture(InputSampler, coord);
    vec4 reflectedColor = texture(InputSampler, mirrorCoord);

    float depthFade = 1.0 - smoothstep(0.0, 1.0, abs(coord.y - 0.5) * 2.0);
    float fresnel = pow(1.0 - abs(coord.y - 0.5) * 2.0, 2.0);
    float reflectionStrength = ReflectionIntensity * fresnel * depthFade;

    return mix(originalColor, reflectedColor, reflectionStrength);
}

void main() {
    vec4 reflectedColor = getReflectedColor();
    vec4 finalColor = reflectedColor * color1 * Brightness;

    float metallic = 0.1 + 0.05 * sin(Time * 4.0 + FragCoord.x * 20.0);
    finalColor.rgb += vec3(metallic) * finalColor.a;

    finalColor.a *= ralpha(Size, FragCoord, Radius, Smoothness);

    if (finalColor.a <= 0.001) {
        discard;
    }

    fragColor = finalColor;
}