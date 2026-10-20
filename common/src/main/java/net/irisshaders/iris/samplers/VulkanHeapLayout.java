package net.irisshaders.iris.samplers;

public record VulkanHeapLayout(int samplers, int images, int buffers) {
	public static final int FRAMES_IN_FLIGHT = 3;
	public static final int SAMPLER_COUNT = 128;
	public static final int IMAGE_COUNT = 2048;
	public static final int BUFFER_COUNT = 256;
	public static final int DESCRIPTOR_SET = 0;
	public static final int BUFFERS_BINDING = 2;
	public static final int FRAME_UNIFORMS_BINDING = 3;
	public static final int SAMPLERS_BINDING = 0;
	public static final int IMAGES_BINDING = 1;
	public static final int DRAW_BUFFER_BINDING = SamplerHeapLayout.Type.values().length;
	public static final VulkanHeapLayout DEFAULT = new VulkanHeapLayout(SAMPLER_COUNT, IMAGE_COUNT, BUFFER_COUNT);

	public static String sourceSampler2D(String name) {
		return """
			layout(set = %d, binding = 0) uniform sampler2D %s;
			""".formatted(SamplerHeapLayout.DRAW_DESCRIPTOR_SET, name);
	}
}
