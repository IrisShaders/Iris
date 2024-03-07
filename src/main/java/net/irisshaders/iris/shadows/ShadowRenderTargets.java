package net.irisshaders.iris.shadows;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import net.irisshaders.iris.gl.texture.DepthCopyStrategy;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.properties.PackShadowDirectives;
import net.irisshaders.iris.targets.DepthTexture;
import net.irisshaders.iris.targets.RenderTarget;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.List;

public class ShadowRenderTargets {
	private final RenderTarget[] targets;
	private final PackShadowDirectives shadowDirectives;
	private final DepthTexture mainDepth;
	private final DepthTexture noTranslucents;
	private final GlFramebuffer depthSourceFb;
	private final GlFramebuffer noTranslucentsDestFb;
	private final boolean[] flipped;

	private final List<GlFramebuffer> ownedFramebuffers;
	private final int resolution;
	private final WorldRenderingPipeline pipeline;

	private boolean fullClearRequired;
	private boolean translucentDepthDirty;
	private final boolean[] hardwareFiltered;
	private final boolean[] linearFiltered;
	private final InternalTextureFormat[] formats;
	private final IntList buffersToBeCleared;
	private final int size;

	private final boolean shouldRefresh;

	public ShadowRenderTargets(WorldRenderingPipeline pipeline, int resolution, PackShadowDirectives shadowDirectives) {
		this.pipeline = pipeline;
		this.shadowDirectives = shadowDirectives;
		this.size = pipeline.hasFeature(FeatureFlags.HIGHER_SHADOWCOLOR) ? PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_IRIS : PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_OF;
		targets = new RenderTarget[size];
		formats = new InternalTextureFormat[size];
		flipped = new boolean[size];
		hardwareFiltered = new boolean[size];
		linearFiltered = new boolean[size];
		buffersToBeCleared = new IntArrayList();

		this.mainDepth = new DepthTexture("shadowtex0", resolution, resolution, DepthBufferFormat.DEPTH);
		this.noTranslucents = new DepthTexture("shadowtex1", resolution, resolution, DepthBufferFormat.DEPTH);

		this.ownedFramebuffers = new ArrayList<>();
		this.resolution = resolution;

		for (int i = 0; i < shadowDirectives.getDepthSamplingSettings().size(); i++) {
			this.hardwareFiltered[i] = shadowDirectives.getDepthSamplingSettings().get(i).getHardwareFiltering();
			this.linearFiltered[i] = !shadowDirectives.getDepthSamplingSettings().get(i).getNearest();
		}

		// NB: Make sure all buffers are cleared so that they don't contain undefined
		// data. Otherwise very weird things can happen.
		fullClearRequired = true;

		this.depthSourceFb = createFramebufferWritingToMain(new int[]{0});

		this.noTranslucentsDestFb = createFramebufferWritingToMain(new int[]{0});
		this.noTranslucentsDestFb.addDepthAttachment(this.noTranslucents.getTextureId());

		this.translucentDepthDirty = true;
		this.shouldRefresh = false;
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

		mainDepth.destroy();
		noTranslucents.destroy();
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

	public DepthTexture getDepthTexture() {
		return mainDepth;
	}

	public DepthTexture getDepthTextureNoTranslucents() {
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
			DepthCopyStrategy.fastest(false).copy(depthSourceFb, mainDepth.getTextureId(), noTranslucentsDestFb, noTranslucents.getTextureId(),
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

		framebuffer.addDepthAttachment(mainDepth.getTextureId());

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

		framebuffer.addDepthAttachment(mainDepth.getTextureId());

		return framebuffer;
	}

	public GlFramebuffer createShadowFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers);

		framebuffer.addDepthAttachment(mainDepth.getTextureId());

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

		framebuffer.addDepthAttachment(mainDepth.getTextureId());

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

	public boolean isLinearFiltered(int i) {
		return linearFiltered[i];
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

}
