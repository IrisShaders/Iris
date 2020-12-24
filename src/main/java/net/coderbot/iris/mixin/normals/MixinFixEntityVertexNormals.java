package net.coderbot.iris.mixin.normals;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * An attempt to fix entity vertex normals so that they are more similar to how they were in 1.14
 * <p>
 * In 1.14 and below, entities and blockentities were both rendered with a single draw call per individual entity, which
 * isn't particularly efficient. So in 1.15 and 1.16, Mojang made it so that many entities could be drawn in only a few
 * draw calls with their new rendering refactors.
 * <p>
 * The problem? There's a few assumptions in ShadersMod/Optifine related to the fact that entities are drawn in a single
 * draw call, and this is one of them. As it happens, the various transformations and translations that were previously
 * stored in the OpenGL matrix stack (gl_ModelViewMatrix) are now baked directly into the vertex data.
 * <p>
 * The result of multiplying a vertex position with gl_ModelViewMatrix or a vertex normal with gl_NormalMatrix will be
 * the same between 1.14 and below and 1.15 and above. What is different is the content of gl_ModelViewMatrix,
 * gl_NormalMatrix, as well as raw vertex normals and positions.
 * <p>
 * This mixin has been disabled for now, since the behavior without it matches OptiFine's current behavior on 1.16.3. It
 * has been preserved for now in case it ends up being necessary again at some point in the future.
 */
@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class MixinFixEntityVertexNormals {
	private static final String RENDER = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V";
	private static final String CHECK_EMPTY = "net/minecraft/client/render/WorldRenderer.checkEmpty(Lnet/minecraft/client/util/math/MatrixStack;)V";
	private static final String PROFILER_SWAP = "net/minecraft/util/profiler/Profiler.swap(Ljava/lang/String;)V";
	private static final String PUSH_MATRIX = "Lcom/mojang/blaze3d/systems/RenderSystem;pushMatrix()V";

	@Inject(method = RENDER, at = {
		// We're in a slice so this doesn't actually target the head of the method, but rather the head of the slice
		// That is, this @At targets the call to profiler.swap("entities")
		@At("HEAD"),
		// Every time Minecraft checks whether the matrix stack is empty, we tear down our modifications to the state
		// Once it's done checking, we restore our modifications
		@At(value = "INVOKE", target = CHECK_EMPTY, shift = At.Shift.AFTER),
		// Right before Minecraft starts rendering VertexConsumerProvider.Immediate buffers again
		@At(value = "INVOKE", target = "Lnet/minecraft/client/render/TexturedRenderLayers;getEntityTranslucentCull()Lnet/minecraft/client/render/RenderLayer;"),
		// The last of the immediate buffer drawing happens within translucency rendering
		// Use a custom slice here instead of the default one to avoid catching the getLines() call
		// around drawBlockOutline
		@At(value = "INVOKE", slice = "after_translucent_rendering",
			target = "Lnet/minecraft/client/render/RenderLayer;getLines()Lnet/minecraft/client/render/RenderLayer;")
	}, slice = {
		// The default slice, used for all @At values above that don't specify a custom slice
		@Slice(from = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=entities")),
		// Used for making sure that we don't catch the RenderLayer.getLines() call that happens in outline rendering,
		// we're only interested in it because we want to restore the matrix state before rendering the last
		// immediate buffers
		@Slice(id = "after_translucent_rendering", from = @At(value = "FIELD:FIRST",
			target = "Lnet/minecraft/client/render/WorldRenderer;transparencyShader:Lnet/minecraft/client/gl/ShaderEffect;")),
		// The opposite of the previous slice, everything up until the translucency rendering.
		@Slice(id = "before_translucent_rendering",
			from = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=entities"),
			to = @At(value = "FIELD:FIRST", target =
				"Lnet/minecraft/client/render/WorldRenderer;transparencyShader:Lnet/minecraft/client/gl/ShaderEffect;")
		)
	})
	private void iris$setupGlMatrix(MatrixStack matrices, float tickDelta, long limitTime,
									boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
									LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		// Don't bake the camera rotation / position into the vertices and vertex normals
		matrices.push();

		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.multMatrix(matrices.peek().getModel());

		matrices.peek().getModel().loadIdentity();
		matrices.peek().getNormal().loadIdentity();
	}

	@Inject(method = RENDER, at = {
		@At(value = "INVOKE", target = CHECK_EMPTY),
		// We only want to select the pushMatrix call that happens right before DebugRenderer::render
		// We use a custom slice here to make sure that we only target this single call.
		// Since the slice has a custom ID, it won't be used for any of the other @At entries here.
		//
		// NB: Make sure the custom slice name here matches the ID of the slice defined below!
		// Otherwise Mixin will silently ignore that fact that you're trying to use a slice at all.
		@At(value = "INVOKE", target = PUSH_MATRIX, slice = "before_debug_rendering"),
		@At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw()V",
			shift = At.Shift.AFTER)
	}, slice = @Slice(id = "before_debug_rendering",
		from = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;"),
		to = @At(value = "INVOKE", target =
			"Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V")
	))
	private void iris$teardownGlMatrix(MatrixStack matrices, float tickDelta, long limitTime,
									   boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
									   LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		// Pop our modified normal matrix from the stack
		matrices.pop();

		// Pop the matrix from the GL stack
		RenderSystem.popMatrix();
	}
}
