package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.GpuFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GpuFormat.class)
public enum MixinGpuFormat {
	IRIS_RGB565_UNORM(GpuFormat.ComponentType.OPAQUE_16, 1),
	IRIS_RGB5A1_UNORM(GpuFormat.ComponentType.OPAQUE_16, 1),
	IRIS_RGB9E5_FLOAT(GpuFormat.ComponentType.OPAQUE_32, 1);

	@Shadow
	MixinGpuFormat(GpuFormat.ComponentType componentType, int componentCount) {
	}
}
