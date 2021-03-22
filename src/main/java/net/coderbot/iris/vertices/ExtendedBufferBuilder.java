package net.coderbot.iris.vertices;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL11C;

public class ExtendedBufferBuilder extends BufferBuilder {
	private VertexFormat originalFormat;
	private VertexFormat extendedFormat;
	private int vertexCount;

	public ExtendedBufferBuilder(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public void begin(int drawMode, VertexFormat format) {
		this.originalFormat = format;
		this.vertexCount = 0;

		if (drawMode != GL11C.GL_QUADS) {
			throw new IllegalArgumentException("Unexpected draw mode: " + drawMode);
		}

		if (this.originalFormat == VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL) {
			this.extendedFormat = IrisVertexFormats.TERRAIN;
		} else {
			throw new IllegalArgumentException("Couldn't extend vertex format: " + this.originalFormat);
		}

		super.begin(drawMode, this.extendedFormat);
	}

	@Override
	public void nextElement() {
		super.nextElement();

		while (this.getCurrentElement() == IrisVertexFormats.TANGENT_ELEMENT ||
				this.getCurrentElement() == IrisVertexFormats.ENTITY_ELEMENT ||
				this.getCurrentElement() == IrisVertexFormats.MID_TEXTURE_ELEMENT) {
			super.nextElement();
		}
	}

	@Override
	public void reset() {
		super.reset();

		this.originalFormat = null;
		this.extendedFormat = null;
		this.vertexCount = 0;
	}

	@Override
	public void next() {
		super.next();

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
