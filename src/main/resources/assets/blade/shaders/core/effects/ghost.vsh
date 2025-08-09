#version 330 core

layout(location = 0) in vec3 a_Pos; // x, y, z - координаты точки на торе
layout(location = 1) in vec3 a_Normal; // x, y, z - нормаль точки

uniform mat4 u_ModelViewMat;
uniform mat4 u_ProjMat;
uniform float u_Time;
uniform float u_VerticalOffset;

out vec3 v_Normal;
out float v_Fade;

void main() {
    vec3 pos = a_Pos;

    // Анимация "глиста" - смещаем Y координату по синусоиде
    // u_Time * 2.0 + pos.x * 5.0 - создает волну вдоль кольца
    pos.y += sin(u_Time * 2.0 + pos.x * 5.0) * 0.2;
    pos.y += u_VerticalOffset;

    gl_Position = u_ProjMat * u_ModelViewMat * vec4(pos, 1.0);

    v_Normal = mat3(transpose(inverse(u_ModelViewMat))) * a_Normal;

    // Эффект появления/исчезновения хвоста
    // a_Pos.x от -1 до 1, sin превращает это в плавный градиент от 0 до 1 и обратно
    v_Fade = sin((a_Pos.x + u_Time) * 3.14159);
}