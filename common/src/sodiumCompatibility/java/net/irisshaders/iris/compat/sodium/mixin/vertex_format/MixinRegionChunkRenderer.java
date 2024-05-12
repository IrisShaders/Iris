package net.irisshaders.iris.compat.sodium.mixin.vertex_format;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.gl.tessellation.TessellationBinding;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
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

	@Redirect(remap = false, method = "createRegionTessellation", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gl/tessellation/TessellationBinding;forVertexBuffer(Lnet/caffeinemc/mods/sodium/client/gl/buffer/GlBuffer;[Lnet/caffeinemc/mods/sodium/client/gl/attribute/GlVertexAttributeBinding;)Lnet/caffeinemc/mods/sodium/client/gl/tessellation/TessellationBinding;"))
	private TessellationBinding iris$onInit(GlBuffer buffer, GlVertexAttributeBinding[] attributes) {
		if (!WorldRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()) {
			return TessellationBinding.forVertexBuffer(buffer, attributes);
		}

		attributes = new GlVertexAttributeBinding[]{
			new GlVertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_POSITION_HI,
				vertexFormat.getAttribute(ChunkMeshAttribute.POSITION_HI)),
			new GlVertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_POSITION_LO,
				vertexFormat.getAttribute(ChunkMeshAttribute.POSITION_LO)),
			new GlVertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_COLOR,
				vertexFormat.getAttribute(ChunkMeshAttribute.COLOR)),
			new GlVertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_TEXTURE,
				vertexFormat.getAttribute(ChunkMeshAttribute.TEXTURE)),
			new GlVertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_MATERIAL_INDEX,
				vertexFormat.getAttribute(ChunkMeshAttribute.LIGHT_MATERIAL_INDEX)),
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
