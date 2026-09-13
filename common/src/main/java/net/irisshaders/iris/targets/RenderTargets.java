package net.irisshaders.iris.targets;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class RenderTargets {
	private final RenderTarget[] targets;
	private final GpuDevice device;
	private GpuTexture noTranslucents;
	private GpuTexture noHand;
	private final GlFramebuffer depthSourceFb;
	private final GlFramebuffer noTranslucentsDestFb;
	private final GlFramebuffer noHandDestFb;
	private final List<GlFramebuffer> ownedFramebuffers;
	private final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> targetSettingsMap;
	private final PackDirectives packDirectives;
	private GpuTexture currentDepthTexture;
	private DepthBufferFormat currentDepthFormat;
	private int cachedWidth;
	private int cachedHeight;
	private boolean fullClearRequired;

	private int cachedDepthBufferVersion;
	private boolean destroyed;

	public RenderTargets(int width, int height, GpuTexture depthTexture, int depthBufferVersion, DepthBufferFormat depthFormat,
		Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargets, PackDirectives packDirectives) {
		this(RenderSystem.getDevice(), width, height, depthTexture, depthBufferVersion, depthFormat, renderTargets, packDirectives);
	}

	public RenderTargets(GpuDevice device, int width, int height, GpuTexture depthTexture, int depthBufferVersion,
		DepthBufferFormat depthFormat, Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargets,
		PackDirectives packDirectives) {
		this.device = device;

		if (!device.getDeviceInfo().backendName().equalsIgnoreCase("Vulkan")) {
			throw new UnsupportedOperationException("Iris requires Vulkan");
		}

		targets = new RenderTarget[renderTargets.size()];

		targetSettingsMap = renderTargets;
		this.packDirectives = packDirectives;

		this.currentDepthTexture = depthTexture;
		this.currentDepthFormat = depthFormat;

		this.cachedWidth = width;
		this.cachedHeight = height;
		this.cachedDepthBufferVersion = depthBufferVersion;

		this.ownedFramebuffers = new ArrayList<>();

		fullClearRequired = true;

		this.depthSourceFb = createFramebufferWritingToMain(new int[]{0});

		GpuFormat mojangDepthFormat = depthFormat.toMojang();
		this.noTranslucents = device.createTexture("Depth / Opaque", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
			mojangDepthFormat, width, height, 1, 1);
		this.noHand = device.createTexture("Depth / Before Hand", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
			mojangDepthFormat, width, height, 1, 1);

		this.noTranslucentsDestFb = createFramebufferWritingToMain(new int[]{0});
		this.noTranslucentsDestFb.addDepthAttachment(this.noTranslucents);

		this.noHandDestFb = createFramebufferWritingToMain(new int[]{0});
		this.noHandDestFb.addDepthAttachment(this.noHand);
	}

	public void destroy() {
		destroyed = true;

		for (GlFramebuffer owned : ownedFramebuffers) {
			owned.destroy();
		}

		for (RenderTarget target : targets) {
			if (target != null) {
				target.destroy();
			}
		}

		noTranslucents.close();
		noHand.close();
	}

	public GpuTextureView getDepthTextureView(int index) {
		return switch (index) {
			case 0 -> depthSourceFb.depthView();
			case 1 -> noTranslucentsDestFb.depthView();
			case 2 -> noHandDestFb.depthView();
			default -> throw new IllegalArgumentException("Invalid depth texture: " + index);
		};
	}

	public int maxDrawBuffers() {
		return device.getDeviceInfo().limits().maxColorAttachments();
	}

	public int getRenderTargetCount() {
		return targets.length;
	}

	public RenderTarget get(int index) {
		if (destroyed) {
			throw new IllegalStateException("Tried to use destroyed RenderTargets");
		}

		if (targets[index] == null) {
			return null;
		}

		return targets[index];
	}

	public RenderTarget getOrCreate(int index) {
		if (destroyed) {
			throw new IllegalStateException("Tried to use destroyed RenderTargets");
		}

		if (targets[index] != null) {
			return targets[index];
		}

		create(index);

		return targets[index];
	}

	private void create(int index) {
		PackRenderTargetDirectives.RenderTargetSettings settings = targetSettingsMap.get(index);
		Vector2i dimensions = packDirectives.getTextureScaleOverride(index, cachedWidth, cachedHeight);
		targets[index] = RenderTarget.builder()
			.setDevice(device)
			.setDimensions(dimensions.x, dimensions.y)
			.setName("colortex" + index)
			.setInternalFormat(settings.getInternalFormat())
			.setPixelFormat(settings.getInternalFormat().getPixelFormat())
			.build();
	}

	public GpuTexture getDepthTexture() {
		return currentDepthTexture;
	}

	public GpuTexture getDepthTextureNoTranslucents() {
		if (destroyed) {
			throw new IllegalStateException("Tried to use destroyed RenderTargets");
		}

		return noTranslucents;
	}

	public GpuTexture getDepthTextureNoHand() {
		return noHand;
	}

	public boolean resizeIfNeeded(int newDepthBufferVersion, GpuTexture newDepthTextureId, int newWidth, int newHeight,
		DepthBufferFormat newDepthFormat, PackDirectives packDirectives) {
		boolean recreateDepth = false;

		if (cachedDepthBufferVersion != newDepthBufferVersion) {
			recreateDepth = true;
			currentDepthTexture = newDepthTextureId;
			cachedDepthBufferVersion = newDepthBufferVersion;
		}

		boolean sizeChanged = newWidth != cachedWidth || newHeight != cachedHeight;
		boolean depthFormatChanged = newDepthFormat != currentDepthFormat;

		if (depthFormatChanged) {
			currentDepthFormat = newDepthFormat;
		}

		if (depthFormatChanged || sizeChanged) {

			noTranslucents.close();
			noHand.close();

			this.noTranslucents = device.createTexture("Depth / Opaque", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
				newDepthTextureId.getFormat(), newWidth, newHeight, 1, 1);
			this.noHand = device.createTexture("Depth / Before Hand", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
				newDepthTextureId.getFormat(), newWidth, newHeight, 1, 1);

			this.noTranslucentsDestFb.addDepthAttachment(this.noTranslucents);
			this.noHandDestFb.addDepthAttachment(this.noHand);

			recreateDepth = true;
		}

		if (recreateDepth) {

			for (GlFramebuffer framebuffer : ownedFramebuffers) {
				if (framebuffer.hasDepthAttachment() && framebuffer != noTranslucentsDestFb && framebuffer != noHandDestFb) {
					framebuffer.addDepthAttachment(newDepthTextureId);
				}
			}
		}

		if (sizeChanged) {
			cachedWidth = newWidth;
			cachedHeight = newHeight;

			for (int i = 0; i < targets.length; i++) {
				if (targets[i] != null) {
					targets[i].resize(packDirectives.getTextureScaleOverride(i, newWidth, newHeight));
				}
			}

			fullClearRequired = true;
		}

		return sizeChanged;
	}

	public void copyPreTranslucentDepth() {
		device.createCommandEncoder().copyTextureToTexture(currentDepthTexture, noTranslucents, 0, 0, 0, 0, 0, cachedWidth, cachedHeight);
	}

	public void copyPreHandDepth() {
		device.createCommandEncoder().copyTextureToTexture(currentDepthTexture, noHand, 0, 0, 0, 0, 0, cachedWidth, cachedHeight);
	}

	public boolean isFullClearRequired() {
		return fullClearRequired;
	}

	public void onFullClear() {
		fullClearRequired = false;
	}

	public GlFramebuffer createFramebufferWritingToMain(int[] drawBuffers) {
		return createFullFramebuffer(false, drawBuffers);
	}

	public GlFramebuffer createFramebufferWritingToAlt(int[] drawBuffers) {
		return createFullFramebuffer(true, drawBuffers);
	}

	public GlFramebuffer createClearFramebuffer(boolean alt, int[] clearBuffers) {
		ImmutableSet<Integer> stageWritesToMain = ImmutableSet.of();

		if (!alt) {
			stageWritesToMain = invert(ImmutableSet.of(), clearBuffers);
		}

		return createColorFramebuffer(stageWritesToMain, clearBuffers);
	}

	private ImmutableSet<Integer> invert(ImmutableSet<Integer> base, int[] relevant) {
		ImmutableSet.Builder<Integer> inverted = ImmutableSet.builder();

		for (int i : relevant) {
			if (!base.contains(i)) {
				inverted.add(i);
			}
		}

		return inverted.build();
	}

	private GlFramebuffer createEmptyFramebuffer() {
		GlFramebuffer framebuffer = new GlFramebuffer(device);
		ownedFramebuffers.add(framebuffer);

		framebuffer.addDepthAttachment(currentDepthTexture);

		framebuffer.addColorAttachment(0, getOrCreate(0), false);
		framebuffer.noDrawBuffers();

		return framebuffer;
	}

	public GlFramebuffer createDHFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		return createColorFramebuffer(stageWritesToMain, drawBuffers);
	}

	public GlFramebuffer createMergedFramebuffer(ColorAttachmentPlan plan, boolean withDepth) {
		if (plan.targets().size() > maxDrawBuffers()) {
			throw new IllegalArgumentException("Too many merged color attachments");
		}

		GlFramebuffer framebuffer = new GlFramebuffer(device);

		try {
			for (int i = 0; i < plan.targets().size(); i++) {
				var target = plan.targets().get(i);
				framebuffer.addColorAttachment(i, getOrCreate(target.index()), target.alt());
			}

			framebuffer.drawBuffers(IntStream.range(0, plan.targets().size()).toArray());

			if (withDepth) {
				framebuffer.addDepthAttachment(currentDepthTexture);
			}

			framebuffer.validateAttachments();
			ownedFramebuffers.add(framebuffer);

			return framebuffer;
		} catch (RuntimeException | Error exception) {
			framebuffer.destroy();

			throw exception;
		}
	}

	public GlFramebuffer createGbufferFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers);
		framebuffer.addDepthAttachment(currentDepthTexture);

		return framebuffer;
	}

	private GlFramebuffer createFullFramebuffer(boolean clearsAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = ImmutableSet.of();

		if (!clearsAlt) {
			stageWritesToMain = invert(ImmutableSet.of(), drawBuffers);
		}

		return createColorFramebufferWithDepth(stageWritesToMain, drawBuffers);
	}

	public GlFramebuffer createColorFramebufferWithDepth(ImmutableSet<Integer> stageWritesToMain, int[] drawBuffers) {
		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers);
		framebuffer.addDepthAttachment(currentDepthTexture);

		return framebuffer;
	}

	public GlFramebuffer createColorFramebuffer(ImmutableSet<Integer> stageWritesToMain, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			throw new IllegalArgumentException("Framebuffer must have at least one color buffer");
		}

		GlFramebuffer framebuffer = new GlFramebuffer(device);
		ownedFramebuffers.add(framebuffer);

		int[] actualDrawBuffers = new int[drawBuffers.length];

		for (int i = 0; i < drawBuffers.length; i++) {
			actualDrawBuffers[i] = i;

			if (drawBuffers[i] >= getRenderTargetCount()) {

				framebuffer.destroy();
				ownedFramebuffers.remove(framebuffer);

				throw new IllegalStateException("Render target with index " + drawBuffers[i] + " is not supported, only " +
					getRenderTargetCount() + " render targets are supported.");
			}

			RenderTarget target = this.getOrCreate(drawBuffers[i]);
			framebuffer.addColorAttachment(i, target, !stageWritesToMain.contains(drawBuffers[i]));
		}

		framebuffer.drawBuffers(actualDrawBuffers);
		framebuffer.readBuffer(0);

		framebuffer.validateAttachments();

		return framebuffer;
	}

	public void destroyFramebuffer(GlFramebuffer framebuffer) {
		framebuffer.destroy();
		ownedFramebuffers.remove(framebuffer);
	}

	public int getCurrentWidth() {
		return cachedWidth;
	}

	public int getCurrentHeight() {
		return cachedHeight;
	}

	public void createIfUnsure(int index) {
		if (targets[index] == null) {
			create(index);
		}
	}
}
