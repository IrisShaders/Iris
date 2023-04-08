package net.irisshaders.iris.mixin.state_tracking;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.irisshaders.iris.Iris;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin listens for code that binds a different framebuffer from the main minecraft framebuffer, and then disables
 * shaders accordingly.
 *
 * <p>This catches most cases of mods that are trying to render things offscreen to a different framebuffer, before
 * potentially using the content of that framebuffer in something that is eventually rendered into the world.
 * Ideally, we'd do this on GlStateManager, but that was too complex to implement for now (since we'd need to handle
 * the case of an Iris framebuffer being bound).</p>
 *
 * <p>Compatibility considerations:</p>
 *
 * <ul>
 *     <li>Immersive Portals: This doesn't appear to break anything with Immersive Portals, but it isn't needed either.</li>
 *     <li>Lifts: Lifts draws GUI content to an offscreen framebuffer, and then draws that framebuffer as a texture to.
 *         the screen. This mixin allows it to work seamlessly with Iris without Lifts needing special compat code for
 *         Iris.</li>
 *     <li>Vanilla Minecraft: This allows us to handle the Glowing effect without any other special casing needed. We
 *         automatically switch off shader rendering as needed when Minecraft is writing to the glowing framebuffer.
 * <p>
 *         No shader packs implement the glowing effect correctly, and few even try to have a decent fallback. The issue
 *         is that implementing it properly requires a separate depth buffer, and shader packs have limited control over
 *         depth buffers.
 * <p>
 *         As it turns out, the implementation of the Glowing effect in Vanilla works fine for the most part. It uses a
 * 		   framebuffer that is completely separate from that of shader pack rendering, and the effect is only applied
 * 		   once the world has finished rendering.</li>
 * </ul>
 */
@Mixin(RenderTarget.class)
public class MixinRenderTarget {
	@Inject(method = "bindWrite(Z)V", at = @At("RETURN"))
	private void iris$onBindFramebuffer(boolean bl, CallbackInfo ci) {
		// IntelliJ is wrong here. It doesn't understand how Mixin works.
		boolean mainBound = this == (Object) Minecraft.getInstance().getMainRenderTarget();

		Iris.getPipelineManager().getPipeline()
			.ifPresent(pipeline -> pipeline.getRenderTargetStateListener().setIsMainBound(mainBound));
	}
}
