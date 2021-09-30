#version 120

#define GODRAYS 32 // [16 32 64 128] Number of godrays
 #define SHADOWS

#define GRASS_SHADOWS // whether tallgrass casts shadows
//#define  // not really a valid define
//#define GALAXIES

const int shadowDistance = 128; // How far away should shadows be rendered in blocks [16 32 48 64 128 256 512]
const int shadowDistanceRenderMul = 1; // [-1 0 1] Controls shadow culling modes
const float ambientOcclusionLevel = 1.0; // [0.0] Allows disabling vanilla ambient occlusion

// #define  volumetricLighting
//#define ANNOYING_STUFF
#define UNCONFIRMED_OR_UNUSED

// these shouldn't be recognized as options, conflicts with fsh values
#define VSH
// #define FSH

#ifdef SHADOWS
uniform sampler2D shadowtex0;
#endif

#ifdef GRASS_SHADOWS
#endif

#ifdef GALAXIES
#endif

#ifdef volumetricLighting
#endif

#ifdef ANNOYING_STUFF
#endif

#ifdef VSH
#endif

#ifdef FSH
#endif

void main() {
    // we're not really doing anything in particular
}
