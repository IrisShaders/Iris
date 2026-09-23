package net.irisshaders.iris.targets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.Iris;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class RenderFramebuffer {
	private final Map<Integer, Supplier<GpuTextureView>> attachments = new HashMap<>();
	private int[] drawBuffers = {0};
	private GpuTextureView depth;
	private boolean closed;

	private void checkValid() {
		if (this.closed) {
			throw new IllegalStateException("framebuffer closed");
		}
	}

	public void addColorAttachment(int index, RenderTarget target, boolean alt) {
		this.checkValid();
		this.attachments.put(index, () -> target.getTextureView(alt, false));
	}

	public void addDepthAttachment(GpuTexture texture) {
		this.checkValid();
		var next = RenderSystem.getDevice().createTextureView(texture, 0, 1);

		if (this.depth != null) {
			this.depth.close();
		}

		this.depth = next;
	}

	public List<GpuTextureView> getColorViews() {
		this.checkValid();

		return Arrays.stream(this.drawBuffers)
			.mapToObj(i -> Objects.requireNonNull(this.attachments.get(i), "missing attachment " + i).get()).toList();
	}

	public GpuTextureView getDepthView() {
		this.checkValid();

		return this.depth;
	}

	public boolean hasDepthAttachment() {
		return this.depth != null;
	}

	public void setDrawBuffers(int[] buffers) {
		this.checkValid();
		int maximum = RenderSystem.getDevice().getDeviceInfo().limits().maxColorAttachments();

		if (buffers.length > maximum) {
			throw new IllegalArgumentException("too many framebuffer attachments");
		}

		for (int index : buffers) {
			if (index < 0 || index >= maximum) {
				throw new IllegalArgumentException("invalid attachment " + index);
			}
		}

		this.drawBuffers = buffers.clone();
	}

	public void disableDrawBuffers() {
		this.checkValid();
		this.drawBuffers = new int[0];
	}

	public void clear(Vector4f color, int flags) {
		this.checkValid();

		if ((flags & GL11C.GL_COLOR_BUFFER_BIT) != 0) {
			Iris.getRenderBackend().commands().clearColors(this.getColorViews(), color);
		}

		if ((flags & GL11C.GL_DEPTH_BUFFER_BIT) != 0 && this.depth != null) {
			RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(this.depth.texture(), 0);
		}

		if ((flags & GL11C.GL_STENCIL_BUFFER_BIT) != 0) {
			throw new UnsupportedOperationException("stencil clear is not implemented");
		}
	}

	public void validateAttachments() {
		this.checkValid();
		var views = new ArrayList<>(this.getColorViews());

		if (this.depth != null) {
			views.add(this.depth);
		}

		if (views.isEmpty()) {
			throw new IllegalStateException("no framebuffer attachments");
		}

		var first = views.getFirst();

		for (var view : views) {
			if (view.isClosed() || view.texture().isClosed() || view.getWidth(0) != first.getWidth(0) || view.getHeight(
				0) != first.getHeight(0)) {
				throw new IllegalStateException("invalid framebuffer attachments");
			}
		}
	}

	public void destroy() {
		if (this.closed) {
			return;
		}

		this.closed = true;

		if (this.depth != null) {
			this.depth.close();
		}

		this.depth = null;
		this.attachments.clear();
	}
}
