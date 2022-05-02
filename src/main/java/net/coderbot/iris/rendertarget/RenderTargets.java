package net.coderbot.iris.rendertarget;

import com.google.common.collect.ImmutableSet;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderTargets {
	private final RenderTarget[] targets;
	private int currentDepthTexture;
	private DepthBufferFormat currentDepthFormat;

	private final DepthTexture noTranslucents;
	private final DepthTexture noHand;
	private final GlFramebuffer depthSourceFb;
	private final GlFramebuffer noTranslucentsDestFb;
	private final GlFramebuffer noHandDestFb;
	private DepthCopyStrategy copyStrategy;

	private final List<GlFramebuffer> ownedFramebuffers;

	private int cachedWidth;
	private int cachedHeight;
	private boolean fullClearRequired;

	private boolean destroyed;

	public RenderTargets(int width, int height, int depthTexture, DepthBufferFormat depthFormat, Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargets) {
		targets = new RenderTarget[renderTargets.size()];

		renderTargets.forEach((index, settings) -> {
			// TODO: Handle mipmapping?
			targets[index] = RenderTarget.builder().setDimensions(width, height)
					.setInternalFormat(settings.getInternalFormat())
					.setPixelFormat(settings.getInternalFormat().getPixelFormat()).build();
		});

		this.currentDepthTexture = depthTexture;
		this.currentDepthFormat = depthFormat;
		this.copyStrategy = DepthCopyStrategy.fastest(currentDepthFormat.isCombinedStencil());

		this.noTranslucents = new DepthTexture(width, height, currentDepthFormat);
		this.noHand = new DepthTexture(width, height, currentDepthFormat);

		this.cachedWidth = width;
		this.cachedHeight = height;

		this.ownedFramebuffers = new ArrayList<>();

		// NB: Make sure all buffers are cleared so that they don't contain undefined
		// data. Otherwise very weird things can happen.
		fullClearRequired = true;

		this.depthSourceFb = createFramebufferWritingToMain(new int[] {0});

		this.noTranslucentsDestFb = createFramebufferWritingToMain(new int[] {0});
		this.noTranslucentsDestFb.addDepthAttachment(this.noTranslucents.getTextureId());

		this.noHandDestFb = createFramebufferWritingToMain(new int[] {0});
		this.noHandDestFb.addDepthAttachment(this.noHand.getTextureId());
	}

	public void destroy() {
		destroyed = true;

		for (GlFramebuffer owned : ownedFramebuffers) {
			owned.destroy();
		}

		for (RenderTarget target : targets) {
			target.destroy();
		}

		noTranslucents.destroy();
		noHand.destroy();
	}

	public int getRenderTargetCount() {
		return targets.length;
	}

	public RenderTarget get(int index) {
		if (destroyed) {
			throw new IllegalStateException("Tried to use destroyed RenderTargets");
		}

		return targets[index];
	}

	public int getDepthTexture() {
		return currentDepthTexture;
	}

	public DepthTexture getDepthTextureNoTranslucents() {
		if (destroyed) {
			throw new IllegalStateException("Tried to use destroyed RenderTargets");
		}

		return noTranslucents;
	}

	public DepthTexture getDepthTextureNoHand() {
		return noHand;
	}

	public void resizeIfNeeded(boolean recreateDepth, int newDepthTextureId, int newWidth, int newHeight, DepthBufferFormat newDepthFormat) {
		if (newDepthTextureId != currentDepthTexture) {
			recreateDepth = true;
			currentDepthTexture = newDepthTextureId;
		}

		boolean sizeChanged = newWidth != cachedWidth || newHeight != cachedHeight;
		boolean depthFormatChanged = newDepthFormat != currentDepthFormat;

		if (depthFormatChanged) {
			// Might need a new copy strategy
			copyStrategy = DepthCopyStrategy.fastest(currentDepthFormat.isCombinedStencil());
		}

		if (depthFormatChanged || sizeChanged)  {
			// Reallocate depth buffers
			noTranslucents.resize(newWidth, newHeight, newDepthFormat);
			noHand.resize(newWidth, newHeight, newDepthFormat);
		}

		if (recreateDepth) {
			// Re-attach the depth textures with the new depth texture ID, since Minecraft re-creates
			// the depth texture when resizing its render targets.
			//
			// I'm not sure if our framebuffers holding on to the old depth texture between frames
			// could be a concern, in the case of resizing and similar. I think it should work
			// based on what I've seen of the spec, though - it seems like deleting a texture
			// automatically detaches it from its framebuffers.
			for (GlFramebuffer framebuffer : ownedFramebuffers) {
				framebuffer.addDepthAttachment(newDepthTextureId);
			}
		}

		if (sizeChanged) {
			cachedWidth = newWidth;
			cachedHeight = newHeight;

			for (RenderTarget target : targets) {
				target.resize(newWidth, newHeight);
			}

			fullClearRequired = true;
		}
	}

	public void copyPreTranslucentDepth() {
		copyStrategy.copy(depthSourceFb, getDepthTexture(), noTranslucentsDestFb, noTranslucents.getTextureId(),
			getCurrentWidth(), getCurrentHeight());
	}

	public void copyPreHandDepth() {
		copyStrategy.copy(depthSourceFb, getDepthTexture(), noHandDestFb, noHand.getTextureId(),
			getCurrentWidth(), getCurrentHeight());
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

	public GlFramebuffer createGbufferFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			throw new IllegalArgumentException("Framebuffer must have at least one color buffer");
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer =  createColorFramebuffer(stageWritesToMain, drawBuffers);

		framebuffer.addDepthAttachment(currentDepthTexture);

		return framebuffer;
	}

	private GlFramebuffer createFullFramebuffer(boolean clearsAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			throw new IllegalArgumentException("Framebuffer must have at least one color buffer");
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

		if (!framebuffer.isComplete()) {
			throw new IllegalStateException("Unexpected error while creating framebuffer");
		}

		return framebuffer;
	}

	public int getCurrentWidth() {
		return cachedWidth;
	}

	public int getCurrentHeight() {
		return cachedHeight;
	}
}
