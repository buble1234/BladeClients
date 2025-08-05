#version 330 core

layout (location = 0) in vec3 pos;
layout (location = 1) in vec3 normal;

out vec3 v_Normal;
out vec3 v_FragPos;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

void main()
{
    // Позиция вершины в пространстве вида (относительно камеры)
    v_FragPos = vec3(ModelViewMat * vec4(pos, 1.0));
    // Нормаль в пространстве вида
    v_Normal = mat3(transpose(inverse(ModelViewMat))) * normal;

    gl_Position = ProjMat * vec4(v_FragPos, 1.0);
}