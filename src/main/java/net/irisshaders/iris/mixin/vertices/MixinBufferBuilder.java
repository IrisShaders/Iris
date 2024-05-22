package net.irisshaders.iris.mixin.vertices;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.BufferBuilderPolygonView;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.vertices.ExtendingBufferBuilder;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisExtendedBufferBuilder;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dynamically and transparently extends the vanilla vertex formats with additional data
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements VertexConsumer, BlockSensitiveBufferBuilder, ExtendingBufferBuilder, IrisExtendedBufferBuilder {
	@Unique
	private final BufferBuilderPolygonView polygon = new BufferBuilderPolygonView();
	@Unique
	private final Vector3f normal = new Vector3f();
	@Unique
	private boolean iris$shouldNotExtend;
	@Unique
	private boolean extending;
	@Unique
	private boolean iris$isTerrain;
	@Unique
	private boolean injectNormalAndUV1;
	@Unique
	private int vertexCount;
	@Unique
	private short currentBlock = -1;
	@Unique
	private short currentRenderType = -1;
	@Unique
	private int currentLocalPosX;
	@Unique
	private int currentLocalPosY;
	@Unique
	private int currentLocalPosZ;
	@Shadow
	private ByteBufferBuilder buffer;

	@Shadow
	private int elementsToFill;

	@Shadow
	public abstract VertexConsumer setNormal(float f, float g, float h);

	@Shadow
	protected abstract long beginElement(VertexFormatElement vertexFormatElement);

	@Shadow
	private long vertexPointer;

	@Shadow
	@Final
	private VertexFormat.Mode mode;

	@Mutable
	@Shadow
	@Final
	private VertexFormat format;

	@Mutable
	@Shadow
	@Final
	private int vertexSize;

	@Mutable
	@Shadow
	@Final
	private int initialElementsToFill;

	@Mutable
	@Shadow
	@Final
	private int[] offsetsByElement;

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void iris$extendFormat(ByteBufferBuilder byteBufferBuilder, VertexFormat.Mode mode, VertexFormat vertexFormat, CallbackInfo ci) {
		extending = false;
		iris$isTerrain = false;
		injectNormalAndUV1 = false;

		VertexFormat format = vertexFormat;

		if (ImmediateState.skipExtend || !WorldRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()) {
			format = vertexFormat;
			return;
		}

		if (format == DefaultVertexFormat.BLOCK) {
			extending = true;
			iris$isTerrain = true;
			injectNormalAndUV1 = false;
			format = IrisVertexFormats.TERRAIN;
		} else if (format == DefaultVertexFormat.NEW_ENTITY) {
			extending = true;
			iris$isTerrain = false;
			injectNormalAndUV1 = false;
			format = IrisVertexFormats.ENTITY;
		} else if (format == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP) {
			extending = true;
			iris$isTerrain = false;
			injectNormalAndUV1 = true;
			format = IrisVertexFormats.GLYPH;
		}

		this.format = format;
		this.vertexSize = vertexFormat.getVertexSize();
		this.initialElementsToFill = vertexFormat.getElementsMask() & ~VertexFormatElement.POSITION.mask();
		this.offsetsByElement = vertexFormat.getOffsetsByElement();
	}

	//@Inject(method = "reset()V", at = @At("HEAD"))
	private void iris$onReset(CallbackInfo ci) {
		vertexCount = 0;
	}

	@Inject(method = "endLastVertex", at = @At("HEAD"))
	private void iris$beforeNext(CallbackInfo ci) {
		if (!extending || vertexPointer <= 0) {
			return;
		}

		if (injectNormalAndUV1 && this.elementsToFill == (this.elementsToFill & ~VertexFormatElement.NORMAL.mask())) {
			this.setNormal(0, 0, 0);
		}

		if (iris$isTerrain) {
			// ENTITY_ELEMENT
			long offset = this.vertexPointer + offsetsByElement[IrisVertexFormats.ENTITY_ELEMENT.id()];
			MemoryUtil.memPutShort(offset, currentBlock);
			MemoryUtil.memPutShort(offset + 2, currentRenderType);
		} else {
			// ENTITY_ID_ELEMENT
			long offset = this.vertexPointer + offsetsByElement[IrisVertexFormats.ENTITY_ID_ELEMENT.id()];
			MemoryUtil.memPutShort(offset, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
			MemoryUtil.memPutShort(offset + 2, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
			MemoryUtil.memPutShort(offset +4, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
		}

		this.elementsToFill = this.elementsToFill & ~IrisVertexFormats.MID_TEXTURE_ELEMENT.mask();
		this.elementsToFill = this.elementsToFill & ~IrisVertexFormats.TANGENT_ELEMENT.mask();

		if (iris$isTerrain) {
			// MID_BLOCK_ELEMENT
			long posIndex = this.vertexPointer;
			float x = MemoryUtil.memGetFloat(posIndex);
			float y = MemoryUtil.memGetFloat(posIndex + 4);
			float z = MemoryUtil.memGetFloat(posIndex + 8);
			long midOffset = this.beginElement(IrisVertexFormats.MID_BLOCK_ELEMENT);

			// TODO WHY
			if (midOffset > 0) {
				MemoryUtil.memPutInt(midOffset, ExtendedDataHelper.computeMidBlock(x, y, z, currentLocalPosX, currentLocalPosY, currentLocalPosZ));
			}
		}

		vertexCount++;

		if (mode == VertexFormat.Mode.QUADS && vertexCount == 4 || mode == VertexFormat.Mode.TRIANGLES && vertexCount == 3) {
			fillExtendedData(vertexCount);
		}
	}

	@Unique
	private void fillExtendedData(int vertexAmount) {

		int stride = format.getVertexSize();
		polygon.setup(vertexPointer + stride, stride, vertexCount);
		vertexCount = 0;

		float midU = 0;
		float midV = 0;

		for (int vertex = 0; vertex < vertexAmount; vertex++) {
			midU += polygon.u(vertex);
			midV += polygon.v(vertex);
		}

		midU /= vertexAmount;
		midV /= vertexAmount;

		int midUOffset;
		int midVOffset;
		int normalOffset;
		int tangentOffset;
		if (iris$isTerrain) {
			midUOffset = 16;
			midVOffset = 12;
			normalOffset = 24;
			tangentOffset = 8;
		} else {
			midUOffset = 14;
			midVOffset = 10;
			normalOffset = 24;
			tangentOffset = 6;
		}

		// TODO 24w21a
		/*
		if (vertexAmount == 3) {
			// NormalHelper.computeFaceNormalTri(normal, polygon);	// Removed to enable smooth shaded triangles. Mods rendering triangles with bad normals need to recalculate their normals manually or otherwise shading might be inconsistent.

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				int packedNormal = buffer.getInt(nextElementByte - normalOffset - stride * vertex); // retrieve per-vertex normal

				int tangent = NormalHelper.computeTangentSmooth(NormI8.unpackX(packedNormal), NormI8.unpackY(packedNormal), NormI8.unpackZ(packedNormal), polygon);

				buffer.putFloat(nextElementByte - midUOffset - stride * vertex, midU);
				buffer.putFloat(nextElementByte - midVOffset - stride * vertex, midV);
				buffer.putInt(nextElementByte - tangentOffset - stride * vertex, tangent);
			}
		} else {
			NormalHelper.computeFaceNormal(normal, polygon);
			int packedNormal = NormI8.pack(normal.x, normal.y, normal.z, 0.0f);
			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, polygon);

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				buffer.putFloat(nextElementByte - midUOffset - stride * vertex, midU);
				buffer.putFloat(nextElementByte - midVOffset - stride * vertex, midV);
				buffer.putInt(nextElementByte - normalOffset - stride * vertex, packedNormal);
				buffer.putInt(nextElementByte - tangentOffset - stride * vertex, tangent);
			}
		}*/
	}

	@Override
	public void beginBlock(short block, short renderType, int localPosX, int localPosY, int localPosZ) {
		this.currentBlock = block;
		this.currentRenderType = renderType;
		this.currentLocalPosX = localPosX;
		this.currentLocalPosY = localPosY;
		this.currentLocalPosZ = localPosZ;
	}

	@Override
	public void endBlock() {
		this.currentBlock = -1;
		this.currentRenderType = -1;
		this.currentLocalPosX = 0;
		this.currentLocalPosY = 0;
		this.currentLocalPosZ = 0;
	}

	@Override
	public VertexFormat iris$format() {
		return format;
	}

	@Override
	public VertexFormat.Mode iris$mode() {
		return mode;
	}

	@Override
	public boolean iris$extending() {
		return extending;
	}

	@Override
	public boolean iris$isTerrain() {
		return iris$isTerrain;
	}

	@Override
	public boolean iris$injectNormalAndUV1() {
		return injectNormalAndUV1;
	}

	@Override
	public int iris$vertexCount() {
		return vertexCount;
	}

	@Override
	public void iris$incrementVertexCount() {
		vertexCount++;
	}

	@Override
	public void iris$resetVertexCount() {
		vertexCount = 0;
	}

	@Override
	public short iris$currentBlock() {
		return currentBlock;
	}

	@Override
	public short iris$currentRenderType() {
		return currentRenderType;
	}

	@Override
	public int iris$currentLocalPosX() {
		return currentLocalPosX;
	}

	@Override
	public int iris$currentLocalPosY() {
		return currentLocalPosY;
	}

	@Override
	public int iris$currentLocalPosZ() {
		return currentLocalPosZ;
	}
}
