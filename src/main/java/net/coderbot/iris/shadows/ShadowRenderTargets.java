package net.coderbot.iris.shadows;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.rendertarget.DepthTexture;
import net.coderbot.iris.rendertarget.RenderTarget;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.List;

public class ShadowRenderTargets {
	private final RenderTarget[] targets;
	private final DepthTexture mainDepth;
	private final DepthTexture noTranslucents;
	private final GlFramebuffer depthSourceFb;
	private final GlFramebuffer noTranslucentsDestFb;
	private final GlFramebuffer mainRenderBuffer;
	private final boolean[] flipped;

	private final List<GlFramebuffer> ownedFramebuffers;
	private final int resolution;

	private boolean fullClearRequired;
	private boolean translucentDepthDirty;
	private boolean[] hardwareFiltered;
	private InternalTextureFormat[] formats;
	private IntList buffersToBeCleared;

	public ShadowRenderTargets(int resolution, PackShadowDirectives shadowDirectives) {
		targets = new RenderTarget[shadowDirectives.getColorSamplingSettings().size()];
		formats = new InternalTextureFormat[shadowDirectives.getColorSamplingSettings().size()];
		flipped = new boolean[shadowDirectives.getColorSamplingSettings().size()];
		hardwareFiltered = new boolean[shadowDirectives.getColorSamplingSettings().size()];
		buffersToBeCleared = new IntArrayList();

		this.mainDepth = new DepthTexture(resolution, resolution, DepthBufferFormat.DEPTH);
		this.noTranslucents = new DepthTexture(resolution, resolution, DepthBufferFormat.DEPTH);
		int[] drawBuffers = new int[shadowDirectives.getColorSamplingSettings().size()];

		for (int i = 0; i < shadowDirectives.getColorSamplingSettings().size(); i++) {
			PackShadowDirectives.SamplingSettings settings = shadowDirectives.getColorSamplingSettings().get(i);
			targets[i] = RenderTarget.builder().setDimensions(resolution, resolution)
				.setInternalFormat(settings.getFormat())
				.setPixelFormat(settings.getFormat().getPixelFormat()).build();
			formats[i] = settings.getFormat();
			if (settings.getClear()) {
				buffersToBeCleared.add(i);
			}

			drawBuffers[i] = i;

			if (settings.getClear()) {
				buffersToBeCleared.add(i);
			}
		}

		for (int i = 0; i < shadowDirectives.getDepthSamplingSettings().size(); i++) {
			this.hardwareFiltered[i] = shadowDirectives.getDepthSamplingSettings().get(i).getHardwareFiltering();
		}

		this.resolution = resolution;

		this.ownedFramebuffers = new ArrayList<>();

		// NB: Make sure all buffers are cleared so that they don't contain undefined
		// data. Otherwise very weird things can happen.
		fullClearRequired = true;

		this.depthSourceFb = createFramebufferWritingToMain(new int[] {0});
		this.mainRenderBuffer = createFramebufferWritingToMain(drawBuffers);
		this.mainRenderBuffer.addDepthAttachment(this.mainDepth.getTextureId());

		this.noTranslucentsDestFb = createFramebufferWritingToMain(new int[] {0});
		this.noTranslucentsDestFb.addDepthAttachment(this.noTranslucents.getTextureId());

		this.translucentDepthDirty = true;
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
			target.destroy();
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

	public GlFramebuffer getMainRenderBuffer() {
		return mainRenderBuffer;
	}

	public GlFramebuffer createShadowFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer =  createColorFramebuffer(stageWritesToMain, drawBuffers);

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
				// TODO: This causes resource leaks, also we should really verify this in the shaderpack parser...
				throw new IllegalStateException("Render target with index " + drawBuffers[i] + " is not supported, only "
					+ getRenderTargetCount() + " render targets are supported.");
			}

			RenderTarget target = this.get(drawBuffers[i]);

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
}
