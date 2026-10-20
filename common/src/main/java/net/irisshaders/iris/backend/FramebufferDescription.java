package net.irisshaders.iris.backend;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import org.joml.Vector2i;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// Owns an eight-slot format array; slots after colorAttachmentCount are unused.
public record FramebufferDescription(GpuFormat[] colorFormats, int colorAttachmentCount, GpuFormat depthFormat) {
	public static final int MAX_COLOR_ATTACHMENTS = 8;

	public FramebufferDescription(List<GpuTextureView> colors, GpuFormat depthFormat) {
		this(formats(colors), colors.size(), depthFormat);
	}

	public FramebufferDescription {
		for (int i = 0; i < colorAttachmentCount; i++) {
			if (!colorFormats[i].hasColorAspect()) {
				throw new IllegalArgumentException("invalid framebuffer formats");
			}
		}

		if (depthFormat != null && !depthFormat.hasDepthAspect()) {
			throw new IllegalArgumentException("invalid framebuffer formats");
		}
	}

	private static GpuFormat[] formats(List<GpuTextureView> colors) {
		var formats = new GpuFormat[MAX_COLOR_ATTACHMENTS];
		for (int i = 0; i < colors.size(); i++) {
			formats[i] = colors.get(i).texture().getFormat();
		}
		return formats;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof FramebufferDescription(GpuFormat[] formats, int attachmentCount,
		                                                GpuFormat format) && this.colorAttachmentCount == attachmentCount
			&& this.depthFormat == format && Arrays.equals(this.colorFormats, formats);
	}

	@Override
	public int hashCode() {
		return 31 * (31 * Arrays.hashCode(this.colorFormats) + this.colorAttachmentCount) + Objects.hashCode(this.depthFormat);
	}

	private static void validate(GpuTextureView view, GpuFormat format, Vector2i viewport) {
		var texture = view.texture();
		if (view.isClosed() || texture.isClosed() || texture.getFormat() != format || view.mipLevels() != 1 || (texture
			.usage() & GpuTexture.USAGE_RENDER_ATTACHMENT) == 0 || view.getWidth(0) < viewport.x || view.getHeight(0) < viewport.y) {
			throw new IllegalArgumentException("invalid framebuffer attachment '" + texture.getLabel() + "': expected format=" + format
				+ ", actual format=" + texture.getFormat() + ", view closed=" + view.isClosed() + ", texture closed=" + texture.isClosed()
				+ ", mip levels=" + view.mipLevels() + ", usage=" + texture.usage() + ", size=" + view.getWidth(0) + "x"
				+ view.getHeight(0) + ", viewport=" + viewport.x + "x" + viewport.y);
		}
	}

	public void validate(List<GpuTextureView> colors, GpuTextureView depth, Vector2i viewport) {
		if (colors.size() != this.colorAttachmentCount || (depth == null) == (this.depthFormat != null) || viewport.x <= 0 || viewport.y <= 0) {
			throw new IllegalArgumentException("framebuffer attachments differ from description");
		}

		for (int i = 0; i < colors.size(); i++) {
			validate(colors.get(i), this.colorFormats[i], viewport);
		}

		if (depth != null) {
			validate(depth, this.depthFormat, viewport);
		}
	}
}
