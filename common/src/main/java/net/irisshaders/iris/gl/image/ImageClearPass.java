package net.irisshaders.iris.gl.image;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import org.lwjgl.opengl.GL30C;

public abstract class ImageClearPass {
	protected final GlFramebuffer framebuffer;

	private ImageClearPass(GlImage image) {
		this.framebuffer = new GlFramebuffer();
		this.framebuffer.addColorAttachment(0, image.getId());
	}

	public static ImageClearPass create(GlImage image) {
		return switch (image.getInternalFormat().getShaderDataType()) {
			case FLOAT -> new Float(image);
			case INT -> new Int(image);
			case UINT -> new UInt(image);
		};
	}

	public abstract void execute();

	public void destroy() {
		framebuffer.destroy();
	}

	private static class Float extends ImageClearPass {
		public Float(GlImage image) {
			super(image);
		}

		@Override
		public void execute() {
			IrisRenderSystem.clearBufferfv(this.framebuffer.getId(), GL30C.GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f});
		}
	}

	private static class Int extends ImageClearPass {
		public Int(GlImage image) {
			super(image);
		}

		@Override
		public void execute() {
			IrisRenderSystem.clearBufferiv(this.framebuffer.getId(), GL30C.GL_COLOR, 0, new int[]{0, 0, 0, 0});
		}
	}

	private static class UInt extends ImageClearPass {
		public UInt(GlImage image) {
			super(image);
		}

		@Override
		public void execute() {
			IrisRenderSystem.clearBufferuiv(this.framebuffer.getId(), GL30C.GL_COLOR, 0, new int[]{0, 0, 0, 0});
		}
	}
}
