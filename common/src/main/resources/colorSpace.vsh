#version 330 core

in vec3 iris_Position;
in vec2 iris_UV0;
uniform mat4 projection;
out vec2 uv;

void main() {
    gl_Position = projection * vec4(iris_Position, 1.0);
    uv = iris_UV0;
}
