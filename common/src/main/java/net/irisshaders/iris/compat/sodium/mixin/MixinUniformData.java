package net.irisshaders.iris.compat.sodium.mixin;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager;
import net.irisshaders.iris.mixinterface.ShadowRenderListAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DynamicGpuDataStorage;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.client.renderer.DynamicGpuDataStorageNonMapped;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UniformBufferManager.class)
public class MixinUniformData implements ShadowRenderListAccess {
	@Shadow
	private boolean hasUpdatedThisFrame;
	@Mutable
	@Shadow
	@Final
	private DynamicGpuDataStorage<?> uniformStorage;
	@Shadow
	private GpuBufferSlice uniformData;
	@Unique
	private DynamicGpuDataStorage<?> shadowUbo;
	@Unique
	private GpuBufferSlice shadowUboSlice;

	@Unique
	private boolean shadowUboFrame;

	@Unique
	private boolean isSwappedToShadow = false;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$init(ClientLevel level, int renderDistance, CallbackInfo ci) {
		this.shadowUbo = new DynamicGpuDataStorageMapped<>("Sodium terrain uniforms (Shadow)", 256, GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM, 8);
	}

	@Inject(method = "endFrame", at = @At("HEAD"))
	private void iris$endFrame(CallbackInfo ci) {
		this.shadowUboSlice = null;
		this.shadowUbo.endFrame();
	}

	@Override
	public void iris$beginShadowRenderListScope() {
		if (isSwappedToShadow) return;
		isSwappedToShadow = true;

		var x = this.uniformStorage;
		this.uniformStorage = shadowUbo;
		this.shadowUbo = x;

		var z = this.uniformData;
		this.uniformData = shadowUboSlice;
		this.shadowUboSlice = z;

		var y = this.hasUpdatedThisFrame;
		this.hasUpdatedThisFrame = this.shadowUboFrame;
		this.shadowUboFrame = y;
	}

	@Override
	public void iris$endShadowRenderListScope() {
		if (!isSwappedToShadow) return;
		isSwappedToShadow = false;

		var x = this.uniformStorage;
		this.uniformStorage = this.shadowUbo;
		this.shadowUbo = x;

		var z = this.uniformData;
		this.uniformData = shadowUboSlice;
		this.shadowUboSlice = z;

		var y = this.hasUpdatedThisFrame;
		this.hasUpdatedThisFrame = this.shadowUboFrame;
		this.shadowUboFrame = y;
	}
}
