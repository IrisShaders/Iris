#version 150 core

in vec3 iris_Position;
uniform mat4 projection;

void main() {
    gl_Position = projection * vec4(iris_Position, 1.0);
}
