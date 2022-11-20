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
    float oldDepth = texture2D(altDepth, vec2(0.5)).r;

    #ifdef IS_GL3
    if (isnan(oldDepth)) {
        oldDepth = currentDepth;
    }

    output = mix(oldDepth, currentDepth, decay2);
    #else
    if (oldDepth != oldDepth) { // cheap isNaN
       oldDepth = currentDepth;
    }
    gl_FragColor = vec4(mix(, currentDepth, decay2), 0, 0, 0);
    #endif
}
