package net.coderbot.iris.mixin.integrationtest;


import com.mojang.blaze3d.pipeline.RenderTarget;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Tests switching the depth texture of the main Minecraft render target to use a stencil buffer, to ensure that
 * Iris remains compatible. Iris should continue to have good performance & should not spam the log with errors.
 *
 * This mixin is not enabled by default. To enable integration tests, add the mixins.iris.integrationtest.json file
 * to the fabric.mod.json mixins list. Note that you'll also want to test if this works with the GL 3.0 framebuffer
 * blit path, by manually disabling the GL 4.3 copy path.
 *
 * Previous issues:
 *
 * <ul>
 *     <li>Log spam when copying depth buffer content</li>
 *     <li>Extreme low FPS caused by hitting a driver slow path. We're talking ~13 FPS on an RTX 2070 Super with
 *         Sildur's Vibrant Medium.</li>
 * </ul>
 *
 * Based on https://gist.github.com/burgerguy/8233170683ad93eea6aa27ee02a5c4d1
 */
@Mixin (RenderTarget.class)
public class MixinRenderTarget_StencilBufferTest {
	private static final boolean STENCIL = true;

	@ModifyArgs (method = "createBuffers",
		at = @At (value = "INVOKE",
			target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
			ordinal = 0))
	public void init(Args args) {
		if (STENCIL) {
			// internalformat
			// NB: The original Gist sets this to 3, but that is incorrect. Arguments are zero-indexed.
			args.set(2, GL30.GL_DEPTH32F_STENCIL8);

			// format
			args.set(6, GL30.GL_DEPTH_STENCIL);

			// type
			args.set(7, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV);
		}
	}

	@ModifyArgs (method = "createBuffers",
		at = @At (value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V"),
		slice = @Slice (from = @At (value = "FIELD", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;useDepth:Z", ordinal = 1)))
	public void init2(Args args) {
		if (STENCIL) {
			// attachment
			args.set(1, GL30.GL_DEPTH_STENCIL_ATTACHMENT);
		}
	}
}
