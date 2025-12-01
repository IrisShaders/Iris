package net.irisshaders.iris.mixin.vertices;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.BufferBuilderPolygonView;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.MojangBufferAccessor;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

/**
 * Dynamically and transparently extends the vanilla vertex formats with additional data
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements VertexConsumer, BlockSensitiveBufferBuilder {
	@Unique
	private final BufferBuilderPolygonView polygon = new BufferBuilderPolygonView();
	@Unique
	private final Vector3f normal = new Vector3f();
	@Unique
	private final long[] vertexOffsets = new long[4];
	@Shadow
	private int elementsToFill;
	@Unique
	private boolean skipEndVertexOnce;
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
	private boolean extending;
	@Unique
	private boolean injectNormalAndUV1;
	@Unique
	private int iris$vertexCount;
	@Unique
	private int currentBlock = -1;
	@Unique
	private byte currentRenderType = -1;
	@Unique
	private int currentLocalPosX;
	@Unique
	private int currentLocalPosY;
	@Unique
	private int currentLocalPosZ;
	@Shadow
	@Final
	private ByteBufferBuilder buffer;

	@Shadow
	public abstract VertexConsumer setNormal(float f, float g, float h);

	@Shadow
	protected abstract long beginElement(VertexFormatElement vertexFormatElement);

	@ModifyVariable(method = "<init>", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/vertex/VertexFormatElement;POSITION:Lcom/mojang/blaze3d/vertex/VertexFormatElement;", ordinal = 0), argsOnly = true)
	private VertexFormat iris$extendFormat(VertexFormat format) {
		boolean iris$isTerrain = false;
		injectNormalAndUV1 = false;

		if (ImmediateState.skipExtension.get() || !ImmediateState.isRenderingLevel || !Iris.isPackInUseQuick()) {
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

	@Redirect(method = "addVertex(FFFIFFIIFFF)V", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;fastFormat:Z"))
	private boolean fastFormat(BufferBuilder instance) {
		return this.fastFormat && !extending;
	}

	@Inject(method = "addVertex(FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;", at = @At("RETURN"))
	private void injectMidBlock(float x, float y, float z, CallbackInfoReturnable<VertexConsumer> cir) {
		if ((this.elementsToFill & IrisVertexFormats.MID_BLOCK_ELEMENT.mask()) != 0) {
			long midBlockOffset = this.beginElement(IrisVertexFormats.MID_BLOCK_ELEMENT);
			MemoryUtil.memPutInt(midBlockOffset, ExtendedDataHelper.computeMidBlock(x, y, z, currentLocalPosX, currentLocalPosY, currentLocalPosZ));
			byte currentBlockEmission = -1;
			MemoryUtil.memPutByte(midBlockOffset + 3, currentBlockEmission);
		}

		if ((this.elementsToFill & IrisVertexFormats.ENTITY_ELEMENT.mask()) != 0) {
			long offset = this.beginElement(IrisVertexFormats.ENTITY_ELEMENT);
			// ENTITY_ELEMENT
			MemoryUtil.memPutShort(offset, (short) currentBlock);
			MemoryUtil.memPutShort(offset + 2, currentRenderType);
		} else if ((this.elementsToFill & IrisVertexFormats.ENTITY_ID_ELEMENT.mask()) != 0) {
			long offset = this.beginElement(IrisVertexFormats.ENTITY_ID_ELEMENT);
			// ENTITY_ID_ELEMENT
			MemoryUtil.memPutShort(offset, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
			MemoryUtil.memPutShort(offset + 2, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
			MemoryUtil.memPutShort(offset + 4, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
		}
	}

	@Dynamic("Used to skip endLastVertex if the last push was made by Sodium")
	@Inject(method = "push", at = @At("TAIL"), remap = false, require = 0)
	private void iris$skipSodiumChange(CallbackInfo ci) {
		skipEndVertexOnce = true;
	}

	@Inject(method = "endLastVertex", at = @At("HEAD"))
	private void iris$beforeNext(CallbackInfo ci) {
		if (this.vertices == 0 || !extending) {
			return;
		}

		// We can't fill these yet.
		this.elementsToFill = this.elementsToFill & ~IrisVertexFormats.MID_TEXTURE_ELEMENT.mask();
		this.elementsToFill = this.elementsToFill & ~IrisVertexFormats.TANGENT_ELEMENT.mask();

		if (injectNormalAndUV1 && this.elementsToFill != (this.elementsToFill & ~VertexFormatElement.NORMAL.mask())) {
			this.setNormal(0, 1, 0);
		}

		if (skipEndVertexOnce) {
			skipEndVertexOnce = false;
			return;
		}

		if (mode != VertexFormat.Mode.QUADS && mode != VertexFormat.Mode.TRIANGLES) {
			return;
		}

		vertexOffsets[iris$vertexCount] = vertexPointer - ((MojangBufferAccessor) buffer).getPointer();

		iris$vertexCount++;

		if (mode == VertexFormat.Mode.QUADS && iris$vertexCount == 4 || mode == VertexFormat.Mode.TRIANGLES && iris$vertexCount == 3) {
			fillExtendedData(iris$vertexCount);
		}
	}

	@Override
	public void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {
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
		iris$vertexCount = 0;

		int stride = format.getVertexSize();

		polygon.setup(((MojangBufferAccessor) buffer).getPointer(), vertexOffsets, stride, vertexAmount);

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
		if (vertexAmount == 3) {
			// NormalHelper.computeFaceNormalTri(normal, polygon);	// Removed to enable smooth shaded triangles. Mods rendering triangles with bad normals need to recalculate their normals manually or otherwise shading might be inconsistent.

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				long newPointer = ((MojangBufferAccessor) buffer).getPointer() + vertexOffsets[vertex];
				int vertexNormal = MemoryUtil.memGetInt(newPointer + normalOffset); // retrieve per-vertex normal

				int tangent = NormalHelper.computeTangentSmooth(NormI8.unpackX(vertexNormal), NormI8.unpackY(vertexNormal), NormI8.unpackZ(vertexNormal), polygon);

				MemoryUtil.memPutFloat(newPointer + midTexOffset, midU);
				MemoryUtil.memPutFloat(newPointer + midTexOffset + 4, midV);
				MemoryUtil.memPutInt(newPointer + tangentOffset, tangent);
			}
		} else {
			// TODO: Temporary fix for EMI item batching
			boolean recalculateNormal = ImmediateState.isRenderingLevel;
			NormalHelper.computeFaceNormal(normal, polygon);
			int packedNormal = 0;
			if (recalculateNormal) {
				packedNormal = NormI8.pack(normal.x, normal.y, normal.z, 0.0f);
			}
			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, polygon);

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				long newPointer = ((MojangBufferAccessor) buffer).getPointer() + vertexOffsets[vertex];

				MemoryUtil.memPutFloat(newPointer + midTexOffset, midU);
				MemoryUtil.memPutFloat(newPointer + midTexOffset + 4, midV);
				if (recalculateNormal) {
					MemoryUtil.memPutInt(newPointer + normalOffset, packedNormal);
				}
				MemoryUtil.memPutInt(newPointer + tangentOffset, tangent);
			}
		}

		Arrays.fill(vertexOffsets, 0);
	}
}
