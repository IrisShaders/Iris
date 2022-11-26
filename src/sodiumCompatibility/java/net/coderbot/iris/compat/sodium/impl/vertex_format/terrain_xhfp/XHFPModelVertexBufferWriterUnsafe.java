package net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterUnsafe;
import me.jellysquid.mods.sodium.client.render.chunk.format.ModelVertexSink;
import net.coderbot.iris.compat.sodium.impl.block_context.BlockContextHolder;
import net.coderbot.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.coderbot.iris.vertices.NormalHelper;
import org.lwjgl.system.MemoryUtil;

import static net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp.XHFPModelVertexType.STRIDE;

public class XHFPModelVertexBufferWriterUnsafe extends VertexBufferWriterUnsafe implements ModelVertexSink, ContextAwareVertexWriter {
	private final QuadViewTerrain.QuadViewTerrainUnsafe quad = new QuadViewTerrain.QuadViewTerrainUnsafe();
	private final Vector3f normal = new Vector3f();

	private BlockContextHolder contextHolder;

	private int vertexCount;
	private float uSum;
	private float vSum;

	public XHFPModelVertexBufferWriterUnsafe(VertexBufferView backingBuffer) {
		super(backingBuffer, IrisModelVertexFormats.MODEL_VERTEX_XHFP);
	}

	@Override
	public void copyQuadAndFlipNormal() {
		ensureCapacity(4);

		MemoryUtil.memCopy(this.writePointer - STRIDE * 4, this.writePointer, STRIDE * 4);

		// Now flip vertex normals
		int packedNormal = MemoryUtil.memGetInt(this.writePointer + 32);
		int inverted = NormalHelper.invertPackedNormal(packedNormal);

		MemoryUtil.memPutInt(this.writePointer + 32, inverted);
		MemoryUtil.memPutInt(this.writePointer + 32 + STRIDE, inverted);
		MemoryUtil.memPutInt(this.writePointer + 32 + STRIDE * 2, inverted);
		MemoryUtil.memPutInt(this.writePointer + 32 + STRIDE * 3, inverted);

		// We just wrote 4 vertices, advance by 4
		for (int i = 0; i < 4; i++) {
			this.advance();
		}

		// Ensure vertices are flushed
		this.flush();
	}

	@Override
	public void writeVertex(float posX, float posY, float posZ, int color, float u, float v, int light, int chunkId) {
		uSum += u;
		vSum += v;

		this.writeQuadInternal(posX, posY, posZ, color, u, v, light, contextHolder.blockId, contextHolder.renderType, chunkId, ExtendedDataHelper.computeMidBlock(posX, posY, posZ, contextHolder.localPosX, contextHolder.localPosY, contextHolder.localPosZ));
	}

