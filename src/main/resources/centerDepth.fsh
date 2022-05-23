#version 120

uniform sampler2D depth;
uniform sampler2D altDepth;
uniform float lastFrameTime;
uniform float decay;

void main() {
    float currentDepth = texture2D(depth, vec2(0.5)).r;
    float decay2 = 1.0 - exp(-decay * lastFrameTime);
    gl_FragColor = vec4(mix(texture2D(altDepth, vec2(0.5)).r, currentDepth, decay2), 0, 0, 0);
}
