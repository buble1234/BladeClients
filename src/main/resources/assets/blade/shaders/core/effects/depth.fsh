#version 330 core

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform sampler2D Tex1;

uniform float Near;
uniform float Far;
uniform float MinThreshold;
uniform float MaxThreshold;

uniform bool Rainbow;
uniform bool RainbowNoise;
uniform float RainbowStrength;
uniform float Time;
uniform float Yaw;
uniform float Pitch;


vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 mod289(vec4 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 permute(vec4 x) { return mod289(((x*34.0)+1.0)*x); }
vec4 taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }

float snoise(vec3 v) {
    const vec2 C = vec2(1.0/6.0, 1.0/3.0);
    const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);
    vec3 i = floor(v + dot(v, C.yyy));
    vec3 x0 = v - i + dot(i, C.xxx);
    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);
    vec3 x1 = x0 - i1 + C.xxx;
    vec3 x2 = x0 - i2 + C.yyy;
    vec3 x3 = x0 - D.yyy;
    i = mod289(i);
    vec4 p = permute(permute(permute(
        i.z + vec4(0.0, i1.z, i2.z, 1.0))
        + i.y + vec4(0.0, i1.y, i2.y, 1.0))
        + i.x + vec4(0.0, i1.x, i2.x, 1.0));
    float n_ = 0.142857142857;
    vec3 ns = n_ * D.wyz - D.xzx;
    vec4 j = p - 49.0 * floor(p * ns.z * ns.z);
    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);
    vec4 x = x_ * ns.x + ns.yyyy;
    vec4 y = y_ * ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);
    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);
    vec4 s0 = floor(b0) * 2.0 + 1.0;
    vec4 s1 = floor(b1) * 2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));
    vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;
    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);
    vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2,p2), dot(p3,p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;
    vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m*m, vec4(dot(p0,x0), dot(p1,x1), dot(p2,x2), dot(p3,x3)));
}

vec3 hsb2rgb(vec3 c) {
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0), 6.0)-3.0)-1.0, 0.0, 1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);
    return c.z * mix(vec3(1.0), rgb, c.y);
}


float linearizeDepth(float depth, float near, float far) {
    return (2.0 * near * far) / (far + near - depth * (far - near));
}

void main() {
    float depth = texture(Tex1, uv).x;
    float distance = linearizeDepth(depth, Near, Far) / Far;

    color = vec4(0.0);

    if (distance > MinThreshold) {
        vec3 blurredColor = texture(Tex0, uv).rgb;
        float alpha = clamp((distance - MinThreshold) / (MaxThreshold - MinThreshold), 0.0, 1.0);

        vec3 finalColor = blurredColor;

        if (Rainbow) {
            vec3 rainbowColor;
            if (RainbowNoise) {
                vec3 noiseCoord = vec3(uv.x + Yaw / 180.0, uv.y - Pitch / 90.0, Time * 0.1);
                float hue = (snoise(noiseCoord) + 1.0) / 2.0;
                rainbowColor = hsb2rgb(vec3(hue, 0.7, 1.0));
            } else {
                float angle = Time * 0.5 + (uv.x + uv.y) * 3.14159 * 2.0;
                rainbowColor = vec3(sin(angle) * 0.5 + 0.5, sin(angle + 2.094395) * 0.5 + 0.5, sin(angle + 4.188790) * 0.5 + 0.5);
            }
            finalColor = mix(blurredColor, rainbowColor, RainbowStrength);
        }

        color = vec4(finalColor, alpha);
    }
}