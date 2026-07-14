package net.irisshaders.iris.mixin.vertices;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.BufferBuilderPolygonView;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;
import net.irisshaders.iris.vertices.MojangBufferAccessor;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

/**
 * Dynamically and transparently extends the vanilla vertex formats with additional data
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements VertexConsumer, BlockSensitiveBufferBuilder {
	@Unique
	private static final int IRIS$UNKNOWN_OFFSET = -1;
	@Unique
	private static final int IRIS$NORMAL_SEMANTIC_ID = 5;
	@Unique
	private static final int IRIS$NORMAL_MASK = 1 << IRIS$NORMAL_SEMANTIC_ID;
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
	private PrimitiveTopology primitiveTopology;
	@Shadow
	@Final
	private VertexFormat format;
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
	private byte currentBlockEmission = -1;
	@Unique
	private int currentLocalPosX;
	@Unique
	private int currentLocalPosY;
	@Unique
	private int currentLocalPosZ;
	@Unique
	private int positionOffset = IRIS$UNKNOWN_OFFSET;
	@Unique
	private int uvOffset = IRIS$UNKNOWN_OFFSET;
	@Unique
	private int normalOffset = IRIS$UNKNOWN_OFFSET;
	@Unique
	private int midTexOffset = IRIS$UNKNOWN_OFFSET;
	@Unique
	private int tangentOffset = IRIS$UNKNOWN_OFFSET;
	@Unique
	private int midBlockOffset = IRIS$UNKNOWN_OFFSET;
	@Unique
	private int entityOffset = IRIS$UNKNOWN_OFFSET;
	@Unique
	private int entityIdOffset = IRIS$UNKNOWN_OFFSET;
	@Shadow
	@Final
	private ByteBufferBuilder buffer;

	@Shadow
	public abstract VertexConsumer setNormal(float f, float g, float h);

	@ModifyVariable(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/vertex/VertexFormat;contains(Ljava/lang/String;)Z"), argsOnly = true)
	private VertexFormat iris$extendFormat(VertexFormat format) {
		injectNormalAndUV1 = false;

		if (ImmediateState.skipExtension.get() || !ImmediateState.isRenderingLevel || !Iris.isPackInUseQuick()) {
			return format;
		}

		if (format.equals(DefaultVertexFormat.BLOCK) || format.equals(IrisVertexFormats.TERRAIN)) {
			extending = true;
			return IrisVertexFormats.TERRAIN;
		} else if (format.equals(DefaultVertexFormat.ENTITY) || format.equals(IrisVertexFormats.ENTITY)) {
			extending = true;
			return IrisVertexFormats.ENTITY;
		} else if (format.equals(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR) || format.equals(IrisVertexFormats.GLYPH)) {
			extending = true;
			injectNormalAndUV1 = true;
			return IrisVertexFormats.GLYPH;
		}

		return format;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void iris$cacheOffsets(ByteBufferBuilder buffer, PrimitiveTopology primitiveTopology, VertexFormat format, CallbackInfo ci) {
		if (!extending) {
			return;
		}

		positionOffset = IrisVertexFormats.getOffset(this.format, DefaultVertexFormat.POSITION_SEMANTIC_NAME);
		uvOffset = IrisVertexFormats.getOffset(this.format, DefaultVertexFormat.UV0_SEMANTIC_NAME);
		normalOffset = IrisVertexFormats.getOffset(this.format, DefaultVertexFormat.NORMAL_SEMANTIC_NAME);
		midTexOffset = IrisVertexFormats.getOffset(this.format, IrisVertexFormats.MID_TEXTURE_ATTRIBUTE);
		tangentOffset = IrisVertexFormats.getOffset(this.format, IrisVertexFormats.TANGENT_ATTRIBUTE);
		midBlockOffset = IrisVertexFormats.getOffset(this.format, IrisVertexFormats.MID_BLOCK_ATTRIBUTE);
		entityOffset = IrisVertexFormats.getOffset(this.format, IrisVertexFormats.ENTITY_ATTRIBUTE);
		entityIdOffset = IrisVertexFormats.getOffset(this.format, IrisVertexFormats.ENTITY_ID_ATTRIBUTE);
	}

	@Inject(method = "addVertex(FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;", at = @At("RETURN"))
	private void iris$fillPerVertexData(float x, float y, float z, CallbackInfoReturnable<VertexConsumer> cir) {
		if (!extending || this.vertexPointer == -1L) {
			return;
		}

		if (midBlockOffset != IRIS$UNKNOWN_OFFSET) {
			long offset = this.vertexPointer + midBlockOffset;
			MemoryUtil.memPutInt(offset, ExtendedDataHelper.computeMidBlock(x, y, z, currentLocalPosX, currentLocalPosY, currentLocalPosZ));
			MemoryUtil.memPutByte(offset + 3, currentBlockEmission);
		}

		if (entityOffset != IRIS$UNKNOWN_OFFSET) {
			long offset = this.vertexPointer + entityOffset;
			MemoryUtil.memPutShort(offset, (short) currentBlock);
			MemoryUtil.memPutShort(offset + 2, currentRenderType);
		}

		if (entityIdOffset != IRIS$UNKNOWN_OFFSET) {
			long offset = this.vertexPointer + entityIdOffset;
			MemoryUtil.memPutShort(offset, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
			MemoryUtil.memPutShort(offset + 2, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
			MemoryUtil.memPutShort(offset + 4, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
			MemoryUtil.memPutShort(offset + 6, (short) 0);
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

		if (injectNormalAndUV1 && (this.elementsToFill & IRIS$NORMAL_MASK) != 0) {
			this.setNormal(0, 1, 0);
		}

		if (skipEndVertexOnce) {
			skipEndVertexOnce = false;
			return;
		}

		int vertexAmount = iris$vertexAmountForExtendedData();
		if (vertexAmount == 0) {
			return;
		}

		vertexOffsets[iris$vertexCount] = vertexPointer - ((MojangBufferAccessor) buffer).getPointer();
		iris$vertexCount++;

		if (iris$vertexCount == vertexAmount) {
			fillExtendedData(vertexAmount);
		}
	}

	@Override
	public void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {
		this.currentBlock = block;
		this.currentRenderType = renderType;
		this.currentBlockEmission = blockEmission;
		this.currentLocalPosX = localPosX;
		this.currentLocalPosY = localPosY;
		this.currentLocalPosZ = localPosZ;
	}

	@Override
	public void endBlock() {
		this.currentBlock = -1;
		this.currentRenderType = -1;
		this.currentBlockEmission = -1;
		this.currentLocalPosX = 0;
		this.currentLocalPosY = 0;
		this.currentLocalPosZ = 0;
	}

	@Unique
	private int iris$vertexAmountForExtendedData() {
		if (this.primitiveTopology == PrimitiveTopology.QUADS) {
			return 4;
		} else if (this.primitiveTopology == PrimitiveTopology.TRIANGLES) {
			return 3;
		}

		return 0;
	}

	@Unique
	private void fillExtendedData(int vertexAmount) {
		iris$vertexCount = 0;

		if (positionOffset == IRIS$UNKNOWN_OFFSET || uvOffset == IRIS$UNKNOWN_OFFSET || midTexOffset == IRIS$UNKNOWN_OFFSET
			|| normalOffset == IRIS$UNKNOWN_OFFSET || tangentOffset == IRIS$UNKNOWN_OFFSET) {
			Arrays.fill(vertexOffsets, 0);
			return;
		}

		long basePointer = ((MojangBufferAccessor) buffer).getPointer();
		polygon.setup(basePointer, vertexOffsets, positionOffset, uvOffset);

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
				long newPointer = basePointer + vertexOffsets[vertex];
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
				long newPointer = basePointer + vertexOffsets[vertex];

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
