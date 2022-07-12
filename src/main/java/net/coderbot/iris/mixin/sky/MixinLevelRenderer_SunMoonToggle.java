package net.coderbot.iris.mixin.sky;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows pipelines to disable the sun, moon, or both.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer_SunMoonToggle {
	/**
	 * This is a convenient way to disable rendering the sun / moon, since this clears the sun's vertices from
	 * the buffer, then when BufferRenderer is passed the empty buffer it will notice that it's empty and
	 * won't dispatch an unnecessary draw call. Nice!
	 */
	@Unique
	private void iris$emptyBuilder() {
		BufferBuilder builder = Tesselator.getInstance().getBuilder();

		builder.discard();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		builder.end();
	}

	@Inject(method = "renderSky",
		at = @At(value = "INVOKE", target = "com/mojang/blaze3d/vertex/BufferUploader.end (Lcom/mojang/blaze3d/vertex/BufferBuilder;)V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.SUN_LOCATION : Lnet/minecraft/resources/ResourceLocation;"),
			to = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.MOON_LOCATION : Lnet/minecraft/resources/ResourceLocation;")),
		allow = 1)
	private void iris$beforeDrawSun(PoseStack arg, Matrix4f arg2, float f, Camera arg3, boolean bl, Runnable runnable, CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSun).orElse(true)) {
			iris$emptyBuilder();
		}
	}

	@Inject(method = "renderSky",
		at = @At(value = "INVOKE", target = "com/mojang/blaze3d/vertex/BufferUploader.end (Lcom/mojang/blaze3d/vertex/BufferBuilder;)V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "net/minecraft/client/renderer/LevelRenderer.MOON_LOCATION : Lnet/minecraft/resources/ResourceLocation;"),
			to = @At(value = "INVOKE", target = "net/minecraft/client/multiplayer/ClientLevel.getStarBrightness (F)F")),
		allow = 1)
	private void iris$beforeDrawMoon(PoseStack arg, Matrix4f arg2, float f, Camera arg3, boolean bl, Runnable runnable, CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderMoon).orElse(true)) {
			iris$emptyBuilder();
		}
	}
}
