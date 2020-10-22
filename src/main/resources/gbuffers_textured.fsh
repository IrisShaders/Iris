#version 120

uniform sampler2D terrain;

varying vec4 color;
varying vec2 tex;

void main() {
    gl_FragColor = texture2D(terrain, tex) * color * vec4(0.5, 1.0, 1.0, 1.0);
}