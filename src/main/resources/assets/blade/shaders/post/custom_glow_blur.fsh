#version 150

uniform sampler2D InSampler;
uniform vec2 BlurDir;
uniform vec4 color;

// --- НАСТРОЙКИ ДЛЯ СБАЛАНСИРОВАННОГО СВЕЧЕНИЯ ---
// Общая входная яркость. Если после фикса свечение кажется тусклым, увеличьте это значение (например, до 1.2).
const float GlowInputStrength = 1.0;

// Резкость ядра. (3.0-8.0)
const float CoreSharpness = 5.0;
// Сила ядра.
const float CoreStrength = 1.5;
// Сила дымки.
const float HazeStrength = 0.75;
// --- КОНЕЦ НАСТРОЕК ---

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    float weight[12] = float[](
    0.153170, 0.144893, 0.128415, 0.106796, 0.083127, 0.060517,
    0.041115, 0.026119, 0.015586, 0.008749, 0.004632, 0.002322
    );

    vec2 tex_offset = oneTexel * BlurDir;

    vec4 blurred_color = texture(InSampler, texCoord) * weight[0];
    for (int i = 1; i < 12; i++) {
        blurred_color += texture(InSampler, texCoord + tex_offset * i) * weight[i];
        blurred_color += texture(InSampler, texCoord - tex_offset * i) * weight[i];
    }

    // --- ВОТ ОН, ФИКС! ---
    // Мы умножаем размытую альфу на общую силу и СРАЗУ ЖЕ ограничиваем её значением 1.0.
    // Это не позволяет "горячим точкам" (где альфа > 1.0) влиять на дальнейшие расчеты.
    float blurred_alpha = min(blurred_color.a * GlowInputStrength, 1.0);
    // ---------------------

    // Теперь, когда `blurred_alpha` "безопасна", дальнейшие расчеты дают предсказуемый результат.
    float core_alpha = pow(blurred_alpha, CoreSharpness) * CoreStrength;
    float haze_alpha = blurred_alpha * HazeStrength; // Используем ту же безопасную альфу
    float final_alpha = core_alpha + haze_alpha;

    fragColor = vec4(color.rgb, clamp(final_alpha, 0.0, 1.0));
}