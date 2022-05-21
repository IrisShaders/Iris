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
import net.coderbot.iris.vertices.ExtendingBufferBuilder;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.coderbot.iris.vertices.NormalHelper;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
public abstract class MixinBufferBuilder implements BufferVertexConsumer, BlockSensitiveBufferBuilder, ExtendingBufferBuilder {
	@Unique
	private boolean extending;

	@Unique
	private int vertexCount;

	@Unique
	private final BufferBuilderPolygonView polygon = new BufferBuilderPolygonView();

	@Unique
	private final Vector3f normal = new Vector3f();

	@Unique
	private boolean injectOverlayAndNormal;

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
	private int mode;

	@Shadow
	private VertexFormat format;

	@Shadow
	private int nextElementByte;

	@Shadow
	private @Nullable VertexFormatElement currentElement;

	@Shadow
	public abstract void begin(int drawMode, VertexFormat vertexFormat);

	@Shadow
	public abstract void putShort(int i, short s);

	@Unique
	private boolean iris$shouldNotExtend = false;

	@Override
	public void iris$beginWithoutExtending(int drawMode, VertexFormat vertexFormat) {
		iris$shouldNotExtend = true;
		begin(drawMode, vertexFormat);
		iris$shouldNotExtend = false;
	}

	@Inject(method = "begin", at = @At("HEAD"))
	private void iris$onBegin(int drawMode, VertexFormat format, CallbackInfo ci) {
		boolean shouldExtend = (!iris$shouldNotExtend) && BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat();
		extending = shouldExtend && (format == DefaultVertexFormat.BLOCK || format == DefaultVertexFormat.NEW_ENTITY
			|| format == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
		vertexCount = 0;

		if (extending) {
			injectOverlayAndNormal = format == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP;
		}
	}

	@Inject(method = "begin", at = @At("RETURN"))
	private void iris$afterBegin(int drawMode, VertexFormat format, CallbackInfo ci) {
		if (extending) {
			this.format = (format == DefaultVertexFormat.BLOCK)
				? IrisVertexFormats.TERRAIN
				: IrisVertexFormats.ENTITY;
			this.currentElement = this.format.getElements().get(0);
		}
	}

	@Inject(method = "discard", at = @At("HEAD"))
	private void iris$onReset(CallbackInfo ci) {
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

	@Inject(method = "nextElement", at = @At("RETURN"))
	private void iris$beforeNextElement(CallbackInfo ci) {
		if (injectOverlayAndNormal && currentElement == DefaultVertexFormat.ELEMENT_UV1) {
			this.putInt(0, OverlayTexture.NO_OVERLAY);
			nextElement();
		}
	}

	@Inject(method = "endVertex", at = @At("HEAD"))
	private void iris$beforeNext(CallbackInfo ci) {
		if (!extending) {
			return;
		}

		if (injectOverlayAndNormal) {
			this.putInt(0, 0);
			this.nextElement();
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

		if (mode == GL11.GL_QUADS && vertexCount == 4 || mode == GL11.GL_TRIANGLES && vertexCount == 3) {
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
