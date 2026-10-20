package net.irisshaders.iris.backend;

import com.mojang.renderpearl.api.GpuFormat;

public class IrisGpuFormats {
	// NeoForge is garbage and I'm not actually allowed to add this to the source as of 26.3.
	public static final GpuFormat RGB565_UNORM = GpuFormat.valueOf("IRIS_RGB565_UNORM");
	public static final GpuFormat RGB5A1_UNORM = GpuFormat.valueOf("IRIS_RGB5A1_UNORM");
	public static final GpuFormat RGBA4_UNORM = GpuFormat.valueOf("IRIS_RGBA4_UNORM");
	public static final GpuFormat RGB9E5_FLOAT = GpuFormat.valueOf("IRIS_RGB9E5_FLOAT");
}
