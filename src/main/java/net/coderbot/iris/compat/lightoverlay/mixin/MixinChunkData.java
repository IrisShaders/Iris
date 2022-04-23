package net.coderbot.iris.compat.lightoverlay.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows us to figure out when Light Overlay is about to dispatch a draw call, since the 1.16.5 version of Light Overlay
 * does not use GlStateManager#drawArrays. This is required for us to update our state in time.
 *
 * This is the code path for when Light Overlay uses display lists to draw crosses.
 */
@Pseudo
@Mixin(targets = "me/shedaniel/lightoverlay/common/fabric/ChunkData")
public class MixinChunkData {
	@Redirect(method = "renderList", at = @At(value = "INVOKE", target = "org/lwjgl/opengl/GL11.glCallList (I)V"))
	private void renderList(int listId) {
		Iris.getPipelineManager().getPipeline().ifPresent(WorldRenderingPipeline::syncProgram);
	}
}
