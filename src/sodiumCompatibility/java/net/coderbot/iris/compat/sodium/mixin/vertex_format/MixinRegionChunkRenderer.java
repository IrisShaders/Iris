package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import net.caffeinemc.gfx.api.array.VertexArrayResourceBinding;
import net.caffeinemc.gfx.api.array.attribute.VertexAttributeBinding;
import net.caffeinemc.gfx.api.array.attribute.VertexFormat;
import net.caffeinemc.sodium.render.chunk.draw.ShaderChunkRenderer;
import net.caffeinemc.sodium.render.terrain.format.TerrainMeshAttribute;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import net.irisshaders.iris.api.v0.IrisApi;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShaderChunkRenderer.class)
public class MixinRegionChunkRenderer {
	@Shadow
	@Final
	protected VertexFormat<TerrainMeshAttribute> vertexFormat;
	/*@Shadow(remap = false)
	@Final
	@Mutable
	private GlVertexAttributeBinding[] vertexAttributeBindings;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$onInit(RenderDevice device, ChunkVertexType vertexType, CallbackInfo ci) {
		if (!Iris.isPackActive()) {
			return;
		}

		GlVertexFormat<ChunkMeshAttribute> vertexFormat = vertexType.getCustomVertexFormat();

		vertexAttributeBindings = ArrayUtils.addAll(vertexAttributeBindings,
				new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
						vertexFormat.getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
				new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
						vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
				new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
						vertexFormat.getAttribute(IrisChunkMeshAttributes.TANGENT)),
				new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
						vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL))
		);
	}*/
}
