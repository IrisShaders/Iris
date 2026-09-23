layout(local_size_x = 16, local_size_y = 16) in;

void main() {
	ivec2 tex = ivec2(gl_GlobalInvocationID.xy);
	ivec2 size = max(sourceSize / 2, ivec2(1));
	if (all(lessThan(tex, size))) {
		storeMip(0, tex, reduceArea(tex, sourceSize, 0));
	}
	finishMipmaps(1);
}
