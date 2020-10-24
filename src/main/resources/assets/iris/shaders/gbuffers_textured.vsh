
#version 120

#define Radius 16. //Globe radius [4. 10. 12. 16. 24. 32.]
#define Toggle 1. //Enable, disable or invert globe [-1. 0. 1.]

#define Terrain .4 //Terrain distortion level [.0 .2 .4 .6 .8 1.]
#define Offset 1. //Camera height offset [.0 1.]

#define Amount .2 //Wave distortion intensity [.0 .2 .5 .8 1.]
#define Frequency .5 //Wave frequency  [.0 .2 .5 .8 1.]
#define Speed .5 //Wave animation speed [.0 .2 .5 .8 1.]

attribute vec2 mc_Entity;

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform vec3 cameraPosition;
uniform float frameTimeCounter;

varying vec4 color;
varying vec3 model;
varying vec3 world;
varying vec2 coord0;
varying vec2 coord1;
varying float id;

vec3 hash3(vec3 p)
{
    return fract(cos(p*mat3(-31.14,15.92,65.35,-89.79,-32.38,46.26,43.38,32.79,-02.88))*41.97);
}
vec3 value3(vec3 p)
{
    vec3 f = floor(p);
    vec3 s = p-f; s*= s*s*(3.-s-s);
    const vec2 o = vec2(0,1);
    return mix(mix(mix(hash3(f+o.xxx),hash3(f+o.yxx),s.x),
    mix(hash3(f+o.xyx),hash3(f+o.yyx),s.x),s.y),
    mix(mix(hash3(f+o.xxy),hash3(f+o.yxy),s.x),
    mix(hash3(f+o.xyy),hash3(f+o.yyy),s.x),s.y),s.z);
}
vec3 off(vec3 p)
{
    vec3 wave = cos(p.zxy*.5*Frequency+frameTimeCounter*Speed)*Amount;
    return (value3(p)*.4+value3(p/2.)*.6+value3(p/8.)-1.)*Terrain+wave;
}

void main()
{
    vec3 pos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    pos = mat3(gbufferModelViewInverse) * pos  + gbufferModelViewInverse[3].xyz;
    model = pos+cameraPosition;

    float c = fract(pos.y+cameraPosition.y);
    c *= min(10.-c/.1,1.);
    pos += off(pos+cameraPosition);
    vec3 h = pos+cameraPosition;
    pos.y -= off(cameraPosition-vec3(0,1,0)).y*Offset;
    float water = float(mc_Entity.x==1.);
    pos.y += ((cos(h.x*2.+h.y*1.+h.z*2.+frameTimeCounter*4.)*.1-.1)*c)*water*Terrain;
    world = pos;
    pos.y -= dot(pos.xz,pos.xz)/Radius/Radius*Toggle;

    gl_Position = gl_ProjectionMatrix * gbufferModelView * vec4(pos,1);
    gl_FogFragCoord = length(pos);

    color = gl_Color;
    coord0 = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    coord1 = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    id = mc_Entity.x;
}

/*
#version 120

varying vec4 color;
varying vec2 tex;

void main() {
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;

    color = gl_Color;
    tex = gl_MultiTexCoord0.xy;
}
*/

