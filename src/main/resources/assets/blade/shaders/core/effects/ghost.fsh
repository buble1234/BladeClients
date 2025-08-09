#version 330 core

in vec3 v_Normal;
in float v_Fade;
out vec4 outColor;

uniform vec3 u_Color1;
uniform vec3 u_Color2;
uniform float u_Alpha;

void main() {
    // Эффект Френеля - края светятся ярче
    float fresnel = pow(1.0 - abs(dot(normalize(v_Normal), vec3(0.0, 0.0, 1.0))), 3.0);
    fresnel = clamp(fresnel, 0.1, 1.0);

    // Смешиваем два цвета на основе нормали
    vec3 finalColor = mix(u_Color1, u_Color2, clamp(v_Normal.y * 0.5 + 0.5, 0.0, 1.0));

    // Применяем Френель, затухание хвоста и общую прозрачность
    outColor = vec4(finalColor, fresnel * v_Fade * u_Alpha);
}