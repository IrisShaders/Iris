shared uint completedWorkGroups;

vec4 reduceArea(ivec2 tex, ivec2 inputSize, uint inputMip) {
	ivec2 size = max(inputSize / 2, ivec2(1));
	// Integer overlap weights avoid cancellation at bright pixels far from the origin.
	ivec2 start = tex * (inputSize % size);
	ivec2 base = tex * (inputSize / size) + start / size;
	start %= size;
	ivec2 end = start + inputSize;
	ivec2 taps = (end + size - 1) / size;
	vec4 sum = vec4(0.0);
	for (int y = 0; y < taps.y; y++) {
		float weightY = float(min(end.y, (y + 1) * size.y) - max(start.y, y * size.y));
		for (int x = 0; x < taps.x; x++) {
			float weightX = float(min(end.x, (x + 1) * size.x) - max(start.x, x * size.x));
			sum += loadMip(inputMip, base + ivec2(x, y)) * (weightX * weightY);
		}
	}
	return sum / (float(inputSize.x) * float(inputSize.y));
}

void finishMipmaps(uint firstMip) {
	if (firstMip == mips) return;

	// Publish every workgroup's output before the last workgroup reads it.
	memoryBarrierImage();
	barrier();
	if (numWorkGroups > 1) {
		if (gl_LocalInvocationIndex == 0) {
			completedWorkGroups = atomicAdd(counter, 1);
		}
		barrier();
		if (completedWorkGroups != numWorkGroups - 1) return;
		if (gl_LocalInvocationIndex == 0) {
			atomicExchange(counter, 0);
		}
	}
	memoryBarrierImage();
	barrier();

	ivec2 inputSize = max(sourceSize >> int(firstMip), ivec2(1));
	for (uint mip = firstMip; mip < mips; mip++) {
		ivec2 size = max(inputSize / 2, ivec2(1));
		for (uint index = gl_LocalInvocationIndex; index < uint(size.x * size.y); index += 256) {
			ivec2 tex = ivec2(int(index) % size.x, int(index) / size.x);
			storeMip(mip, tex, reduceArea(tex, inputSize, mip));
		}
		memoryBarrierImage();
		barrier();
		inputSize = size;
	}
}
