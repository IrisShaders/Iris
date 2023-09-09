#version 330 core

const int SAMPLE_COUNT = 10;

const float OFFSETS[10] = float[10](
    -8.456814493133681,
    -6.466941272204228,
    -4.477095577950016,
    -2.4872697699036146,
    -0.4974522695575193,
    1.4923608703786542,
    3.4821807463742394,
    5.472015437203308,
    7.461873982996055,
    9
);

const float WEIGHTS[10] = float[10](
    0.06618541985300355,
    0.08970336395182268,
    0.11210975911041324,
    0.12920061912152314,
    0.13730098788873435,
    0.13454575120851442,
    0.12157824474454274,
    0.10130423138377932,
    0.07783716371467683,
    0.03023445902298974
);


uniform float blurAmount;
uniform float frostAmount;

uniform vec2 texSize;

// blurDirection is:
//     vec2(1,0) for horizontal pass
//     vec2(0,1) for vertical pass
// The sourceTexture to be blurred MUST use linear filtering!
// pixelCoord is in [0..1]
vec4 blur(in sampler2D sourceTexture, vec2 blurDirection, vec2 pixelCoord)
{
    float blurAm = 0.5;
    vec4 result = vec4(0.0);
    for (int i = 0; i < SAMPLE_COUNT; ++i)
    {
        vec2 offset = blurDirection * OFFSETS[i] * (blurAm * 2) / texSize;
        float weight = WEIGHTS[i];
        result += texture(sourceTexture, pixelCoord + offset) * weight;
    }
    return result;
}

float random2 (in vec2 st) {
    return fract(sin(dot(st.xy,vec2(12.9898,78.233)))* 43758.5453123);
}

vec2 myPattern(in vec2 uv){
	vec2 uv2 = uv;
    uv2.y = uv2.y + 1.0 * (random2(uv));
    uv2.x = uv2.x + 1.0 * (random2(uv + 0.05));
    return uv2 - uv;
}

uniform sampler2D readImage;
uniform sampler2D noisetex;
uniform vec2 direction;

uniform vec4 noiseBounds;

layout (location = 0) out vec4 color;

void main() {
        vec2 size = gl_FragCoord.xy / texSize;
        /*if (size.x > 0.5) {
            color = texture(readImage, gl_FragCoord.xy / texSize);
            return;
        }*/
    float blurAm = frostAmount;

      //if (gl_FragCoord.x > noiseBounds.x && gl_FragCoord.y > noiseBounds.y && gl_FragCoord.x < noiseBounds.z && gl_FragCoord.y < noiseBounds.w) {
      vec2 p = size;
            for (int i = 0; i < 3; i ++) p += myPattern(p) * 0.005;
            for (int i = 0; i < 3; i ++) p -= myPattern(p) * 0.005;
            size = mix(size, p, blurAm);
      //}

        vec4 originalImage = texture(readImage, gl_FragCoord.xy / texSize);
        float distance = (size.y > 0.5 ? -distance(1.0, size.y) : -distance(0, size.y)) * 3;
        color = blur(readImage, direction, size) * mix(vec4(0.6, 0.6, 0.6, 1.0), vec4(0.45, 0.45, 0.45, 1.0), distance);
        color = mix(originalImage, color, blurAmount);
}
