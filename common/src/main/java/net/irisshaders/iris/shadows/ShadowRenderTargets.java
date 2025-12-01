package net.irisshaders.iris.shadows;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.DepthCopyStrategy;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.properties.PackShadowDirectives;
import net.irisshaders.iris.targets.RenderTarget;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.List;

public class ShadowRenderTargets {
	private final RenderTarget[] targets;
	private final PackShadowDirectives shadowDirectives;
	private final GpuTexture mainDepth;
	private final GpuTexture noTranslucents;
	private final GlFramebuffer depthSourceFb;
	private final GlFramebuffer noTranslucentsDestFb;
	private final boolean[] flipped;

	private final List<GlFramebuffer> ownedFramebuffers;
	private final int resolution;
	private final boolean[] hardwareFiltered;
	private final boolean[] mipped;
	private final boolean[] linearFiltered;
	private final InternalTextureFormat[] formats;
	private final IntList buffersToBeCleared;
	private final int size;
	private boolean fullClearRequired;
	private boolean translucentDepthDirty;

	public ShadowRenderTargets(WorldRenderingPipeline pipeline, int resolution, PackShadowDirectives shadowDirectives) {
		this.shadowDirectives = shadowDirectives;
		this.size = pipeline.hasFeature(FeatureFlags.HIGHER_SHADOWCOLOR) ? PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_IRIS : PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_OF;
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

		this.mainDepth = RenderSystem.getDevice().createTexture("Shadow Map", GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.DEPTH32, resolution, resolution, 1, this.mipped[0] ? log2(resolution) : 1);
		this.noTranslucents = RenderSystem.getDevice().createTexture("Shadow Map / Opaque", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.DEPTH32, resolution, resolution, 1, this.mipped[1] ? log2(resolution) : 1);

		this.noTranslucents.setTextureFilter(linearFiltered[1] ? FilterMode.LINEAR : FilterMode.NEAREST, this.mipped[1]);
		this.mainDepth.setTextureFilter(linearFiltered[0] ? FilterMode.LINEAR : FilterMode.NEAREST, this.mipped[0]);

		// NB: Make sure all buffers are cleared so that they don't contain undefined
		// data. Otherwise very weird things can happen.
		fullClearRequired = true;

		this.depthSourceFb = createFramebufferWritingToMain(new int[]{0});

		this.noTranslucentsDestFb = createFramebufferWritingToMain(new int[]{0});
		this.noTranslucentsDestFb.addDepthAttachment(this.noTranslucents);

		this.translucentDepthDirty = true;
		boolean shouldRefresh = false;
	}

	private static final double LN_OF_2 = Math.log(2.0);

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
		for (GlFramebuffer owned : ownedFramebuffers) {
			owned.destroy();
		}

		for (RenderTarget target : targets) {
			if (target != null) {
				target.destroy();
			}
		}

		mainDepth.close();
		noTranslucents.close();
	}

	public int getRenderTargetCount() {
		return targets.length;
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
		if (targets[index] != null) {
			return targets[index];
		}

		create(index);
		return targets[index];
	}

	private void create(int index) {
		if (index > size) {
			throw new IllegalStateException("Tried to access buffer higher than allowed limit of " + size + "! If you're trying to use shadowcolor2-7, you need to activate it's feature flag!");
		}


		PackShadowDirectives.SamplingSettings settings = shadowDirectives.getColorSamplingSettings().computeIfAbsent(index, i -> new PackShadowDirectives.SamplingSettings());
		targets[index] = RenderTarget.builder().setDimensions(resolution, resolution)
			.setInternalFormat(settings.getFormat())
			.setName("shadowcolor" + index)
			.setPixelFormat(settings.getFormat().getPixelFormat()).build();
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
		if (targets[index] == null) {
			create(index);
		}
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

	public GlFramebuffer getDepthSourceFb() {
		return depthSourceFb;
	}

	public void copyPreTranslucentDepth() {
		if (translucentDepthDirty) {
			translucentDepthDirty = false;
			IrisRenderSystem.blitFramebuffer(depthSourceFb.getId(), noTranslucentsDestFb.getId(), 0, 0, resolution, resolution,
				0, 0, resolution, resolution,
				GL30C.GL_DEPTH_BUFFER_BIT,
				GL30C.GL_NEAREST);
		} else {
			DepthCopyStrategy.fastest(false).copy(depthSourceFb, mainDepth.iris$getGlId(), noTranslucentsDestFb, noTranslucents.iris$getGlId(),
				resolution, resolution);
		}
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
		GlFramebuffer framebuffer = new GlFramebuffer();
		ownedFramebuffers.add(framebuffer);

		framebuffer.addDepthAttachment(mainDepth);

		// NB: Before OpenGL 3.0, all framebuffers are required to have a color
		// attachment no matter what.
		framebuffer.addColorAttachment(0, get(0).getMainTexture());
		framebuffer.noDrawBuffers();

		return framebuffer;
	}

	public GlFramebuffer createDHFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers);

		framebuffer.addDepthAttachment(mainDepth);

		return framebuffer;
	}

	public GlFramebuffer createShadowFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers);

		framebuffer.addDepthAttachment(mainDepth);

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

		framebuffer.addDepthAttachment(mainDepth);

		return framebuffer;
	}

	public GlFramebuffer createColorFramebuffer(ImmutableSet<Integer> stageWritesToMain, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			throw new IllegalArgumentException("Framebuffer must have at least one color buffer");
		}

		GlFramebuffer framebuffer = new GlFramebuffer();
		ownedFramebuffers.add(framebuffer);

		int[] actualDrawBuffers = new int[drawBuffers.length];

		for (int i = 0; i < drawBuffers.length; i++) {
			actualDrawBuffers[i] = i;

			if (drawBuffers[i] >= getRenderTargetCount()) {
				// If a shader is using an invalid drawbuffer, they're most likely relying on the Optifine behavior of ignoring DRAWBUFFERS in the shadow pass.
				// We need to fix this for them, since apparantly this is a common issue.
				// Iris.logger.warn("Invalid framebuffer was attempted to be created! Forcing a framebuffer with DRAWBUFFERS 01 for shadow.");
				ownedFramebuffers.remove(framebuffer);
				framebuffer.destroy();
				return createColorFramebuffer(stageWritesToMain, new int[]{0, 1});
			}

			RenderTarget target = this.getOrCreate(drawBuffers[i]);

			int textureId = stageWritesToMain.contains(drawBuffers[i]) ? target.getMainTexture() : target.getAltTexture();

			framebuffer.addColorAttachment(i, textureId);
		}

		framebuffer.drawBuffers(actualDrawBuffers);
		framebuffer.readBuffer(0);

		int status = framebuffer.getStatus();
		if (status != GL30C.GL_FRAMEBUFFER_COMPLETE) {
			throw new IllegalStateException("Unexpected error while creating framebuffer");
		}

		return framebuffer;
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
