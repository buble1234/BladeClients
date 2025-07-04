#version 150

#moj_import <blade:common.glsl>

in vec3 Position;
in vec2 UV;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 localCoord;
out vec2 texCoord;
out vec4 fragColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    localCoord = rvertexcoord(gl_VertexID);
    texCoord = UV;
    fragColor = Color;
}