package net.irisshaders.iris.vulkan;

// this exists to help the drivers that do not manage to track this themselves... like NVIDIA! Thanks.
public class VulkanBindingState {
	private long pipeline;

	public void reset() {
		this.pipeline = 0;
	}

	public boolean pipeline(long pipeline) {
		if (this.pipeline == pipeline) return false;
		this.pipeline = pipeline;
		return true;
	}

}
