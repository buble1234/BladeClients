#version 150

#moj_import <blade:common.glsl>

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec2 iResolution;
uniform vec2 iMouse;

uniform float BlurSize;
uniform float Quality;
uniform float Direction;

in vec2 localCoord;
in vec2 texCoord;
in vec4 fragColor;

out vec4 OutColor;

#define R     iResolution
#define PI    3.14159265
#define S     smoothstep
#define PX(a) (a / Size.y)

mat2 Rot(float a) {
    return mat2(cos(a), -sin(a), sin(a), cos(a));
}

float Box(vec2 p, vec2 b) {
    vec2 d = abs(p) - b;
    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
}

float IconPhoto(vec2 uv) {
    float c = 0.0;
    for (float i = 0.0; i < 8.0; i++) {
        vec2 u = uv;
        u *= Rot(i / 8.0 * 2.0 * PI);
        u += vec2(0.0, PX(40.0));
        float b = Box(u, vec2(PX(0.0), PX(13.0)));
        c += S(PX(1.5), 0.0, b - PX(15.0)) * 0.2;
    }
    return c;
}

vec4 LiquidGlass(sampler2D tex, vec2 uv, float direction, float quality, float size) {
    vec2 radius = size / R;
    vec4 color = texture(tex, uv);
    float total = 1.0;

    for (float d = 0.0; d < PI; d += PI / direction) {
        for (float i = 1.0 / quality; i <= 1.0; i += 1.0 / quality) {
            color += texture(tex, uv + vec2(cos(d), sin(d)) * radius * i);
            total += 1.0;
        }
    }

    return color / total;
}

vec4 Icon(vec2 uv) {
    float box = Box(uv, vec2(PX(50.0))),
          boxShape = S(PX(1.5), 0.0, box - PX(50.0)),
          boxDisp = S(PX(35.0), 0.0, box - PX(25.0)),
          boxLight = boxShape * S(0.0, PX(30.0), box - PX(40.0)),
          icon = IconPhoto(uv);
    return vec4(boxShape, boxDisp, boxLight, icon);
}

void main() {
    vec2 st = (localCoord - 0.5 * Size) / Size.y;

    vec2 quadCenterScreen = gl_FragCoord.xy - localCoord + 0.5 * Size;
    vec2 M = (iMouse - quadCenterScreen) / Size.y;

    vec4 icon = Icon(st - M);

    vec2 uv = gl_FragCoord.xy / R;

    vec2 uv2 = uv - iMouse / R;
    uv2 *= 0.5 + 0.5 * S(0.5, 1.0, icon.y);
    uv2 += iMouse / R;

    vec3 col = LiquidGlass(Sampler0, uv2, Direction, Quality, BlurSize).rgb;

    col += icon.z * 0.9;

    col *= 1.0 - 0.2 * S(PX(80.0), 0.0, Box(st - M + vec2(0.0, PX(40.0)), vec2(PX(50.0))));

    OutColor = vec4(col, icon.x) * fragColor;

    if (OutColor.a < 0.01) {
        discard;
    }
}