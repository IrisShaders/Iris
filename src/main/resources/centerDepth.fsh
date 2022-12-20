#version 150 core

uniform sampler2D depth;
uniform sampler2D altDepth;
uniform float lastFrameTime;
uniform float decay;

out float iris_fragColor;

void main() {
    float currentDepth = texture(depth, vec2(0.5)).r;
    float decay2 = 1.0 - exp(-decay * lastFrameTime);
    float oldDepth = texture(altDepth, vec2(0.5)).r;

    if (isnan(oldDepth)) {
       oldDepth = currentDepth;
    }

    iris_fragColor = mix(oldDepth, currentDepth, decay2);
}
