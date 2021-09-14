package net.coderbot.iris.mixin.fantastic;

import net.coderbot.batchedentityrendering.impl.RenderBuffersExt;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.SortedMap;

@Mixin(RenderBuffers.class)
public class MixinRenderBuffers implements RenderBuffersExt {
	@Shadow
	@Final
	private SortedMap<RenderType, BufferBuilder> fixedBuffers;

	@Unique
	private static void iris$assignBufferBuilder(SortedMap<RenderType, BufferBuilder> builderStorage, RenderType type) {
		builderStorage.put(type, new BufferBuilder(type.bufferSize()));
	}

	@Inject(method = "<init>()V", at = @At("RETURN"))
	private void iris$onInit(CallbackInfo ci) {
		// Add a few render layers to the list of specially-buffered layers in order to improve batching in some
		// common survival scenes.

		// Special-case for enderman eyes and spider eyes since they're so common.
		iris$assignBufferBuilder(fixedBuffers, RenderType.eyes(new ResourceLocation("textures/entity/enderman/enderman_eyes.png")));
		iris$assignBufferBuilder(fixedBuffers, RenderType.eyes(new ResourceLocation("textures/entity/enderman/spider_eyes.png")));

		// Similar deal with wool on sheeps.
		iris$assignBufferBuilder(fixedBuffers, RenderType.entityCutoutNoCull(new ResourceLocation("textures/entity/sheep/sheep_fur.png")));
	}

	@Unique
	private int begins = 0;

	@Override
	public void beginLevelRendering() {
		begins += 1;
	}

	@Override
	public void endLevelRendering() {
		begins -= 1;
	}
}
