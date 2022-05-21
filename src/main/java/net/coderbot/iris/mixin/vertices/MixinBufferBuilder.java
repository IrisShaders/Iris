package net.coderbot.iris.mixin.vertices;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.coderbot.iris.vertices.BufferBuilderPolygonView;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.coderbot.iris.vertices.NormalHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

/**
 * Dynamically and transparently extends the vanilla vertex formats with additional data
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements BufferVertexConsumer, BlockSensitiveBufferBuilder  {
	@Unique
	private boolean extending;

	@Unique
	private int vertexCount;

	@Unique
	private final BufferBuilderPolygonView polygon = new BufferBuilderPolygonView();

	@Unique
	private final Vector3f normal = new Vector3f();

	@Unique
	private int normalOffset;

	@Unique
	private short currentBlock;

	@Unique
	private short currentRenderType;

	@Shadow
	private boolean fastFormat;

	@Shadow
	private boolean fullFormat;

	@Shadow
	private ByteBuffer buffer;

	@Shadow
	private VertexFormat.Mode mode;

	@Shadow
	private VertexFormat format;

	@Shadow
	private int nextElementByte;

	@Shadow
	private @Nullable VertexFormatElement currentElement;

	@Shadow
	public abstract void putShort(int i, short s);

	@Inject(method = "begin", at = @At("HEAD"))
	private void iris$onBegin(VertexFormat.Mode drawMode, VertexFormat format, CallbackInfo ci) {
		boolean shouldExtend = BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat();
		extending = shouldExtend && (format == DefaultVertexFormat.BLOCK || format == IrisVertexFormats.TERRAIN || format == DefaultVertexFormat.NEW_ENTITY || format == IrisVertexFormats.ENTITY);
		vertexCount = 0;

		if (extending) {
			normalOffset = format.getElements().indexOf(DefaultVertexFormat.ELEMENT_NORMAL);
		}
	}

	@Inject(method = "begin", at = @At("RETURN"))
	private void iris$afterBegin(VertexFormat.Mode drawMode, VertexFormat format, CallbackInfo ci) {
		if (extending) {
			this.format = (format == DefaultVertexFormat.NEW_ENTITY || format == IrisVertexFormats.ENTITY) ? IrisVertexFormats.ENTITY : IrisVertexFormats.TERRAIN;
			this.currentElement = this.format.getElements().get(0);
		}
	}

	@Inject(method = "discard()V", at = @At("HEAD"))
	private void iris$onDiscard(CallbackInfo ci) {
		extending = false;
		vertexCount = 0;
	}

	@Inject(method = "switchFormat", at = @At("RETURN"))
	private void iris$preventHardcodedVertexWriting(VertexFormat format, CallbackInfo ci) {
		if (!extending) {
			return;
		}

		fastFormat = false;
		fullFormat = false;
	}

	@Inject(method = "endVertex", at = @At("HEAD"))
	private void iris$beforeNext(CallbackInfo ci) {
		if (!extending) {
			return;
		}

		this.putShort(0, currentBlock);
		this.putShort(2, currentRenderType);
		this.nextElement();
		this.putFloat(0, 0);
		this.putFloat(4, 0);
		this.nextElement();
		this.putInt(0, 0);
		this.nextElement();

		vertexCount++;

		if (mode == VertexFormat.Mode.QUADS && vertexCount == 4 || mode == VertexFormat.Mode.TRIANGLES && vertexCount == 3) {
			fillExtendedData(vertexCount);
		}
	}

	@Override
	public void beginBlock(short block, short renderType) {
		this.currentBlock = block;
		this.currentRenderType = renderType;
	}

	@Override
	public void endBlock() {
		this.currentBlock = -1;
		this.currentRenderType = -1;
	}

	@Unique
	private void fillExtendedData(int vertexAmount) {
		vertexCount = 0;

		// TODO: Keep this in sync with the extensions
		int extendedDataLength = (2 * 2) + (2 * 4) + (1 * 4);

		int stride = this.format.getVertexSize();

		polygon.setup(buffer, this.nextElementByte, stride, vertexAmount);

		float midU = 0;
		float midV = 0;

		for (int vertex = 0; vertex < vertexAmount; vertex++) {
			midU += polygon.u(vertex);
			midV += polygon.v(vertex);
		}

		midU /= vertexAmount;
		midV /= vertexAmount;

		if (vertexAmount == 3) {
			NormalHelper.computeFaceNormalTri(normal, polygon);
		} else {
			NormalHelper.computeFaceNormal(normal, polygon);
		}
		int packedNormal = NormalHelper.packNormal(normal, 0.0f);

		int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, polygon);

		for (int vertex = 0; vertex < vertexAmount; vertex++) {
			buffer.putFloat(nextElementByte - 12 - stride * vertex, midU);
			buffer.putFloat(nextElementByte - 8 - stride * vertex, midV);
			buffer.putInt(nextElementByte - 4 - extendedDataLength - stride * vertex, packedNormal);
			buffer.putInt(nextElementByte - 4 - stride * vertex, tangent);
		}
	}

	@Unique
	private void putInt(int i, int value) {
		this.buffer.putInt(this.nextElementByte + i, value);
	}
}
