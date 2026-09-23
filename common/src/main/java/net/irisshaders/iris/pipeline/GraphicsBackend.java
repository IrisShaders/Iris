package net.irisshaders.iris.pipeline;

import com.mojang.renderpearl.api.device.GpuDevice;

public enum GraphicsBackend {
	OPENGL,
	VULKAN;

	private static GraphicsBackend active;

	public static void initialize(GpuDevice device) {
		active = fromName(device.getDeviceInfo().backendName());
	}

	public static GraphicsBackend fromName(String name) {
		if ("OpenGL".equalsIgnoreCase(name)) {
			return OPENGL;
		}

		if ("Vulkan".equalsIgnoreCase(name)) {
			return VULKAN;
		}

		throw new IllegalArgumentException("unsupported Iris graphics backend: " + name);
	}

	public static boolean isOpenGL() {
		return active == OPENGL;
	}

	public static boolean isVulkan() {
		return active == VULKAN;
	}

}
