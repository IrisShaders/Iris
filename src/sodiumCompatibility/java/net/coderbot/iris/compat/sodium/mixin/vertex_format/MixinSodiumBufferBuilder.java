package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.render.vertex.buffer.ExtendedBufferBuilder;
import me.jellysquid.mods.sodium.client.render.vertex.buffer.SodiumBufferBuilder;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.NormalAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisCommonVertexAttributes;
import net.coderbot.iris.compat.sodium.impl.vertex_format.SodiumBufferBuilderPolygonView;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.coderbot.iris.vertices.IrisExtendedBufferBuilder;
import net.coderbot.iris.vertices.NormI8;
import net.coderbot.iris.vertices.NormalHelper;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SodiumBufferBuilder.class)
public abstract class MixinSodiumBufferBuilder implements BlockSensitiveBufferBuilder {
	@Shadow
	@Final
	private static int ATTRIBUTE_NOT_PRESENT;

	@Shadow
	@Final
	private static int ATTRIBUTE_NORMAL_BIT;

	@Shadow
	@Final
	private ExtendedBufferBuilder builder;

	@Shadow
	private int attributeOffsetPosition;

	@Shadow
	private int attributeOffsetTexture;

	@Shadow
	private int attributeOffsetNormal;

	@Shadow
	private int requiredAttributes, writtenAttributes;

	@Shadow
	public abstract BufferBuilder getOriginalBufferBuilder();

	@Shadow
	abstract void putNormalAttribute(int normal);

	@Unique
	private static final int
			ATTRIBUTE_TANGENT_BIT       = 1 << IrisCommonVertexAttributes.TANGENT.ordinal(),
			ATTRIBUTE_MID_TEX_COORD_BIT = 1 << IrisCommonVertexAttributes.MID_TEX_COORD.ordinal(),
			ATTRIBUTE_BLOCK_ID_BIT      = 1 << IrisCommonVertexAttributes.BLOCK_ID.ordinal(),
			ATTRIBUTE_ENTITY_ID_BIT     = 1 << IrisCommonVertexAttributes.ENTITY_ID.ordinal(),
			ATTRIBUTE_MID_BLOCK_BIT     = 1 << IrisCommonVertexAttributes.MID_BLOCK.ordinal();

	@Unique
	private int
		    attributeOffsetTangent,
		    attributeOffsetMidTexCoord,
		    attributeOffsetBlockId,
		    attributeOffsetEntityId,
		    attributeOffsetMidBlock;

	@Unique
	private final SodiumBufferBuilderPolygonView polygon = new SodiumBufferBuilderPolygonView();

	@Unique
	private final Vector3f normal = new Vector3f();

	@Unique
	private void putBlockIdAttribute(short s0, short s1) {
		if (this.attributeOffsetBlockId == ATTRIBUTE_NOT_PRESENT) {
			return;
		}

		final long offset = MemoryUtil.memAddress(this.builder.sodium$getBuffer(), this.builder.sodium$getElementOffset() + this.attributeOffsetBlockId);
		MemoryUtil.memPutShort(offset, s0);
		MemoryUtil.memPutShort(offset + 2, s1);

		this.writtenAttributes |= ATTRIBUTE_BLOCK_ID_BIT;
	}

	@Unique
	private void putEntityIdAttribute(short s0, short s1, short s2) {
		if (this.attributeOffsetEntityId == ATTRIBUTE_NOT_PRESENT) {
			return;
		}

		final long offset = MemoryUtil.memAddress(this.builder.sodium$getBuffer(), this.builder.sodium$getElementOffset() + this.attributeOffsetEntityId);
		MemoryUtil.memPutShort(offset, s0);
		MemoryUtil.memPutShort(offset + 2, s1);
		MemoryUtil.memPutShort(offset + 4, s2);

		this.writtenAttributes |= ATTRIBUTE_ENTITY_ID_BIT;
	}

	@Unique
	private void putMidBlockAttribute(int midBlock) {
		if (this.attributeOffsetMidBlock == ATTRIBUTE_NOT_PRESENT) {
			return;
		}

		final long offset = MemoryUtil.memAddress(this.builder.sodium$getBuffer(), this.builder.sodium$getElementOffset() + this.attributeOffsetMidBlock);
		MemoryUtil.memPutInt(offset, midBlock);

		this.writtenAttributes |= ATTRIBUTE_MID_BLOCK_BIT;
	}

	@Override
	public void beginBlock(short block, short renderType, int localPosX, int localPosY, int localPosZ) {
		((BlockSensitiveBufferBuilder) getOriginalBufferBuilder()).beginBlock(block, renderType, localPosX, localPosY, localPosZ);
	}

	@Override
	public void endBlock() {
		((BlockSensitiveBufferBuilder) getOriginalBufferBuilder()).endBlock();
	}

	@Inject(method = "resetAttributeBindings", at = @At("RETURN"), remap = false)
	private void onResetAttributeBindings(CallbackInfo ci) {
		attributeOffsetTangent = ATTRIBUTE_NOT_PRESENT;
		attributeOffsetMidTexCoord = ATTRIBUTE_NOT_PRESENT;
		attributeOffsetBlockId = ATTRIBUTE_NOT_PRESENT;
		attributeOffsetEntityId = ATTRIBUTE_NOT_PRESENT;
		attributeOffsetMidBlock = ATTRIBUTE_NOT_PRESENT;
	}

