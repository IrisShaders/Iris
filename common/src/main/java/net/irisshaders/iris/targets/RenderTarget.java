package net.irisshaders.iris.targets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelFormat;
import net.irisshaders.iris.gl.texture.PixelType;
import net.irisshaders.iris.vulkan.VulkanTextureOps;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class RenderTarget {
	private static final Map<GpuTexture, Views> rgbTextures = new IdentityHashMap<>();
	public static final int USAGE_STORAGE = 1 << 20;
	private boolean storage;

	public void enableStorage() {
		requireValid();

		if (storage) {
			return;
		}

		storage = true;

		try {
			resizeGpuTextures(width, height);
		} catch (RuntimeException | Error failure) {
			storage = false;

			throw failure;
		}
	}

	private final InternalTextureFormat internalFormat;
	private final PixelFormat format;
	private final PixelType type;
	private final GpuDevice gpuDevice;
	private GpuTexture mainGpuTexture, altGpuTexture;
	private GpuTextureView mainView, altView;
	private GpuTextureView mainMipView, altMipView;
	private int width;
	private int height;
	private boolean isValid;
	private String name;
	private boolean allowsLinear;
	private boolean mipmapsOnAlt;
	private boolean mipmapsOnMain;

	public RenderTarget(Builder builder) {
		this.isValid = true;

		this.name = builder.name;
		this.internalFormat = builder.internalFormat;
		this.format = builder.format;
		this.type = builder.type;

		this.width = builder.width;
		this.height = builder.height;

		GpuDevice device = builder.device != null ? builder.device : RenderSystem.getDevice();

		if (!device.getDeviceInfo().backendName().equalsIgnoreCase("Vulkan")) {
			throw new UnsupportedOperationException("Iris requires Vulkan");
		}

		this.gpuDevice = device;
		this.allowsLinear = !internalFormat.getPixelFormat().isInteger();
		resizeGpuTextures(width, height);
	}

	private void resizeGpuTextures(int width, int height) {
		int levels = 32 - Integer.numberOfLeadingZeros(Math.max(width, height));
		int usage = (storage ? USAGE_STORAGE : 0) | GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING |
			GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST;
		GpuTexture newMain = null, newAlt = null;
		GpuTextureView newMainView = null, newAltView = null, newMainMips = null, newAltMips = null;

		try {
			newMain = gpuDevice.createTexture(name + " main", usage, internalFormat.toGpuFormat(), width, height, 1, levels);
			newAlt = gpuDevice.createTexture(name + " alt", usage, internalFormat.toGpuFormat(), width, height, 1, levels);

			if (internalFormat.getPixelFormat() == PixelFormat.RGB || internalFormat.getPixelFormat() == PixelFormat.RGB_INTEGER) {
				registerRgb(newMain);
				registerRgb(newAlt);
			}

			newMainView = gpuDevice.createTextureView(newMain, 0, 1);
			newAltView = gpuDevice.createTextureView(newAlt, 0, 1);
			newMainMips = gpuDevice.createTextureView(newMain);
			newAltMips = gpuDevice.createTextureView(newAlt);
		} catch (RuntimeException | Error exception) {
			if (newAltMips != null) {
				newAltMips.close();
			}

			if (newMainMips != null) {
				newMainMips.close();
			}

			if (newAltView != null) {
				newAltView.close();
			}

			if (newMainView != null) {
				newMainView.close();
			}

			if (newAlt != null) {
				releaseRgbViews(newAlt);
				newAlt.close();
			}

			if (newMain != null) {
				releaseRgbViews(newMain);
				newMain.close();
			}

			throw new IllegalStateException(
				"Could not allocate Iris target " + name + " (" + internalFormat + " as " + internalFormat.toGpuFormat() + ", " + width +
					"x" + height + ", " + levels + " mips)", exception);
		}

		closeGpuTextures();
		mainGpuTexture = newMain;
		altGpuTexture = newAlt;
		mainView = newMainView;
		altView = newAltView;
		mainMipView = newMainMips;
		altMipView = newAltMips;
		mipmapsOnMain = mipmapsOnAlt = false;
	}

	private void closeGpuTextures() {
		if (mainView == null) {
			return;
		}

		releaseRgbViews(mainGpuTexture);
		releaseRgbViews(altGpuTexture);
		mainMipView.close();
		altMipView.close();
		mainView.close();
		altView.close();
		mainGpuTexture.close();
		altGpuTexture.close();
		mainMipView = altMipView = mainView = altView = null;
		mainGpuTexture = altGpuTexture = null;
	}

	public GpuTextureView getTextureView(boolean alt, boolean mipmapped) {
		requireValid();

		return alt ? (mipmapped ? altMipView : altView) : (mipmapped ? mainMipView : mainView);
	}

	public boolean allowsLinearFiltering() {
		return allowsLinear;
	}

	public boolean hasMipmaps(boolean alt) {
		return alt ? mipmapsOnAlt : mipmapsOnMain;
	}

	public static Builder builder() {
		return new Builder();
	}

	void resize(Vector2i textureScaleOverride) {
		this.resize(textureScaleOverride.x, textureScaleOverride.y);
	}

	// Package private, call CompositeRenderTargets#resizeIfNeeded instead.
	void resize(int width, int height) {
		requireValid();

		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("Invalid render target dimensions");
		}

		resizeGpuTextures(width, height);
		this.width = width;
		this.height = height;
	}

	public InternalTextureFormat getInternalFormat() {
		return internalFormat;
	}

	public int getMainTexture() {
		requireValid();

		throw new IllegalStateException("Vulkan render targets do not have OpenGL texture names");
	}

	public int getAltTexture() {
		requireValid();

		throw new IllegalStateException("Vulkan render targets do not have OpenGL texture names");
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void destroy() {
		requireValid();
		isValid = false;

		closeGpuTextures();
	}

	private void requireValid() {
		if (!isValid) {
			throw new IllegalStateException("Attempted to use a deleted composite render target");
		}
	}

	public GlSampler getAltSampler() {
		if (mipmapsOnAlt) {
			return allowsLinear ? GlSampler.MIPPED_LINEAR : GlSampler.MIPPED_NEAREST;
		}

		return allowsLinear ? GlSampler.LINEAR : GlSampler.NEAREST;
	}

	public GlSampler getMainSampler() {
		if (mipmapsOnMain) {
			return allowsLinear ? GlSampler.MIPPED_LINEAR : GlSampler.MIPPED_NEAREST;
		}

		return allowsLinear ? GlSampler.LINEAR : GlSampler.NEAREST;
	}

	public void generateMipmaps(boolean alt) {
		requireValid();
		VulkanTextureOps.generateMipmaps(gpuDevice, alt ? altGpuTexture : mainGpuTexture, allowsLinear);
		turnOnMips(alt);
	}

	public void turnOnMips(boolean alt) {
		if (alt) {
			this.mipmapsOnAlt = true;
		} else {
			this.mipmapsOnMain = true;
		}
	}

	public void turnOffMips(boolean alt) {
		if (alt) {
			this.mipmapsOnAlt = false;
		} else {
			this.mipmapsOnMain = false;
		}
	}

	public static class Builder {
		private InternalTextureFormat internalFormat = InternalTextureFormat.RGBA8;
		private int width = 0;
		private int height = 0;
		private PixelFormat format = PixelFormat.RGBA;
		private PixelType type = PixelType.UNSIGNED_BYTE;
		private String name = null;
		private GpuDevice device;

		public Builder setDevice(GpuDevice device) {
			this.device = Objects.requireNonNull(device);

			return this;
		}

		private Builder() {
			// No-op
		}

		public Builder setName(String name) {
			this.name = name;

			return this;
		}

		public Builder setInternalFormat(InternalTextureFormat format) {
			this.internalFormat = format;

			return this;
		}

		public Builder setDimensions(int width, int height) {
			if (width <= 0) {
				throw new IllegalArgumentException("Width must be greater than zero");
			}

			if (height <= 0) {
				throw new IllegalArgumentException("Height must be greater than zero");
			}

			this.width = width;
			this.height = height;

			return this;
		}

		public Builder setPixelFormat(PixelFormat pixelFormat) {
			this.format = pixelFormat;

			return this;
		}

		public Builder setPixelType(PixelType pixelType) {
			this.type = pixelType;

			return this;
		}

		public RenderTarget build() {
			return new RenderTarget(this);
		}
	}


	private static void registerRgb(GpuTexture texture) {
		rgbTextures.put(texture, new Views());
	}

	public static long sampledView(VulkanDevice device, VulkanGpuTextureView source) {
		var views = rgbTextures.get(source.texture());

		if (views == null) {
			return source.vkImageView();
		}

		views.device = device;

		return views.handles.computeIfAbsent(source, view -> {
			try (var stack = MemoryStack.stackPush()) {
				var info = VkImageViewCreateInfo.calloc(stack).sType$Default().image(view.texture().vkImage())
					.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D).format(VulkanConst.toVk(view.texture().getFormat()));
				info.components().a(VK10.VK_COMPONENT_SWIZZLE_ONE);
				info.subresourceRange().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).baseMipLevel(view.baseMipLevel())
					.levelCount(view.mipLevels()).baseArrayLayer(0).layerCount(1);

				var handle = stack.mallocLong(1);
				int result = VK10.vkCreateImageView(device.vkDevice(), info, null, handle);

				if (result != VK10.VK_SUCCESS) {
					throw new IllegalStateException("Could not create RGB sampling view: " + result);
				}

				return handle.get(0);
			}
		});
	}

	private static void releaseRgbViews(GpuTexture texture) {
		var views = rgbTextures.remove(texture);

		if (views == null || views.handles.isEmpty()) {
			return;
		}

		var encoder = views.device.createCommandEncoder();

		for (long handle : views.handles.values()) {
			encoder.queueForDestroy(() -> VK10.vkDestroyImageView(views.device.vkDevice(), handle, null));
		}
	}

	private static final class Views {
		final Map<VulkanGpuTextureView, Long> handles = new IdentityHashMap<>();
		VulkanDevice device;
	}
}
