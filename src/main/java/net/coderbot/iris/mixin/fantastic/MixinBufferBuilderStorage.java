package net.coderbot.iris.mixin.fantastic;

import net.coderbot.iris.fantastic.ExtendedBufferStorage;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedMap;

@Mixin(BufferBuilderStorage.class)
public class MixinBufferBuilderStorage implements ExtendedBufferStorage {
	@Shadow
	@Final
	private SortedMap<RenderLayer, BufferBuilder> entityBuilders;

	@Unique
	private static void iris$assignBufferBuilder(SortedMap<RenderLayer, BufferBuilder> builderStorage, RenderLayer layer) {
		builderStorage.put(layer, new BufferBuilder(layer.getExpectedBufferSize()));
	}

	@Inject(method = "<init>()V", at = @At("RETURN"))
	private void iris$onInit(CallbackInfo ci) {
		// Add a few render layers to the list of specially-buffered layers in order to improve batching in some
		// common survival scenes.

		// Special-case for enderman eyes and spider eyes since they're so common.
		iris$assignBufferBuilder(entityBuilders, RenderLayer.getEyes(new Identifier("textures/entity/enderman/enderman_eyes.png")));
		iris$assignBufferBuilder(entityBuilders, RenderLayer.getEyes(new Identifier("textures/entity/enderman/spider_eyes.png")));

		// Similar deal with wool on sheeps.
		iris$assignBufferBuilder(entityBuilders, RenderLayer.getEntityCutoutNoCull(new Identifier("textures/entity/sheep/sheep_fur.png")));
	}

	@Unique
	private int begins = 0;

	@Override
	public void beginWorldRendering() {
		begins += 1;
	}

	@Override
	public void endWorldRendering() {
		begins -= 1;
	}
}
