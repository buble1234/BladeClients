#version 330 core

layout(location = 0) in vec3 a_Pos;
layout(location = 1) in vec2 a_UV;

uniform mat4 u_ModelMat;
uniform mat4 u_ProjMat;

out vec2 v_UV;

void main() {
    gl_Position = u_ProjMat * u_ModelMat * vec4(a_Pos, 1.0);
    v_UV = a_UV;
}