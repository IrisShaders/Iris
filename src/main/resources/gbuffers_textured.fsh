#version 120

uniform sampler2D terrain;
uniform sampler2D lightmap;

varying vec4 color;
varying vec2 tex;
varying vec2 lmcoord;

void main() {
    vec4 lightmap = texture2D(lightmap, lmcoord);
    vec4 color = texture2D(terrain, tex) * color;
    color *= lightmap;
    gl_FragColor = color;
}
