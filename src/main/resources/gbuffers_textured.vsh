#version 120

varying vec4 color;
varying vec2 tex;

void main() {
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;

    color = gl_Color;
    tex = gl_MultiTexCoord0.xy;
}