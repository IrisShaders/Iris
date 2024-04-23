package net.irisshaders.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import me.jellysquid.mods.sodium.client.gl.buffer.GlBuffer;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.tessellation.TessellationBinding;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.irisshaders.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DefaultChunkRenderer.class)
public abstract class MixinRegionChunkRenderer extends ShaderChunkRenderer {
	public MixinRegionChunkRenderer(RenderDevice device, ChunkVertexType vertexType) {
		super(device, vertexType);
	}

	@Redirect(remap = false, method = "createRegionTessellation", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/gl/tessellation/TessellationBinding;forVertexBuffer(Lme/jellysquid/mods/sodium/client/gl/buffer/GlBuffer;[Lme/jellysquid/mods/sodium/client/gl/attribute/GlVertexAttributeBinding;)Lme/jellysquid/mods/sodium/client/gl/tessellation/TessellationBinding;"))
	private TessellationBinding iris$onInit(GlBuffer buffer, GlVertexAttributeBinding[] attributes) {
		if (!WorldRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()) {
			return TessellationBinding.forVertexBuffer(buffer, attributes);
		}

		attributes = new GlVertexAttributeBinding[]{
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID,
				vertexFormat.getAttribute(ChunkMeshAttribute.POSITION_MATERIAL_MESH)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.ATTRIBUTE_COLOR,
				vertexFormat.getAttribute(ChunkMeshAttribute.COLOR_SHADE)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE,
				vertexFormat.getAttribute(ChunkMeshAttribute.BLOCK_TEXTURE)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE,
				vertexFormat.getAttribute(ChunkMeshAttribute.LIGHT_TEXTURE)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.MID_BLOCK,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_BLOCK)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.TANGENT)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL))
		};

		return TessellationBinding.forVertexBuffer(buffer, attributes);
	}
}
