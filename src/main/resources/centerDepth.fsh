#version VERSIONPLACEHOLDER

// This will be removed by Iris if the system does not support GL3.
#define IS_GL3

uniform sampler2D depth;
uniform sampler2D altDepth;
uniform float lastFrameTime;
uniform float decay;

#ifdef IS_GL3
out float output;
#endif

void main() {
    float currentDepth = texture2D(depth, vec2(0.5)).r;
    float decay2 = 1.0 - exp(-decay * lastFrameTime);
    #ifdef IS_GL3
    output = mix(texture2D(altDepth, vec2(0.5)).r, currentDepth, decay2);
    #else
    gl_FragColor = vec4(mix(texture2D(altDepth, vec2(0.5)).r, currentDepth, decay2), 0, 0, 0);
    #endif
}
