package net.irisshaders.iris.shadows;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.properties.PackShadowDirectives;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.targets.RenderFramebuffer;
import net.irisshaders.iris.targets.RenderTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ShadowRenderTargets {
	private static final double LN_OF_2 = Math.log(2.0);
	private final RenderTarget[] targets;
	private final GpuTextureView[] depthViews;
	private final PackShadowDirectives shadowDirectives;
	private final GpuTexture mainDepth;
	private final GpuTexture noTranslucents;
	private final RenderFramebuffer depthSourceFb;
	private final RenderFramebuffer noTranslucentsDestFb;
	private final boolean[] flipped;
	private final List<RenderFramebuffer> ownedFramebuffers;
	private final int resolution;
	private final boolean[] hardwareFiltered;
	private final boolean[] mipped;
	private final boolean[] linearFiltered;
	private final InternalTextureFormat[] formats;
	private final IntList buffersToBeCleared;
	private final int size;
	private boolean fullClearRequired;

	public ShadowRenderTargets(WorldRenderingPipeline pipeline, int resolution, PackShadowDirectives shadowDirectives) {
		this(pipeline.hasFeature(FeatureFlags.HIGHER_SHADOWCOLOR), resolution, shadowDirectives);
	}

	public ShadowRenderTargets(boolean higherShadowColor, int resolution, PackShadowDirectives shadowDirectives) {
		this.shadowDirectives = shadowDirectives;
		this.size = higherShadowColor ? PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_IRIS : PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_OF;
		targets = new RenderTarget[size];
		formats = new InternalTextureFormat[size];
		flipped = new boolean[size];
		hardwareFiltered = new boolean[size];
		mipped = new boolean[size];
		linearFiltered = new boolean[size];
		buffersToBeCleared = new IntArrayList();


		this.ownedFramebuffers = new ArrayList<>();
		this.resolution = resolution;

		for (int i = 0; i < shadowDirectives.getDepthSamplingSettings().size(); i++) {
			this.hardwareFiltered[i] = shadowDirectives.getDepthSamplingSettings().get(i).getHardwareFiltering();
			this.mipped[i] = shadowDirectives.getDepthSamplingSettings().get(i).getMipmap();
			this.linearFiltered[i] = !shadowDirectives.getDepthSamplingSettings().get(i).getNearest();
		}

		this.mainDepth = RenderSystem.getDevice().createTexture("Shadow Map",
			GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
			GpuFormat.D32_FLOAT, resolution, resolution, 1, this.mipped[0] ? log2(resolution) + 1 : 1);
		this.noTranslucents = RenderSystem.getDevice().createTexture("Shadow Map / Opaque",
			GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, GpuFormat.D32_FLOAT,
			resolution, resolution, 1, this.mipped[1] ? log2(resolution) + 1 : 1);
		this.depthViews = new GpuTextureView[]{RenderSystem.getDevice().createTextureView(
			this.mainDepth), RenderSystem.getDevice().createTextureView(this.noTranslucents)};

		// NB: Make sure all buffers are cleared so that they don't contain undefined
		// data. Otherwise very weird things can happen.
		fullClearRequired = true;

		this.depthSourceFb = createFramebufferWritingToMain(new int[]{0});

		this.noTranslucentsDestFb = createFramebufferWritingToMain(new int[]{0});
		this.noTranslucentsDestFb.addDepthAttachment(this.noTranslucents);
	}

	public static int log2(int val) {
		return (int) Math.floor(Math.log(val) / LN_OF_2);
	}

	// TODO: Actually flip. This is required for shadow composites!
	public void flip(int target) {
		flipped[target] = !flipped[target];
	}

	public boolean isFlipped(int target) {
		return flipped[target];
	}

	public void destroy() {
		for (RenderFramebuffer owned : this.ownedFramebuffers) {
			owned.destroy();
		}

		for (RenderTarget target : targets) {
			if (target != null) {
				target.destroy();
			}
		}

		for (var view : this.depthViews) {
			view.close();
		}

		mainDepth.close();
		noTranslucents.close();
	}

	public int maxDrawBuffers() {
		return RenderSystem.getDevice().getDeviceInfo().limits().maxColorAttachments();
	}

	public GpuTextureView getDepthTextureView(int index) {
		return this.depthViews[index];
	}

	public int getRenderTargetCount() {
		return targets.length;
	}

	public int[] getDrawBuffers(int[] drawBuffers) {
		for (int buffer : drawBuffers) {
			if (buffer >= this.getRenderTargetCount()) {
				// Some packs rely on OptiFine ignoring invalid DRAWBUFFERS in shadow shaders.
				return new int[]{0, 1};
			}
		}

		return drawBuffers;
	}

	public RenderTarget get(int index) {
		return targets[index];
	}

	/**
	 * Gets the render target assigned to an index, and creates it if it does not exist.
	 * This is a <b>expensive</b> opetation nad may block other tasks! Use it sparingly, and use {@code get()} if possible.
	 *
	 * @param index The index of the render target to get
	 * @return The existing or a new render target, if no existing one exists
	 */
	public RenderTarget getOrCreate(int index) {
		if (index < 0 || index >= this.size) {
			throw new IllegalArgumentException(
				"invalid shadowcolor index " + index + " (limit " + this.size + "). Shadowcolor2-7 requires the HIGHER_SHADOWCOLOR feature flag.");
		}

		if (targets[index] != null) {
			return targets[index];
		}

		create(index);
		return targets[index];
	}

	private void create(int index) {
		PackShadowDirectives.SamplingSettings settings = shadowDirectives.getColorSamplingSettings()
			.computeIfAbsent(index, i -> new PackShadowDirectives.SamplingSettings());
		targets[index] = RenderTarget.builder().setDimensions(resolution, resolution).setInternalFormat(settings.getFormat())
			.setName("shadowcolor" + index).setPixelFormat(settings.getFormat().getPixelFormat()).build();
		formats[index] = settings.getFormat();
		if (settings.getClear()) {
			buffersToBeCleared.add(index);
		}

		if (settings.getClear()) {
			buffersToBeCleared.add(index);
		}

		fullClearRequired = true;
	}

	public void createIfEmpty(int index) {
		this.getOrCreate(index);
	}

	public int getResolution() {
		return resolution;
	}

	public GpuTexture getDepthTexture() {
		return mainDepth;
	}

	public GpuTexture getDepthTextureNoTranslucents() {
		return noTranslucents;
	}

	public RenderFramebuffer getDepthSourceFb() {
		return depthSourceFb;
	}

	public void copyPreTranslucentDepth() {
		RenderSystem.getDevice().createCommandEncoder()
			.copyTextureToTexture(this.mainDepth, this.noTranslucents, 0, 0, 0, 0, 0, this.resolution, this.resolution);
	}

	public boolean isFullClearRequired() {
		return fullClearRequired;
	}

	public void onFullClear() {
		fullClearRequired = false;
	}

	public RenderFramebuffer createFramebufferWritingToMain(int[] drawBuffers) {
		return createFullFramebuffer(false, drawBuffers);
	}

	public RenderFramebuffer createFramebufferWritingToAlt(int[] drawBuffers) {
		return createFullFramebuffer(true, drawBuffers);
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

	private RenderFramebuffer createEmptyFramebuffer() {
		RenderFramebuffer framebuffer = new RenderFramebuffer();
		ownedFramebuffers.add(framebuffer);

		framebuffer.addDepthAttachment(mainDepth);

		// NB: Before OpenGL 3.0, all framebuffers are required to have a color
		// attachment no matter what.
		framebuffer.addColorAttachment(0, this.getOrCreate(0), false);
		framebuffer.disableDrawBuffers();

		return framebuffer;
	}

	public RenderFramebuffer createDHFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		RenderFramebuffer framebuffer = this.createColorFramebuffer(stageWritesToMain, drawBuffers);

		framebuffer.addDepthAttachment(mainDepth);

		return framebuffer;
	}

	public RenderFramebuffer createShadowFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		RenderFramebuffer framebuffer = this.createColorFramebuffer(stageWritesToMain, drawBuffers);

		framebuffer.addDepthAttachment(mainDepth);

		return framebuffer;
	}

	private RenderFramebuffer createFullFramebuffer(boolean clearsAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = ImmutableSet.of();

		if (!clearsAlt) {
			stageWritesToMain = invert(ImmutableSet.of(), drawBuffers);
		}

		return createColorFramebufferWithDepth(stageWritesToMain, drawBuffers);
	}

	public RenderFramebuffer createColorFramebufferWithDepth(ImmutableSet<Integer> stageWritesToMain, int[] drawBuffers) {
		RenderFramebuffer framebuffer = this.createColorFramebuffer(stageWritesToMain, drawBuffers);

		framebuffer.addDepthAttachment(mainDepth);

		return framebuffer;
	}

	public RenderFramebuffer createColorFramebuffer(ImmutableSet<Integer> stageWritesToMain, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			throw new IllegalArgumentException("Framebuffer must have at least one color buffer");
		}

		drawBuffers = this.getDrawBuffers(drawBuffers);
		RenderFramebuffer framebuffer = new RenderFramebuffer();
		ownedFramebuffers.add(framebuffer);

		int[] actualDrawBuffers = new int[drawBuffers.length];

		for (int i = 0; i < drawBuffers.length; i++) {
			actualDrawBuffers[i] = i;

			RenderTarget target = this.getOrCreate(drawBuffers[i]);
			framebuffer.addColorAttachment(i, target, !stageWritesToMain.contains(drawBuffers[i]));
		}

		framebuffer.setDrawBuffers(actualDrawBuffers);

		framebuffer.validateAttachments();

		return framebuffer;
	}

	public RenderFramebuffer createMergedFramebuffer(ColorAttachmentPlan plan) {
		RenderFramebuffer framebuffer = new RenderFramebuffer();

		try {
			for (int i = 0; i < plan.targets().size(); i++) {
				var target = plan.targets().get(i);
				framebuffer.addColorAttachment(i, this.getOrCreate(target.index()), target.alt());
			}

			framebuffer.setDrawBuffers(IntStream.range(0, plan.targets().size()).toArray());
			framebuffer.addDepthAttachment(this.mainDepth);
			framebuffer.validateAttachments();
			this.ownedFramebuffers.add(framebuffer);

			return framebuffer;
		} catch (RuntimeException | Error failure) {
			framebuffer.destroy();

			throw failure;
		}
	}

	public int getColorTextureId(int i) {
		return isFlipped(i) ? get(i).getAltTexture() : get(i).getMainTexture();
	}

	public boolean isHardwareFiltered(int i) {
		return hardwareFiltered[i];
	}

	public int getNumColorTextures() {
		return targets.length;
	}

	public InternalTextureFormat getColorTextureFormat(int index) {
		return formats[index];
	}

	public ImmutableSet<Integer> snapshot() {
		ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
		for (int i = 0; i < flipped.length; i++) {
			if (flipped[i]) {
				builder.add(i);
			}
		}

		return builder.build();
	}

	public IntList getBuffersToBeCleared() {
		return buffersToBeCleared;
	}

	public void generateMipmaps() {
		for (int i = 0; i < 2; i++) {
			if (this.mipped[i]) {
				Iris.getRenderBackend().commands().generateDepthMips(i == 0 ? this.mainDepth : this.noTranslucents);
			}
		}

		for (int i = 0; i < this.targets.length; i++) {
			var target = this.targets[i];

			if (target == null) {
				continue;
			}

			var settings = this.shadowDirectives.getColorSamplingSettings().get(i);

			if (settings != null && settings.getMipmap()) {
				target.generateMipmaps(false);
			}
		}
	}

	public GlSampler getSamplerFor(int i) {
		if (hardwareFiltered[i]) {
			if (linearFiltered[i]) {
				if (mipped[i]) {
					return GlSampler.MIPPED_LINEAR_HW;
				} else {
					return GlSampler.LINEAR_HW;
				}
			} else {
				if (mipped[i]) {
					return GlSampler.MIPPED_NEAREST_HW;
				} else {
					return GlSampler.NEAREST_HW;
				}
			}
		} else {
			if (linearFiltered[i]) {
				if (mipped[i]) {
					return GlSampler.MIPPED_LINEAR;
				} else {
					return GlSampler.LINEAR;
				}
			} else {
				if (mipped[i]) {
					return GlSampler.MIPPED_NEAREST;
				} else {
					return GlSampler.NEAREST;
				}
			}
		}
	}
}
