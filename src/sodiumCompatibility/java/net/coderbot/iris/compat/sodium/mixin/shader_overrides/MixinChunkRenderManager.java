package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderBackend;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderManager;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ChunkRenderBackendExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkRenderManager.class)
public class MixinChunkRenderManager {
	@Redirect(method = "renderLayer",
			at = @At(value = "INVOKE",
					target = "me/jellysquid/mods/sodium/client/render/chunk/ChunkRenderBackend.begin (" +
								"Lcom/mojang/blaze3d/vertex/PoseStack;" +
							")V"))
	private void iris$backendBeginExt(ChunkRenderBackend<?> backend, PoseStack poseStack,
									  PoseStack poseStackArg, BlockRenderPass pass, double x, double y, double z) {
		if (backend instanceof ChunkRenderBackendExt) {
			((ChunkRenderBackendExt) backend).iris$begin(poseStack, pass);
		} else {
			backend.begin(poseStack);
		}
	}
}
