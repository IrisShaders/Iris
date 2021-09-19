package net.coderbot.iris.vertices;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.opengl.GL11C;

public class ExtendedBufferBuilder extends BufferBuilder {
	private VertexFormat originalFormat;
	private VertexFormat extendedFormat;
	private int vertexCount;

	public ExtendedBufferBuilder(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public void begin(VertexFormat.Mode drawMode, VertexFormat format) {
		this.originalFormat = format;
		this.vertexCount = 0;

		if (drawMode != VertexFormat.Mode.QUADS) {
			throw new IllegalArgumentException("Unexpected draw mode: " + drawMode);
		}

		if (this.originalFormat == DefaultVertexFormat.BLOCK) {
			this.extendedFormat = IrisVertexFormats.TERRAIN;
		} else {
			throw new IllegalArgumentException("Couldn't extend vertex format: " + this.originalFormat);
		}

		super.begin(drawMode, this.extendedFormat);
	}

	@Override
	public void discard() {
		super.discard();

		this.originalFormat = null;
		this.extendedFormat = null;
		this.vertexCount = 0;
	}

	@Override
	public void endVertex() {
		this.putShort(0, (short) -1);
		this.putShort(2, (short) -1);
		this.putShort(4, (short) -1);
		this.putShort(6, (short) -1);
		this.nextElement();
		this.putFloat(0, 0F);
		this.putFloat(4, 0F);
		this.nextElement();
		this.putFloat(0, 1F);
		this.putFloat(4, 0F);
		this.putFloat(8, 0F);
		this.putFloat(12, 1F);
		this.nextElement();

		super.endVertex();

		vertexCount += 1;

		if (vertexCount == 4) {
			vertexCount = 0;
			extendVertexData();
		}
	}

	private void extendVertexData() {
		// TODO: Use captured data to compute the tangent and miduv properties
		// TODO: Also compute correct vertex normals
	}
}