	private void writeQuadInternal(float posX, float posY, float posZ, int color,
								   float u, float v, int light, short materialId, short renderType, int chunkId, int packedMidBlock) {
		long i = this.writePointer;

		vertexCount++;
		// NB: uSum and vSum must already be incremented outside of this function.

		MemoryUtil.memPutShort(i + 0, XHFPModelVertexType.encodePosition(posX));
		MemoryUtil.memPutShort(i + 2, XHFPModelVertexType.encodePosition(posY));
		MemoryUtil.memPutShort(i + 4, XHFPModelVertexType.encodePosition(posZ));
		MemoryUtil.memPutShort(i + 6, (short) chunkId);

		MemoryUtil.memPutInt(i + 8, color);

		MemoryUtil.memPutShort(i + 12, XHFPModelVertexType.encodeBlockTexture(u));
		MemoryUtil.memPutShort(i + 14, XHFPModelVertexType.encodeBlockTexture(v));

		MemoryUtil.memPutShort(i + 16, (short) (light & 0xFFFF));
		MemoryUtil.memPutShort(i + 18, (short) (light >> 16 & 0xFFFF));

		// NB: We don't set midTexCoord, normal, and tangent here, they will be filled in later.
		// block ID: We only set the first 2 values, any legacy shaders using z or w will get filled in based on the GLSL spec
		// https://www.khronos.org/opengl/wiki/Vertex_Specification#Vertex_format
		// TODO: can we pack this into one short?
		MemoryUtil.memPutShort(i + 36, materialId);
		MemoryUtil.memPutShort(i + 38, renderType);
		MemoryUtil.memPutInt(i + 40, packedMidBlock);

		if (vertexCount == 4) {
			vertexCount = 0;

			// FIXME
			// The following logic is incorrect because OpenGL denormalizes shorts by dividing by 65535. The atlas is
			// based on power-of-two values and so a normalization factor that is not a power of two causes the values
			// used in the shader to be off by enough to cause visual errors. These are most noticeable on 1.18 with POM
			// on block edges.
			//
			// The only reliable way that this can be fixed is to apply the same shader transformations to midTexCoord
			// as Sodium does to the regular texture coordinates - dividing them by the correct power-of-two value inside
			// of the shader instead of letting OpenGL value normalization do the division. However, this requires
			// fragile patching that is not yet possible.
			//
			// As a temporary solution, the normalized shorts have been replaced with regular floats, but this takes up
			// an extra 4 bytes per vertex.

			// NB: Be careful with the math here! A previous bug was caused by midU going negative as a short, which
			// was sign-extended into midTexCoord, causing midV to have garbage (likely NaN data). If you're touching
			// this code, be aware of that, and don't introduce those kinds of bugs!
			//
			// Also note that OpenGL takes shorts in the range of [0, 65535] and transforms them linearly to [0.0, 1.0],
			// so multiply by 65535, not 65536.
			//
			// TODO: Does this introduce precision issues? Do we need to fall back to floats here? This might break
			// with high resolution texture packs.
//			int midU = (int)(65535.0F * Math.min(uSum * 0.25f, 1.0f)) & 0xFFFF;
//			int midV = (int)(65535.0F * Math.min(vSum * 0.25f, 1.0f)) & 0xFFFF;
//			int midTexCoord = (midV << 16) | midU;

			uSum *= 0.25f;
			vSum *= 0.25f;

			MemoryUtil.memPutFloat(i + 20, uSum);
			MemoryUtil.memPutFloat(i + 20 - STRIDE, uSum);
			MemoryUtil.memPutFloat(i + 20 - STRIDE * 2, uSum);
			MemoryUtil.memPutFloat(i + 20 - STRIDE * 3, uSum);

			MemoryUtil.memPutFloat(i + 24, vSum);
			MemoryUtil.memPutFloat(i + 24 - STRIDE, vSum);
			MemoryUtil.memPutFloat(i + 24 - STRIDE * 2, vSum);
			MemoryUtil.memPutFloat(i + 24 - STRIDE * 3, vSum);

			uSum = 0;
			vSum = 0;

			// normal computation
			// Implementation based on the algorithm found here:
			// https://github.com/IrisShaders/ShaderDoc/blob/master/vertex-format-extensions.md#surface-normal-vector

			quad.setup(i, STRIDE);
			NormalHelper.computeFaceNormal(normal, quad);
			int packedNormal = NormalHelper.packNormal(normal, 0.0f);

			MemoryUtil.memPutInt(i + 32, packedNormal);
			MemoryUtil.memPutInt(i + 32 - STRIDE, packedNormal);
			MemoryUtil.memPutInt(i + 32 - STRIDE * 2, packedNormal);
			MemoryUtil.memPutInt(i + 32 - STRIDE * 3, packedNormal);

			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, quad);

			MemoryUtil.memPutInt(i + 28, tangent);
			MemoryUtil.memPutInt(i + 28 - STRIDE, tangent);
			MemoryUtil.memPutInt(i + 28 - STRIDE * 2, tangent);
			MemoryUtil.memPutInt(i + 28 - STRIDE * 3, tangent);
		}

		this.advance();
	}

	@Override
	public void iris$setContextHolder(BlockContextHolder holder) {
		this.contextHolder = holder;
	}
}
