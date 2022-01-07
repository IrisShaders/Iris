#version 120

// these shouldn't be recognized as options, conflicts with vsh values
// #define VSH
#define FSH

#ifdef FSH
#endif

#ifdef VSH
#endif

const int shadowDistance = 128; // How far away should shadows be rendered in blocks [16 32 48 64 128 256 512]
const int shadowDistanceRenderMul = 1; // [-1 0 1] Controls shadow culling modes
const float ambientOcclusionLevel = 1.0; // [0.0] Allows disabling vanilla ambient occlusion

void main() {
    // we're not really doing anything in particular
}
