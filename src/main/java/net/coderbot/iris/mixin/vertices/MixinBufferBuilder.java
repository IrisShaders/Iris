package net.coderbot.iris.mixin.vertices;

import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.coderbot.iris.vertices.NormalHelper;
import net.coderbot.iris.vertices.QuadView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.nio.ByteBuffer;

/**
 * Dynamically and transparently extends the vanilla vertex formats with additional data
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements BufferVertexConsumer, BlockSensitiveBufferBuilder  {
	@Unique
	boolean extending;

	@Unique
	private int vertexCount;

	@Unique
	private QuadView quad = new QuadView();

	@Unique
	private Vector3f normal = new Vector3f();

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
	private VertexFormat format;

	@Shadow
	private int nextElementByte;

	@Shadow
	private @Nullable VertexFormatElement currentElement;

	@Inject(method = "begin", at = @At("HEAD"))
	private void iris$onBegin(int drawMode, VertexFormat format, CallbackInfo ci) {
		extending = format == DefaultVertexFormat.BLOCK || format == IrisVertexFormats.TERRAIN;
		vertexCount = 0;

		if (extending) {
			normalOffset = format.getElements().indexOf(DefaultVertexFormat.ELEMENT_NORMAL);
		}
	}

	@Inject(method = "begin", at = @At("RETURN"))
	private void iris$afterBegin(int drawMode, VertexFormat format, CallbackInfo ci) {
		if (extending) {
			this.format = IrisVertexFormats.TERRAIN;
			this.currentElement = IrisVertexFormats.TERRAIN.getElements().get(0);
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

	@Inject(method = "endVertex", at = @At("HEAD"))
	private void iris$beforeNext(CallbackInfo ci) {
		if (!extending) {
			return;
		}

		this.putFloat(0, currentBlock);
		this.putFloat(4, currentRenderType);
		this.putFloat(8, (short) -1);
		this.putFloat(12, (short) -1);
		this.nextElement();
		this.putFloat(0, 0F);
		this.putFloat(4, 0F);
		this.nextElement();
		this.putFloat(0, 1F);
		this.putFloat(4, 0F);
		this.putFloat(8, 0F);
		this.putFloat(12, 1F);
		this.nextElement();

		vertexCount += 1;

		// TODO: This assumes quads
		if (vertexCount < 4) {
			return;
		}

		vertexCount = 0;

		// TODO: Keep this in sync with the extensions
		int extendedDataLength = (4 * 4) + (2 * 4) + (4 * 4);

		int stride = this.format.getVertexSize();

		quad.setup(this.buffer, this.nextElementByte, stride);
		NormalHelper.computeFaceNormal(this.normal, quad);
		int packedNormal = NormalHelper.packNormal(this.normal, 0);

		buffer.putInt(this.nextElementByte - 4 - extendedDataLength, packedNormal);
		buffer.putInt(this.nextElementByte - 4 - extendedDataLength - stride, packedNormal);
		buffer.putInt(this.nextElementByte - 4 - extendedDataLength - stride * 2, packedNormal);
		buffer.putInt(this.nextElementByte - 4 - extendedDataLength - stride * 3, packedNormal);

		computeTangents();

		float midU = 0;
		float midV = 0;

		for (int vertex = 0; vertex < 4; vertex++) {
			midU += quad.u(vertex);
			midV += quad.v(vertex);
		}

		midU *= 0.25;
		midV *= 0.25;

		for (int vertex = 0; vertex < 4; vertex++) {
			buffer.putFloat(this.nextElementByte - 24 - stride * vertex, midU);
			buffer.putFloat(this.nextElementByte - 20 - stride * vertex, midV);
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

	private void computeTangents() {
		// Capture all of the relevant vertex positions
		float x0 = quad.x(0);
		float y0 = quad.y(0);
		float z0 = quad.z(0);

		float x1 = quad.x(1);
		float y1 = quad.y(1);
		float z1 = quad.z(1);

		float x2 = quad.x(2);
		float y2 = quad.y(2);
		float z2 = quad.z(2);

		float edge1x = x1 - x0;
		float edge1y = y1 - y0;
		float edge1z = z1 - z0;

		float edge2x = x2 - x0;
		float edge2y = y2 - y0;
		float edge2z = z2 - z0;

		float u0 = quad.u(0);
		float v0 = quad.v(0);

		float u1 = quad.u(1);
		float v1 = quad.v(1);

		float u2 = quad.u(2);
		float v2 = quad.v(2);

		float deltaU1 = u1 - u0;
		float deltaV1 = v1 - v0;
		float deltaU2 = u2 - u0;
		float deltaV2 = v2 - v0;

		float fdenom = deltaU1 * deltaV2 - deltaU2 * deltaV1;
		float f;

		if (fdenom == 0.0) {
			f = 1.0f;
		} else {
			f = 1.0f / fdenom;
		}

		float tangentx = f * (deltaV2 * edge1x - deltaV1 * edge2x);
		float tangenty = f * (deltaV2 * edge1y - deltaV1 * edge2y);
		float tangentz = f * (deltaV2 * edge1z - deltaV1 * edge2z);
		float tcoeff = rsqrt(tangentx * tangentx + tangenty * tangenty + tangentz * tangentz);
		tangentx *= tcoeff;
		tangenty *= tcoeff;
		tangentz *= tcoeff;

		float bitangentx = f * (-deltaU2 * edge1x + deltaU1 * edge2x);
		float bitangenty = f * (-deltaU2 * edge1y + deltaU1 * edge2y);
		float bitangentz = f * (-deltaU2 * edge1z + deltaU1 * edge2z);
		float bitcoeff = rsqrt(bitangentx * bitangentx + bitangenty * bitangenty + bitangentz * bitangentz);
		bitangentx *= bitcoeff;
		bitangenty *= bitcoeff;
		bitangentz *= bitcoeff;

		// predicted bitangent = tangent × normal
		// Compute the determinant of the following matrix to get the cross product
		//  i  j  k
		// tx ty tz
		// nx ny nz

		float pbitangentx =   tangenty * normal.z - tangentz * normal.y;
		float pbitangenty = -(tangentx * normal.z - tangentz * normal.x);
		float pbitangentz =   tangentx * normal.x - tangenty * normal.y;

		float dot = (bitangentx * pbitangentx) + (bitangenty * pbitangenty) + (bitangentz * pbitangentz);
		float tangentW;

		if (dot < 0) {
			tangentW = -1.0F;
		} else {
			tangentW = 1.0F;
		}

		int tangent = NormalHelper.packNormal(tangentx, tangenty, tangentz, tangentW);

		int stride = this.format.getVertexSize();

		// TODO: Use packed tangents in the vertex format
		/*buffer.putInt(this.elementOffset - 16, tangent);
		buffer.putInt(this.elementOffset - 16 - stride, tangent);
		buffer.putInt(this.elementOffset - 16 - stride * 2, tangent);
		buffer.putInt(this.elementOffset - 16 - stride * 3, tangent);*/

		for (int vertex = 0; vertex < 4; vertex++) {
			buffer.putFloat(this.nextElementByte - 16 - stride * vertex, tangentx);
			buffer.putFloat(this.nextElementByte - 12 - stride * vertex, tangenty);
			buffer.putFloat(this.nextElementByte - 8 - stride * vertex, tangentz);
			buffer.putFloat(this.nextElementByte - 4 - stride * vertex, 1.0F);
		}
	}

	@Unique
	private static float rsqrt(float value) {
		if (value == 0.0f) {
			// You heard it here first, folks: 1 divided by 0 equals 1
			// In actuality, this is a workaround for normalizing a zero length vector (leaving it as zero length)
			return 1.0f;
		} else {
			return (float) (1.0 / Math.sqrt(value));
		}
	}
}
