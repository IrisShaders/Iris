package net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterUnsafe;
import me.jellysquid.mods.sodium.client.render.chunk.format.ModelVertexSink;
import net.coderbot.iris.block_rendering.MaterialIdHolder;
import net.coderbot.iris.compat.sodium.impl.block_id.MaterialIdAwareVertexWriter;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.compat.sodium.impl.vertex_format.NormalHelper;
import net.coderbot.iris.vendored.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class XHFPModelVertexBufferWriterUnsafe extends VertexBufferWriterUnsafe implements ModelVertexSink, MaterialIdAwareVertexWriter {
    private MaterialIdHolder idHolder;

    public XHFPModelVertexBufferWriterUnsafe(VertexBufferView backingBuffer) {
        super(backingBuffer, IrisModelVertexFormats.MODEL_VERTEX_XHFP);
    }

    private static final int STRIDE = 36;

    int vertexCount = 0;
    float uSum;
    float vSum;

    private final QuadViewTerrain.QuadViewTerrainUnsafe currentQuad = new QuadViewTerrain.QuadViewTerrainUnsafe();
    private final Vector3f normal = new Vector3f();

	@Override
	public void copyQuadAndFlipNormal() {
		ensureCapacity(4);

		MemoryUtil.memCopy(this.writePointer - STRIDE * 4, this.writePointer, STRIDE * 4);

		// Now flip vertex normals
		int packedNormal = MemoryUtil.memGetInt(this.writePointer + 28);
		int inverted = NormalHelper.invertPackedNormal(packedNormal);

		MemoryUtil.memPutInt(this.writePointer + 28, inverted);
		MemoryUtil.memPutInt(this.writePointer + 28 + STRIDE, inverted);
		MemoryUtil.memPutInt(this.writePointer + 28 + STRIDE * 2, inverted);
		MemoryUtil.memPutInt(this.writePointer + 28 + STRIDE * 3, inverted);

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

		short materialId = idHolder.id;
		short renderType = idHolder.renderType;

		this.writeQuadInternal(posX, posY, posZ, color, u, v, light, materialId, renderType, chunkId);
	}

	private void writeQuadInternal(float posX, float posY, float posZ, int color,
								   float u, float v, int light, short materialId, short renderType, int chunkId) {
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

		MemoryUtil.memPutInt(i + 16, XHFPModelVertexType.encodeLightMapTexCoord(light));

		// NB: We don't set midTexCoord, normal, and tangent here, they will be filled in later.
		// block ID: We only set the first 2 values, any legacy shaders using z or w will get filled in based on the GLSL spec
		// https://www.khronos.org/opengl/wiki/Vertex_Specification#Vertex_format
		// TODO: can we pack this into one short?
		MemoryUtil.memPutShort(i + 32, materialId);
		MemoryUtil.memPutShort(i + 34, renderType);

		if (vertexCount == 4) {
			// TODO: Consider applying similar vertex coordinate transformations as the normal HFP texture coordinates

			// NB: Be careful with the math here! A previous bug was caused by midU going negative as a short, which
			// was sign-extended into midTexCoord, causing midV to have garbage (likely NaN data). If you're touching
			// this code, be aware of that, and don't introduce those kinds of bugs!
			//
			// Also note that OpenGL takes shorts in the range of [0, 65535] and transforms them linearly to [0.0, 1.0],
			// so multiply by 65535, not 65536.
			//
			// TODO: Does this introduce precision issues? Do we need to fall back to floats here? This might break
			// with high resolution texture packs.
			int midU = (int)(65535.0F * Math.min(uSum * 0.25f, 1.0f)) & 0xFFFF;
			int midV = (int)(65535.0F * Math.min(vSum * 0.25f, 1.0f)) & 0xFFFF;
			int midTexCoord = (midV << 16) | midU;

			MemoryUtil.memPutInt(i + 20, midTexCoord);
			MemoryUtil.memPutInt(i + 20 - STRIDE, midTexCoord);
			MemoryUtil.memPutInt(i + 20 - STRIDE * 2, midTexCoord);
			MemoryUtil.memPutInt(i + 20 - STRIDE * 3, midTexCoord);

			vertexCount = 0;
			uSum = 0;
			vSum = 0;

			// normal computation
			// Implementation based on the algorithm found here:
			// https://github.com/IrisShaders/ShaderDoc/blob/master/vertex-format-extensions.md#surface-normal-vector

			currentQuad.setup(writePointer, XHFPModelVertexType.STRIDE);
			NormalHelper.computeFaceNormal(normal, currentQuad);
			int packedNormal = NormalHelper.packNormal(normal, 0.0f);

			MemoryUtil.memPutInt(i + 28, packedNormal);
			MemoryUtil.memPutInt(i + 28 - STRIDE, packedNormal);
			MemoryUtil.memPutInt(i + 28 - STRIDE * 2, packedNormal);
			MemoryUtil.memPutInt(i + 28 - STRIDE * 3, packedNormal);

			int tangent = currentQuad.computeTangent(normal.x(), normal.y(), normal.z());

			MemoryUtil.memPutInt(i + 24, tangent);
			MemoryUtil.memPutInt(i + 24 - STRIDE, tangent);
			MemoryUtil.memPutInt(i + 24 - STRIDE * 2, tangent);
			MemoryUtil.memPutInt(i + 24 - STRIDE * 3, tangent);
		}

		this.advance();
	}

    @Override
    public void iris$setIdHolder(MaterialIdHolder holder) {
        this.idHolder = holder;
    }
}
