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
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dynamically and transparently extends the vanilla vertex formats with additional data
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements VertexConsumer, BlockSensitiveBufferBuilder {
	@Shadow
	private int elementsToFill;

	@Shadow
	public abstract VertexConsumer setNormal(float f, float g, float h);

	@Shadow
	protected abstract long beginElement(VertexFormatElement vertexFormatElement);

	@Shadow
	@Final
	private VertexFormat.Mode mode;
	@Shadow
	@Final
	private VertexFormat format;
	@Shadow
	@Final
	private int[] offsetsByElement;
	@Shadow
	@Final
	private boolean fastFormat;
	@Shadow
	private long vertexPointer;
	@Shadow
	private int vertices;
	@Unique
	private final BufferBuilderPolygonView polygon = new BufferBuilderPolygonView();
	@Unique
	private final Vector3f normal = new Vector3f();
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

	@ModifyVariable(method = "<init>", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/vertex/VertexFormatElement;POSITION:Lcom/mojang/blaze3d/vertex/VertexFormatElement;", ordinal = 0), argsOnly = true)
	private VertexFormat iris$extendFormat(VertexFormat format) {
		iris$isTerrain = false;
		injectNormalAndUV1 = false;

		if (ImmediateState.skipExtension.get() || !WorldRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()) {
			return format;
		}

		if (format == DefaultVertexFormat.BLOCK || format == IrisVertexFormats.TERRAIN) {
			extending = true;
			iris$isTerrain = true;
			injectNormalAndUV1 = false;
			return IrisVertexFormats.TERRAIN;
		} else if (format == DefaultVertexFormat.NEW_ENTITY || format == IrisVertexFormats.ENTITY) {
			extending = true;
			iris$isTerrain = false;
			injectNormalAndUV1 = false;
			return IrisVertexFormats.ENTITY;
		} else if (format == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP || format == IrisVertexFormats.GLYPH) {
			extending = true;
			iris$isTerrain = false;
			injectNormalAndUV1 = true;
			return IrisVertexFormats.GLYPH;
		}

		return format;
	}

	// TODO: why is this needed?
	@Inject(method = "<init>", at = @At("TAIL"))
	private void ensureInit(ByteBufferBuilder byteBufferBuilder, VertexFormat.Mode mode, VertexFormat vertexFormat, CallbackInfo ci) {
		if (this.format == IrisVertexFormats.TERRAIN || this.format == IrisVertexFormats.ENTITY || this.format == IrisVertexFormats.GLYPH) {
			if (!this.extending) {
				throw new IllegalStateException("wtf (again)?");
			} else if (this.format == IrisVertexFormats.TERRAIN && !iris$isTerrain) {
				throw new IllegalStateException("wtf (again x2)");
			} else if (this.format != IrisVertexFormats.TERRAIN && iris$isTerrain) {
				throw new IllegalStateException("wtf (again x3)");
			}
		}
	}

	@Redirect(method = "addVertex(FFFIFFIIFFF)V", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;fastFormat:Z"))
	private boolean fastFormat(BufferBuilder instance) {
		return this.fastFormat && !extending;
	}

	@Inject(method = "endLastVertex", at = @At("HEAD"))
	private void iris$beforeNext(CallbackInfo ci) {
		if (this.vertices == 0) {
			return;
		}

		if (!extending) {
			if (this.format.contains(IrisVertexFormats.TANGENT_ELEMENT)) {
				throw new IllegalStateException("wtf? " + format);
			}
			return;
		}

		if (injectNormalAndUV1 && this.elementsToFill != (this.elementsToFill & ~VertexFormatElement.NORMAL.mask())) {
			this.setNormal(0, 0, 0);
		}

		if (iris$isTerrain) {
			long offset = this.beginElement(IrisVertexFormats.ENTITY_ELEMENT);
			if (offset > 0) {
				// ENTITY_ELEMENT
				MemoryUtil.memPutShort(offset, currentBlock);
				MemoryUtil.memPutShort(offset + 2, currentRenderType);
			}
		} else {
			long offset = this.beginElement(IrisVertexFormats.ENTITY_ID_ELEMENT);
			// ENTITY_ID_ELEMENT
			if (offset > 0) {

				MemoryUtil.memPutShort(offset, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
				MemoryUtil.memPutShort(offset + 2, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
				MemoryUtil.memPutShort(offset + 4, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
			}
		}

		// We can't fill these yet.
		this.elementsToFill = this.elementsToFill & ~IrisVertexFormats.MID_TEXTURE_ELEMENT.mask();
		this.elementsToFill = this.elementsToFill & ~IrisVertexFormats.TANGENT_ELEMENT.mask();

		if (iris$isTerrain) {
			// MID_BLOCK_ELEMENT
			int posIndex = this.offsetsByElement[VertexFormatElement.POSITION.id()];
			float x = MemoryUtil.memGetFloat(vertexPointer + posIndex);
			float y = MemoryUtil.memGetFloat(vertexPointer + posIndex + 4);
			float z = MemoryUtil.memGetFloat(vertexPointer + posIndex + 8);
			long midBlockOffset = this.beginElement(IrisVertexFormats.MID_BLOCK_ELEMENT);
			MemoryUtil.memPutInt(midBlockOffset, ExtendedDataHelper.computeMidBlock(x, y, z, currentLocalPosX, currentLocalPosY, currentLocalPosZ));
		}

		vertexCount++;

		if (mode == VertexFormat.Mode.QUADS && vertexCount == 4 || mode == VertexFormat.Mode.TRIANGLES && vertexCount == 3) {
			fillExtendedData(vertexCount);
		}
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

	@Unique
	private void fillExtendedData(int vertexAmount) {
		vertexCount = 0;

		int stride = format.getVertexSize();

		polygon.setup(vertexPointer + stride, stride, vertexAmount);

		float midU = 0;
		float midV = 0;

		for (int vertex = 0; vertex < vertexAmount; vertex++) {
			midU += polygon.u(vertex);
			midV += polygon.v(vertex);
		}

		midU /= vertexAmount;
		midV /= vertexAmount;

		int midTexOffset = this.offsetsByElement[IrisVertexFormats.MID_TEXTURE_ELEMENT.id()];
		int normalOffset = this.offsetsByElement[VertexFormatElement.NORMAL.id()];
		int tangentOffset = this.offsetsByElement[IrisVertexFormats.TANGENT_ELEMENT.id()];
		/*if (vertexAmount == 3) {
			// NormalHelper.computeFaceNormalTri(normal, polygon);	// Removed to enable smooth shaded triangles. Mods rendering triangles with bad normals need to recalculate their normals manually or otherwise shading might be inconsistent.

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				int packedNormal = buffer.getInt(nextElementByte - normalOffset - stride * vertex); // retrieve per-vertex normal

				int tangent = NormalHelper.computeTangentSmooth(NormI8.unpackX(packedNormal), NormI8.unpackY(packedNormal), NormI8.unpackZ(packedNormal), polygon);

				MemoryUtil.memPutFloat(nextElementByte - midUOffset - stride * vertex, midU);
				MemoryUtil.memPutFloat(nextElementByte - midVOffset - stride * vertex, midV);
				MemoryUtil.memPutInt(nextElementByte - tangentOffset - stride * vertex, tangent);
			}
		} else*/ {
			NormalHelper.computeFaceNormal(normal, polygon);
			int packedNormal = NormI8.pack(normal.x, normal.y, normal.z, 0.0f);
			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, polygon);

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				MemoryUtil.memPutFloat((vertexPointer - (stride * vertex)) + midTexOffset, midU);
				MemoryUtil.memPutFloat((vertexPointer - (stride * vertex)) + midTexOffset + 4, midV);
				MemoryUtil.memPutInt((vertexPointer - (stride * vertex)) + normalOffset, packedNormal);
				MemoryUtil.memPutInt((vertexPointer - (stride * vertex)) + tangentOffset, tangent);
			}
		}
	}
}
