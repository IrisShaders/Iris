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
import net.irisshaders.iris.targets.ArrayDepthTexture;
import net.irisshaders.iris.targets.ArrayRenderTarget;
import net.irisshaders.iris.targets.DepthTexture;
import net.irisshaders.iris.targets.RenderTarget;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.List;

public class ShadowRenderTargets {
	public static final int NUM_CASCADES = 4;
	public static final int TEMP_LAYER = 0;

	private final ArrayRenderTarget[] targets;
	private final PackShadowDirectives shadowDirectives;
	private final ArrayDepthTexture mainDepth;
	private final ArrayDepthTexture noTranslucents;
	private final GlFramebuffer[] depthSourceFb;
	private final GlFramebuffer[] noTranslucentsDestFb;
	private final boolean[] flipped;

	private final List<GlFramebuffer> ownedFramebuffers;
	private final int resolution;
	private final boolean[] hardwareFiltered;
	private final boolean[] linearFiltered;
	private final InternalTextureFormat[] formats;
	private final IntList buffersToBeCleared;
	private final int size;
	private boolean fullClearRequired;
	private boolean translucentDepthDirty;

	public ShadowRenderTargets(WorldRenderingPipeline pipeline, int resolution, PackShadowDirectives shadowDirectives) {
		this.shadowDirectives = shadowDirectives;
		this.size = pipeline.hasFeature(FeatureFlags.HIGHER_SHADOWCOLOR) ? PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_IRIS : PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_OF;
		targets = new ArrayRenderTarget[size];
		formats = new InternalTextureFormat[size];
		flipped = new boolean[size];
		hardwareFiltered = new boolean[size];
		linearFiltered = new boolean[size];
		buffersToBeCleared = new IntArrayList();

		this.mainDepth = new ArrayDepthTexture("shadowtex0", resolution, resolution, NUM_CASCADES, DepthBufferFormat.DEPTH);
		this.noTranslucents = new ArrayDepthTexture("shadowtex1", resolution, resolution, NUM_CASCADES, DepthBufferFormat.DEPTH);

		this.ownedFramebuffers = new ArrayList<>();
		this.resolution = resolution;

		for (int i = 0; i < shadowDirectives.getDepthSamplingSettings().size(); i++) {
			this.hardwareFiltered[i] = shadowDirectives.getDepthSamplingSettings().get(i).getHardwareFiltering();
			this.linearFiltered[i] = !shadowDirectives.getDepthSamplingSettings().get(i).getNearest();
		}

		// NB: Make sure all buffers are cleared so that they don't contain undefined
		// data. Otherwise very weird things can happen.
		fullClearRequired = true;

		this.depthSourceFb = new GlFramebuffer[NUM_CASCADES];
		this.noTranslucentsDestFb = new GlFramebuffer[NUM_CASCADES];

		for (int i = 0; i < NUM_CASCADES; i++) {
			this.depthSourceFb[i] = createFramebufferWritingToMain(new int[]{0}, i);

			this.noTranslucentsDestFb[i] = createFramebufferWritingToMain(new int[]{0}, i);

			this.noTranslucentsDestFb[i].addDepthAttachmentLayer(this.noTranslucents.getTextureId(), i);
		}

		this.translucentDepthDirty = true;
		boolean shouldRefresh = false;
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

		for (ArrayRenderTarget target : targets) {
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

	public ArrayRenderTarget get(int index) {
		return targets[index];
	}

	/**
	 * Gets the render target assigned to an index, and creates it if it does not exist.
	 * This is a <b>expensive</b> opetation nad may block other tasks! Use it sparingly, and use {@code get()} if possible.
	 *
	 * @param index The index of the render target to get
	 * @return The existing or a new render target, if no existing one exists
	 */
	public ArrayRenderTarget getOrCreate(int index) {
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
		targets[index] = ArrayRenderTarget.builder().setDimensions(resolution, resolution, NUM_CASCADES)
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

	public ArrayDepthTexture getDepthTexture() {
		return mainDepth;
	}

	public ArrayDepthTexture getDepthTextureNoTranslucents() {
		return noTranslucents;
	}

	public GlFramebuffer[] getDepthSourceFb() {
		return depthSourceFb;
	}

	public void copyPreTranslucentDepth() {
		if (translucentDepthDirty) {
			translucentDepthDirty = false;
			IrisRenderSystem.blitFramebuffer(depthSourceFb[ShadowRenderTargets.TEMP_LAYER].getId(), noTranslucentsDestFb[ShadowRenderTargets.TEMP_LAYER].getId(), 0, 0, resolution, resolution,
				0, 0, resolution, resolution,
				GL30C.GL_DEPTH_BUFFER_BIT,
				GL30C.GL_NEAREST);
		} else {
			GL43C.glCopyImageSubData(
				mainDepth.getTextureId(),
				GL43C.GL_TEXTURE_2D_ARRAY,
				0,
				0,
				0,
				0,
				noTranslucents.getTextureId(),
				GL43C.GL_TEXTURE_2D_ARRAY,
				0,
				0,
				0,
				0,
				resolution,
				resolution,
				NUM_CASCADES
			);
		}
	}

	public boolean isFullClearRequired() {
		return fullClearRequired;
	}

	public void onFullClear() {
		fullClearRequired = false;
	}

	public GlFramebuffer createFramebufferWritingToMain(int[] drawBuffers, int layer) {
		return createFullFramebuffer(false, drawBuffers, layer);
	}

	public GlFramebuffer createFramebufferWritingToAlt(int[] drawBuffers, int layer) {
		return createFullFramebuffer(true, drawBuffers, layer);
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

	private GlFramebuffer createEmptyFramebuffer(int layer) {
		GlFramebuffer framebuffer = new GlFramebuffer();
		ownedFramebuffers.add(framebuffer);

		framebuffer.addDepthAttachmentLayer(mainDepth.getTextureId(), layer);

		// NB: Before OpenGL 3.0, all framebuffers are required to have a color
		// attachment no matter what.
		framebuffer.addColorAttachment(0, get(0).getMainTexture());
		framebuffer.noDrawBuffers();

		return framebuffer;
	}

	public GlFramebuffer createDHFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers, int layer) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer(layer);
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers, layer);

		framebuffer.addDepthAttachmentLayer(mainDepth.getTextureId(), layer);

		return framebuffer;
	}

	public GlFramebuffer createShadowFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers, int layer) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer(layer);
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers, layer);

		framebuffer.addDepthAttachmentLayer(mainDepth.getTextureId(), layer);

		return framebuffer;
	}

	private GlFramebuffer createFullFramebuffer(boolean clearsAlt, int[] drawBuffers, int layer) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer(layer);
		}

		ImmutableSet<Integer> stageWritesToMain = ImmutableSet.of();

		if (!clearsAlt) {
			stageWritesToMain = invert(ImmutableSet.of(), drawBuffers);
		}

		return createColorFramebufferWithDepth(stageWritesToMain, drawBuffers, layer);
	}

	public GlFramebuffer createColorFramebufferWithDepth(ImmutableSet<Integer> stageWritesToMain, int[] drawBuffers, int layer) {
		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers, layer);

		framebuffer.addDepthAttachmentLayer(mainDepth.getTextureId(), layer);

		return framebuffer;
	}

	public GlFramebuffer createColorFramebuffer(ImmutableSet<Integer> stageWritesToMain, int[] drawBuffers, int layer) {
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
				return createColorFramebuffer(stageWritesToMain, new int[]{0, 1}, layer);
			}

			ArrayRenderTarget target = this.getOrCreate(drawBuffers[i]);

			int textureId = stageWritesToMain.contains(drawBuffers[i]) ? target.getMainTexture() : target.getAltTexture();

			framebuffer.addColorAttachmentLayer(i, textureId, layer);
		}

		framebuffer.drawBuffers(actualDrawBuffers);
		framebuffer.readBuffer(0);

		int status = framebuffer.getStatus();
		if (status != GL30C.GL_FRAMEBUFFER_COMPLETE) {
			throw new IllegalStateException("Unexpected error while creating framebuffer (Status: " + status + ")");
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
