#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D Tex0;
uniform float Brightness;
uniform float Contrast;
uniform float Exposure;
uniform float Saturation;
uniform int Hue;
uniform float Temperature;
uniform vec3 Lift;
uniform vec3 Gamma;
uniform vec3 Gain;
uniform vec3 Offset;

// --- Содержимое из Luminance.glsl ---
float luminance(const vec3 rgb) {
    const vec3 w = vec3(0.2126, 0.7152, 0.0722);
    return dot(rgb, w);
}

vec3 luminosity(const vec3 rgb, const float lum) {
    return mix(vec3(luminance(rgb)), rgb, lum);
}

// --- Содержимое из Color.glsl ---
vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// --- Содержимое из Temperature.glsl ---
vec3 colorFromKelvin(float temperature) { // photographic temperature values are between 15 to 150
    // ИСПРАВЛЕНО: Убрана ошибочная строка с делением на 100
    vec3 color;
    if (temperature <= 6600.0) { // Значения теперь соответствуют K
        color.r = 1.0;
        color.g = (99.4708025861 * log(temperature / 100.0) - 161.1195681661) / 255.0;
        if (temperature < 1900.0) color.b = 0.0;
        else color.b = (138.5177312231 * log((temperature / 100.0) - 10.0) - 305.0447927307) / 255.0;
    } else {
        color.r = (329.698727446 * pow((temperature / 100.0) - 60.0, -0.1332047592)) / 255.0;
        color.g = (288.1221695283 * pow((temperature / 100.0) - 60.0, -0.0755148492)) / 255.0;
        color.b = 1.0;
    }
    return clamp(color, 0.0, 1.0);
}


// --- Содержимое из FilmicTonemap.glsl ---
vec3 filmic(vec3 x) {
    vec3 X = max(vec3(0.0), x - 0.004);
    vec3 result = (X * (6.2 * X + 0.5)) / (X * (6.2 * X + 1.7) + 0.06);
    return pow(result, vec3(2.2));
}

void main() {
    vec3 color = texture(Tex0, uv).rgb;
    color += Brightness;
    color = (color - 0.5) * Contrast + 0.5;
    color = (1.0 + Exposure) * color;
    color = luminosity(color, Saturation);
    color = hsv2rgb(rgb2hsv(color) + vec3(fract(Hue / 360.0), 0.0, 0.0));
    // ИСПРАВЛЕНО: Добавлена проверка на ноль, чтобы избежать белого экрана
    vec3 tempColor = colorFromKelvin(Temperature);
    if (tempColor.r > 0.0 && tempColor.g > 0.0 && tempColor.b > 0.0) {
        color /= tempColor;
    }
    color = pow(max(vec3(0.0), color * (1.0 + Gain - Lift) + Lift + Offset), max(vec3(0.0), 1.0 - Gamma));
    color = filmic(color);
    outColor = vec4(color, 1.0);
}