	@Inject(method = "updateAttributeBindings", at = @At("RETURN"), remap = false)
	private void onUpdateAttributeBindings(VertexFormatDescription desc, CallbackInfo ci) {
		if (desc.containsElement(IrisCommonVertexAttributes.TANGENT)) {
			requiredAttributes |= ATTRIBUTE_TANGENT_BIT;
			attributeOffsetTangent = desc.getElementOffset(IrisCommonVertexAttributes.TANGENT);
		}

		if (desc.containsElement(IrisCommonVertexAttributes.MID_TEX_COORD)) {
			requiredAttributes |= ATTRIBUTE_MID_TEX_COORD_BIT;
			attributeOffsetMidTexCoord = desc.getElementOffset(IrisCommonVertexAttributes.MID_TEX_COORD);
		}

		if (desc.containsElement(IrisCommonVertexAttributes.BLOCK_ID)) {
			requiredAttributes |= ATTRIBUTE_BLOCK_ID_BIT;
			attributeOffsetBlockId = desc.getElementOffset(IrisCommonVertexAttributes.BLOCK_ID);
		}

		if (desc.containsElement(IrisCommonVertexAttributes.ENTITY_ID)) {
			requiredAttributes |= ATTRIBUTE_ENTITY_ID_BIT;
			attributeOffsetEntityId = desc.getElementOffset(IrisCommonVertexAttributes.ENTITY_ID);
		}

		if (desc.containsElement(IrisCommonVertexAttributes.MID_BLOCK)) {
			requiredAttributes |= ATTRIBUTE_MID_BLOCK_BIT;
			attributeOffsetMidBlock = desc.getElementOffset(IrisCommonVertexAttributes.MID_BLOCK);
		}
	}

	@Inject(method = "endVertex", at = @At("HEAD"))
	private void onEndVertex(CallbackInfo ci) {
		IrisExtendedBufferBuilder ext = (IrisExtendedBufferBuilder) builder;

		if (!ext.iris$extending()) {
			return;
		}

		if (ext.iris$injectNormalAndUV1() && (writtenAttributes & ATTRIBUTE_NORMAL_BIT) == 0) {
			putNormalAttribute(0);
		}

		if (ext.iris$isTerrain()) {
			putBlockIdAttribute(ext.iris$currentBlock(), ext.iris$currentRenderType());
		} else {
			putEntityIdAttribute((short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity(),
				(short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity(),
				(short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
		}

		this.writtenAttributes |= ATTRIBUTE_MID_TEX_COORD_BIT;
		this.writtenAttributes |= ATTRIBUTE_TANGENT_BIT;

		if (ext.iris$isTerrain()) {
			final long offset = MemoryUtil.memAddress(this.builder.sodium$getBuffer(), this.builder.sodium$getElementOffset() + this.attributeOffsetPosition);
			float x = PositionAttribute.getX(offset);
			float y = PositionAttribute.getY(offset);
			float z = PositionAttribute.getZ(offset);
			putMidBlockAttribute(ExtendedDataHelper.computeMidBlock(x, y, z, ext.iris$currentLocalPosX(), ext.iris$currentLocalPosY(), ext.iris$currentLocalPosZ()));
		}

		ext.iris$incrementVertexCount();

		VertexFormat.Mode mode = ext.iris$mode();
		int vertexCount = ext.iris$vertexCount();

		if (mode == VertexFormat.Mode.QUADS && vertexCount == 4 || mode == VertexFormat.Mode.TRIANGLES && vertexCount == 3) {
			fillExtendedData(vertexCount);
		}
	}

	@Unique
	private void fillExtendedData(int vertexAmount) {
		IrisExtendedBufferBuilder ext = (IrisExtendedBufferBuilder) builder;

		ext.iris$resetVertexCount();

		int stride = ext.iris$format().getVertexSize();

		long ptr = MemoryUtil.memAddress(builder.sodium$getBuffer(), this.builder.sodium$getElementOffset());
		polygon.setup(ptr, attributeOffsetPosition, attributeOffsetTexture, stride, vertexAmount);

		float midU = 0;
		float midV = 0;

		for (int vertex = 0; vertex < vertexAmount; vertex++) {
			midU += polygon.u(vertex);
			midV += polygon.v(vertex);
		}

		midU /= vertexAmount;
		midV /= vertexAmount;

		if (vertexAmount == 3) {
			// NormalHelper.computeFaceNormalTri(normal, polygon);	// Removed to enable smooth shaded triangles. Mods rendering triangles with bad normals need to recalculate their normals manually or otherwise shading might be inconsistent.

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				int packedNormal = NormalAttribute.get(ptr + attributeOffsetNormal - stride * vertex); // retrieve per-vertex normal

				int tangent = NormalHelper.computeTangentSmooth(NormI8.unpackX(packedNormal), NormI8.unpackY(packedNormal), NormI8.unpackZ(packedNormal), polygon);

				MemoryUtil.memPutFloat(ptr + attributeOffsetMidTexCoord - stride * vertex, midU);
				MemoryUtil.memPutFloat(ptr + attributeOffsetMidTexCoord + 4 - stride * vertex, midV);
				MemoryUtil.memPutInt(ptr + attributeOffsetTangent - stride * vertex, tangent);
			}
		} else {
			NormalHelper.computeFaceNormal(normal, polygon);
			int packedNormal = NormI8.pack(normal.x, normal.y, normal.z, 0.0f);
			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, polygon);

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				MemoryUtil.memPutFloat(ptr + attributeOffsetMidTexCoord - stride * vertex, midU);
				MemoryUtil.memPutFloat(ptr + attributeOffsetMidTexCoord + 4 - stride * vertex, midV);
				MemoryUtil.memPutInt(ptr + attributeOffsetNormal - stride * vertex, packedNormal);
				MemoryUtil.memPutInt(ptr + attributeOffsetTangent - stride * vertex, tangent);
			}
		}
	}
}
