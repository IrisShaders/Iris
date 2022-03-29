package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import net.caffeinemc.gfx.api.buffer.BufferMapFlags;
import net.caffeinemc.gfx.api.buffer.BufferStorageFlags;
import net.caffeinemc.gfx.api.buffer.MappedBuffer;
import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.sodium.render.chunk.draw.DefaultChunkRenderer;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public class MixinDefaultChunkRenderer {
	@Shadow
	@Final
	private MappedBuffer bufferCameraMatrices;

	@Unique
	private MappedBuffer bufferCameraMatricesShadow;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void newBuffers(RenderDevice device, TerrainVertexType vertexType, CallbackInfo ci) {
		this.bufferCameraMatricesShadow = device.createMappedBuffer(192L, EnumSet.of(BufferStorageFlags.WRITABLE, BufferStorageFlags.COHERENT, BufferStorageFlags.PERSISTENT), EnumSet.of(BufferMapFlags.WRITE, BufferMapFlags.COHERENT, BufferMapFlags.PERSISTENT));
	}

	@Redirect(method = "setupUniforms", at = @At(value = "FIELD", target = "Lnet/caffeinemc/sodium/render/chunk/draw/DefaultChunkRenderer;bufferCameraMatrices:Lnet/caffeinemc/gfx/api/buffer/MappedBuffer;"))
	private MappedBuffer redirectCameraMatrices(DefaultChunkRenderer instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? bufferCameraMatricesShadow : bufferCameraMatrices;
	}
}
