package net.irisshaders.iris.backend;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import org.joml.Vector2i;

import java.util.List;

public record FramebufferDescription(List<GpuFormat> colorFormats, GpuFormat depthFormat) {
	public FramebufferDescription {
		colorFormats = List.copyOf(colorFormats);

		if (colorFormats.stream().anyMatch(format -> !format.hasColorAspect()) || depthFormat != null && !depthFormat.hasDepthAspect()) {
			throw new IllegalArgumentException("invalid framebuffer formats");
		}
	}

	private static void validate(GpuTextureView view, GpuFormat format, Vector2i viewport) {
		if (view.isClosed() || view.texture().isClosed() || view.texture().getFormat() != format || view.mipLevels() != 1 || (view.texture()
			.usage() & GpuTexture.USAGE_RENDER_ATTACHMENT) == 0 || view.getWidth(0) < viewport.x || view.getHeight(0) < viewport.y) {
			throw new IllegalArgumentException("invalid framebuffer attachment");
		}
	}

	public void validate(List<GpuTextureView> colors, GpuTextureView depth, Vector2i viewport) {
		if (colors.size() != this.colorFormats.size() || (depth == null) == (this.depthFormat != null) || viewport.x <= 0 || viewport.y <= 0) {
			throw new IllegalArgumentException("framebuffer attachments differ from description");
		}

		for (int i = 0; i < colors.size(); i++) {
			validate(colors.get(i), this.colorFormats.get(i), viewport);
		}

		if (depth != null) {
			validate(depth, this.depthFormat, viewport);
		}
	}
}
