#version 150

in vec4 FragColor;

out vec4 OutColor;

void main() {
    if (FragColor.a < 0.001) {
        discard;
    }
    OutColor = FragColor;
}