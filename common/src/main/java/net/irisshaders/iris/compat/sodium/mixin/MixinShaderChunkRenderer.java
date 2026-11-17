package net.irisshaders.iris.compat.sodium.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.GraphicsBackend;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.minecraft.client.renderer.oit.OitStage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderChunkRenderer.class)
public abstract class MixinShaderChunkRenderer {
	@Inject(method = "compileProgram", at = @At("RETURN"))
	private void iris$preparePipelines(TerrainRenderPass pass, OitStage stage, @Coerce Object parameters,
		CallbackInfoReturnable<RenderPipeline> cir) {
		if (GraphicsBackend.isVulkan() && Iris.isPackInUseQuick()) {
			IrisPipelines.registerPipeline(cir.getReturnValue());
		}
	}

	@Redirect(method = "createShader", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/ShaderChunkRenderer;vertexFormat:Lcom/mojang/renderpearl/api/vertex/VertexFormat;"))
	private VertexFormat iris$forceSoWeCanLookUpLater(ShaderChunkRenderer instance) {
		return ChunkMeshFormats.COMPACT.getVertexFormat();
	}
}
