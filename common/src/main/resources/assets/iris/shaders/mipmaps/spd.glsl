layout(local_size_x = 256) in;

shared uint spdCounter;
shared vec4 intermediate[16][16];

uint SpdGetAtomicCounter() {
	return spdCounter;
}

void SpdIncreaseAtomicCounter(uint slice) {
	spdCounter = atomicAdd(counter, 1);
}

void SpdResetAtomicCounter(uint slice) {
	atomicExchange(counter, 0);
}

vec4 SpdLoadSourceImage(ivec2 tex, uint slice) {
	return texelFetch(sourceImage, clamp(tex, ivec2(0), sourceSize - 1), 0);
}

vec4 SpdLoad(ivec2 tex, uint slice) {
	ivec2 size = max(sourceSize >> 6, ivec2(1));
	return imageLoad(mipImages[5], clamp(tex, ivec2(0), size - 1));
}

void SpdStore(ivec2 tex, vec4 value, uint mip, uint slice) {
	ivec2 size = max(sourceSize >> int(mip + 1), ivec2(1));
	if (any(greaterThanEqual(tex, size))) return;
	storeMip(mip, tex, value);
}

vec4 SpdLoadIntermediate(uint x, uint y) {
	return intermediate[x][y];
}

void SpdStoreIntermediate(uint x, uint y, vec4 value) {
	intermediate[x][y] = value;
}

vec4 SpdReduce4(vec4 a, vec4 b, vec4 c, vec4 d) {
	return (a + b + c + d) * 0.25;
}

// FFX_SPD

void main() {
	SpdDownsample(gl_WorkGroupID.xy, gl_LocalInvocationIndex, initialMips, numWorkGroups, 0);
	finishMipmaps(initialMips);
}
