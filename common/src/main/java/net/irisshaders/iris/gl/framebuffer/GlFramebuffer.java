package net.irisshaders.iris.gl.framebuffer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.irisshaders.iris.gl.GlResource;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.vulkan.VulkanTextureOps;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class GlFramebuffer extends GlResource {
	private final Int2IntMap attachments;
	private final int maxDrawBuffers;
	private final int maxColorAttachments;
	private boolean hasDepthAttachment;
	private final GpuDevice vulkanDevice;
	private final Map<Integer, Supplier<GpuTextureView>> gpuAttachments = new HashMap<>();
	private GpuTextureView depthView;
	private int[] gpuDrawBuffers = new int[]{0};

	public GlFramebuffer() {
		this(RenderSystem.getDevice());
	}

	public GlFramebuffer(GpuDevice device) {
		super(isVulkan(device) ? 0 : IrisRenderSystem.createFramebuffer());
		vulkanDevice = isVulkan(device) ? device : null;

		this.attachments = new Int2IntArrayMap();
		this.maxDrawBuffers = vulkanDevice != null ? device.getDeviceInfo().limits().maxColorAttachments() :
			GlStateManager._getInteger(GL30C.GL_MAX_DRAW_BUFFERS);
		this.maxColorAttachments = vulkanDevice != null ? maxDrawBuffers : GlStateManager._getInteger(GL30C.GL_MAX_COLOR_ATTACHMENTS);
		this.hasDepthAttachment = false;
	}

	private static boolean isVulkan(GpuDevice device) {
		return device.getDeviceInfo().backendName().equalsIgnoreCase("Vulkan");
	}

	public boolean isVulkan() {
		return vulkanDevice != null;
	}

	public void addColorAttachment(int index, RenderTarget target, boolean alt) {
		assertValid();

		if (!isVulkan()) {
			addColorAttachment(index, alt ? target.getAltTexture() : target.getMainTexture());

			return;
		}

		gpuAttachments.put(index, () -> target.getTextureView(alt, false));
	}

	public List<GpuTextureView> colorViews() {
		assertValid();

		if (!isVulkan()) {
			throw new IllegalStateException("OpenGL framebuffer has no Vulkan views");
		}

		return Arrays.stream(gpuDrawBuffers).mapToObj(index -> {
			var supplier = gpuAttachments.get(index);

			if (supplier == null) {
				throw new IllegalStateException("Missing color attachment " + index);
			}

			return supplier.get();
		}).toList();
	}

	public GpuTextureView depthView() {
		assertValid();

		return depthView;
	}

	public void clear(Vector4f color, int flags) {
		assertValid();

		if (!isVulkan()) {
			throw new IllegalStateException("Use the OpenGL clear path for this framebuffer");
		}

		if ((flags & GL30C.GL_STENCIL_BUFFER_BIT) != 0) {
			throw new UnsupportedOperationException("Stencil clears are not implemented for Iris Vulkan framebuffers");
		}

		var encoder = vulkanDevice.createCommandEncoder();

		if ((flags & GL30C.GL_COLOR_BUFFER_BIT) != 0) {
			VulkanTextureOps.clearColors(vulkanDevice, colorViews(), color);
		}

		if ((flags & GL30C.GL_DEPTH_BUFFER_BIT) != 0 && depthView != null) {
			encoder.clearDepthTexture(depthView.texture(), 0.0);
		}
	}

	public void addDepthAttachment(GpuTexture texture) {
		assertValid();

		if (isVulkan()) {
			GpuTextureView replacement = vulkanDevice.createTextureView(texture, 0, 1);

			if (depthView != null) {
				depthView.close();
			}

			depthView = replacement;
			hasDepthAttachment = true;

			return;
		}

		int fb = getGlId();

		// TODO: NeoForge 1.21.5
		//if (texture.getFormat().hasStencilAspect()) {
		//	IrisRenderSystem.framebufferTexture2D(fb, GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_STENCIL_ATTACHMENT, GL30C.GL_TEXTURE_2D,
		//	texture, 0);
		//} else {
		IrisRenderSystem.framebufferTexture2D(fb, GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D,
			((GlTexture) texture).glId(), 0);
		//}

		this.hasDepthAttachment = true;
	}

	public void addDepthAttachmentBypass(int texture) {
		int fb = getGlId();
		IrisRenderSystem.framebufferTexture2D(fb, GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, texture, 0);

		this.hasDepthAttachment = true;
	}

	public void addColorAttachment(int index, int texture) {
		int fb = getGlId();
		IrisRenderSystem.framebufferTexture2D(fb, GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0 + index, GL30C.GL_TEXTURE_2D, texture,
			0);
		attachments.put(index, texture);
	}

	public void noDrawBuffers() {
		if (isVulkan()) {
			assertValid();
			gpuDrawBuffers = new int[0];

			return;
		}

		IrisRenderSystem.drawBuffers(getGlId(), new int[]{GL30C.GL_NONE});
	}

	public void drawBuffers(int[] buffers) {
		int[] glBuffers = new int[buffers.length];
		int index = 0;

		if (buffers.length > maxDrawBuffers) {
			throw new IllegalArgumentException("Cannot write to more than " + maxDrawBuffers + " draw buffers on this GPU");
		}

		for (int buffer : buffers) {
			if (buffer >= maxColorAttachments) {
				throw new IllegalArgumentException("Only " + maxColorAttachments +
					" color attachments are supported on this GPU, but an attempt was made to write to a color attachment with index " +
					buffer);
			}

			glBuffers[index++] = GL30C.GL_COLOR_ATTACHMENT0 + buffer;
		}

		if (isVulkan()) {
			assertValid();
			gpuDrawBuffers = buffers.clone();

			return;
		}

		IrisRenderSystem.drawBuffers(getGlId(), glBuffers);
	}

	public void readBuffer(int buffer) {
		if (isVulkan()) {
			assertValid();

			return;
		}

		IrisRenderSystem.readBuffer(getGlId(), GL30C.GL_COLOR_ATTACHMENT0 + buffer);
	}

	public int getColorAttachment(int index) {
		assertValid();

		if (isVulkan()) {
			throw new IllegalStateException("Vulkan attachments have views, not OpenGL texture names");
		}

		return attachments.get(index);
	}

	public boolean hasDepthAttachment() {
		return hasDepthAttachment;
	}

	public void bind() {
		GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, getGlId());
	}

	public void bindAsReadBuffer() {
		GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, getGlId());
	}

	public void bindAsDrawBuffer() {
		GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, getGlId());
	}

	protected void destroyInternal() {
		if (isVulkan()) {
			if (depthView != null) {
				depthView.close();
			}

			depthView = null;
			gpuAttachments.clear();

			return;
		}

		GlStateManager._glDeleteFramebuffers(getGlId());
	}

	public void validateAttachments() {
		assertValid();

		var views = new ArrayList<>(colorViews());

		if (depthView != null) {
			views.add(depthView);
		}

		if (views.isEmpty()) {
			throw new IllegalStateException("Framebuffer has no attachments");
		}

		var first = views.getFirst();

		for (var view : views) {
			if (view.isClosed() || view.texture().isClosed() || view.getWidth(0) != first.getWidth(0) ||
				view.getHeight(0) != first.getHeight(0)) {
				throw new IllegalStateException("Framebuffer attachments are closed or have mismatched dimensions");
			}
		}
	}

	public int getStatus() {
		if (isVulkan()) {
			var views = new ArrayList<>(colorViews());

			if (depthView != null) {
				views.add(depthView);
			}

			if (views.isEmpty()) {
				return GL30C.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
			}

			var first = views.getFirst();

			for (var view : views) {
				if (view.isClosed() || view.getWidth(0) != first.getWidth(0) || view.getHeight(0) != first.getHeight(0)) {
					return GL30C.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
				}
			}

			return GL30C.GL_FRAMEBUFFER_COMPLETE;
		}

		bind();

		return IrisRenderSystem.checkFramebufferStatus(GL30C.GL_FRAMEBUFFER);
	}

	@Override
	protected int getGlId() {
		if (isVulkan()) {
			throw new IllegalStateException("Vulkan attachments cannot be used as an OpenGL framebuffer");
		}

		return super.getGlId();
	}

	public int getId() {
		return getGlId();
	}
}
