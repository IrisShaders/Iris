package net.irisshaders.iris.opengl;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.texture.TextureType;
import org.lwjgl.opengl.ARBClearTexture;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

final class GlImage implements IrisImage {
	private final int target;
	private final TextureType type;
	private final GpuFormat format;
	private final int width;
	private final int height;
	private final int depth;
	private int id;

	GlImage(TextureType type, GpuFormat format, int width, int height, int depth) {
		if (width < 1 || height < 0 || depth < 0) {
			throw new IllegalArgumentException("invalid image extent");
		}

		this.width = width;
		this.type = type;
		this.height = Math.max(1, height);
		this.depth = Math.max(1, depth);
		this.format = format;
		this.target = type.getGlType();
		this.id = GlStateManager._genTexture();
		GlShaderBindings.bindTexture(this.target, this.id);
		int internal = GlConst.toGlInternalId(format);
		int external = GlConst.toGlExternalId(format);
		int pixelType = GlConst.toGlType(format);

		switch (type) {
			case TEXTURE_1D -> IrisRenderSystem.texImage1D(this.id, this.target, 0, internal, width, 0, external, pixelType, null);
			case TEXTURE_2D, TEXTURE_RECTANGLE ->
				IrisRenderSystem.texImage2D(this.id, this.target, 0, internal, width, this.height, 0, external, pixelType, null);
			case TEXTURE_3D ->
				IrisRenderSystem.texImage3D(this.id, this.target, 0, internal, width, this.height, this.depth, 0, external, pixelType,
					null);
		}

		GlStateManager._texParameter(this.target, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		GlStateManager._texParameter(this.target, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		GlStateManager._texParameter(this.target, GL12C.GL_TEXTURE_MAX_LEVEL, 0);
		this.clear();
	}

	@Override
	public TextureType getType() {
		return this.type;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public int getDepth() {
		return this.depth;
	}

	@Override
	public long getView() {
		if (this.id == 0) {
			throw new IllegalStateException("image closed");
		}

		return this.id;
	}

	@Override
	public long getImage() {
		return this.getView();
	}

	@Override
	public void upload(ByteBuffer data) {
		GlShaderBindings.bindTexture(this.target, this.id);
		int alignment = GlStateManager._getInteger(GL11C.GL_UNPACK_ALIGNMENT);
		GlStateManager._pixelStore(GL11C.GL_UNPACK_ALIGNMENT, 1);

		try {
			int external = GlConst.toGlExternalId(this.format);
			int type = GlConst.toGlType(this.format);

			switch (this.target) {
				case GL11C.GL_TEXTURE_1D -> GL11C.glTexSubImage1D(this.target, 0, 0, this.width, external, type, data);
				case GL12C.GL_TEXTURE_3D ->
					GL12C.glTexSubImage3D(this.target, 0, 0, 0, 0, this.width, this.height, this.depth, external, type, data);
				default -> GlStateManager._texSubImage2D(this.target, 0, 0, 0, this.width, this.height, external, type, data);
			}
		} finally {
			GlStateManager._pixelStore(GL11C.GL_UNPACK_ALIGNMENT, alignment);
		}
	}

	@Override
	public void clear() {
		int external = GlConst.toGlExternalId(this.format);
		int pixelType = GlConst.toGlType(this.format);

		if (GL.getCapabilities().OpenGL44) {
			GL44C.glClearTexImage(this.id, 0, external, pixelType, (ByteBuffer) null);
			return;
		} else if (GL.getCapabilities().GL_ARB_clear_texture) {
			ARBClearTexture.glClearTexImage(this.id, 0, external, pixelType, (ByteBuffer) null);
			return;
		}

		var zeros = MemoryUtil.memCalloc(Math.multiplyExact(Math.multiplyExact(this.width, this.height), this.format.blockSize()));
		GlShaderBindings.bindTexture(this.target, this.id);
		int alignment = GlStateManager._getInteger(GL11C.GL_UNPACK_ALIGNMENT);
		GlStateManager._pixelStore(GL11C.GL_UNPACK_ALIGNMENT, 1);

		try {
			switch (this.type) {
				case TEXTURE_1D -> GL11C.glTexSubImage1D(this.target, 0, 0, this.width, external, pixelType, zeros);
				case TEXTURE_2D, TEXTURE_RECTANGLE ->
					GlStateManager._texSubImage2D(this.target, 0, 0, 0, this.width, this.height, external, pixelType, zeros);
				case TEXTURE_3D -> {
					for (int z = 0; z < this.depth; z++) {
						GL12C.glTexSubImage3D(this.target, 0, 0, 0, z, this.width, this.height, 1, external, pixelType, zeros);
					}
				}
			}
		} finally {
			GlStateManager._pixelStore(GL11C.GL_UNPACK_ALIGNMENT, alignment);
			MemoryUtil.memFree(zeros);
		}
	}

	@Override
	public void close() {
		if (this.id != 0) {
			GlStateManager._deleteTexture(this.id);
			this.id = 0;
		}
	}
}
