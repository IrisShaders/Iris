package net.coderbot.iris.rendertarget;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.Arrays;
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
	private final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> targetSettingsMap;
	private final PackDirectives packDirectives;

	private int cachedWidth;
	private int cachedHeight;
	private boolean fullClearRequired;
	private boolean translucentDepthDirty;
	private boolean handDepthDirty;

	private int cachedDepthBufferVersion;
	private boolean destroyed;

	public RenderTargets(int width, int height, int depthTexture, int depthBufferVersion, DepthBufferFormat depthFormat, Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargets, PackDirectives packDirectives) {
		targets = new RenderTarget[renderTargets.size()];

		targetSettingsMap = renderTargets;
		this.packDirectives = packDirectives;

		this.currentDepthTexture = depthTexture;
		this.currentDepthFormat = depthFormat;
		this.copyStrategy = DepthCopyStrategy.fastest(currentDepthFormat.isCombinedStencil());

		this.cachedWidth = width;
		this.cachedHeight = height;
		this.cachedDepthBufferVersion = depthBufferVersion;

		this.ownedFramebuffers = new ArrayList<>();

		// NB: Make sure all buffers are cleared so that they don't contain undefined
		// data. Otherwise very weird things can happen.
		fullClearRequired = true;

		this.depthSourceFb = createFramebufferWritingToMain(new int[] {0});

		this.noTranslucents = new DepthTexture(width, height, currentDepthFormat);
		this.noHand = new DepthTexture(width, height, currentDepthFormat);

		this.noTranslucentsDestFb = createFramebufferWritingToMain(new int[] {0});
		this.noTranslucentsDestFb.addDepthAttachment(this.noTranslucents.getTextureId());

		this.noHandDestFb = createFramebufferWritingToMain(new int[] {0});
		this.noHandDestFb.addDepthAttachment(this.noHand.getTextureId());

		this.translucentDepthDirty = true;
		this.handDepthDirty = true;
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

		if (targets[index] == null) {
			return null;
		}

		return targets[index];
	}

	public RenderTarget getOrCreate(int index) {
		if (destroyed) {
			throw new IllegalStateException("Tried to use destroyed RenderTargets");
		}

		if (targets[index] != null) return targets[index];

		create(index);

		return targets[index];
	}

	private void create(int index) {
		PackRenderTargetDirectives.RenderTargetSettings settings = targetSettingsMap.get(index);
		Vector2i dimensions = packDirectives.getTextureScaleOverride(index, cachedWidth, cachedHeight);
		targets[index] = RenderTarget.builder().setDimensions(dimensions.x, dimensions.y)
			.setInternalFormat(settings.getInternalFormat())
			.setPixelFormat(settings.getInternalFormat().getPixelFormat()).build();
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

	public boolean resizeIfNeeded(int newDepthBufferVersion, int newDepthTextureId, int newWidth, int newHeight, DepthBufferFormat newDepthFormat, PackDirectives packDirectives) {
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
			// Might need a new copy strategy
			copyStrategy = DepthCopyStrategy.fastest(currentDepthFormat.isCombinedStencil());
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
				if (framebuffer == noHandDestFb || framebuffer == noTranslucentsDestFb) {
					// NB: Do not change the depth attachment of these framebuffers
					// as it is intentionally different
					continue;
				}

				if (framebuffer.hasDepthAttachment()) {
					framebuffer.addDepthAttachment(newDepthTextureId);
				}
			}
		}

		if (depthFormatChanged || sizeChanged)  {
			// Reallocate depth buffers
			noTranslucents.resize(newWidth, newHeight, newDepthFormat);
			noHand.resize(newWidth, newHeight, newDepthFormat);
			this.translucentDepthDirty = true;
			this.handDepthDirty = true;
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
		if (translucentDepthDirty) {
			translucentDepthDirty = false;
			RenderSystem.bindTexture(noTranslucents.getTextureId());
			depthSourceFb.bindAsReadBuffer();
			IrisRenderSystem.copyTexImage2D(GL20C.GL_TEXTURE_2D, 0, currentDepthFormat.getGlInternalFormat(), 0, 0, cachedWidth, cachedHeight, 0);
		} else {
			copyStrategy.copy(depthSourceFb, getDepthTexture(), noTranslucentsDestFb, noTranslucents.getTextureId(),
				getCurrentWidth(), getCurrentHeight());
		}
	}

	public void copyPreHandDepth() {
		if (handDepthDirty) {
			handDepthDirty = false;
			RenderSystem.bindTexture(noHand.getTextureId());
			depthSourceFb.bindAsReadBuffer();
			IrisRenderSystem.copyTexImage2D(GL20C.GL_TEXTURE_2D, 0, currentDepthFormat.getGlInternalFormat(), 0, 0, cachedWidth, cachedHeight, 0);
		} else {
			copyStrategy.copy(depthSourceFb, getDepthTexture(), noHandDestFb, noHand.getTextureId(),
				getCurrentWidth(), getCurrentHeight());
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
		GlFramebuffer framebuffer = new GlFramebuffer();
		ownedFramebuffers.add(framebuffer);

		framebuffer.addDepthAttachment(currentDepthTexture);

		// NB: Before OpenGL 3.0, all framebuffers are required to have a color
		// attachment no matter what.
		framebuffer.addColorAttachment(0, getOrCreate(0).getMainTexture());
		framebuffer.noDrawBuffers();

		return framebuffer;
	}

	public GlFramebuffer createGbufferFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer =  createColorFramebuffer(stageWritesToMain, drawBuffers);

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

		GlFramebuffer framebuffer = new GlFramebuffer();
		ownedFramebuffers.add(framebuffer);

		int[] actualDrawBuffers = new int[drawBuffers.length];

		for (int i = 0; i < drawBuffers.length; i++) {
			actualDrawBuffers[i] = i;

			if (drawBuffers[i] >= getRenderTargetCount()) {
				// TODO: This causes resource leaks, also we should really verify this in the shaderpack parser...
				framebuffer.destroy();
				ownedFramebuffers.remove(framebuffer);
				throw new IllegalStateException("Render target with index " + drawBuffers[i] + " is not supported, only "
						+ getRenderTargetCount() + " render targets are supported.");
			}

			RenderTarget target = this.getOrCreate(drawBuffers[i]);

			int textureId = stageWritesToMain.contains(drawBuffers[i]) ? target.getMainTexture() : target.getAltTexture();

			framebuffer.addColorAttachment(i, textureId);
		}

		framebuffer.drawBuffers(actualDrawBuffers);
		framebuffer.readBuffer(0);


		int status = framebuffer.getStatus();
		if (status != GL30C.GL_FRAMEBUFFER_COMPLETE) {
			throw new IllegalStateException("Unexpected error while creating framebuffer: Draw buffers " + Arrays.toString(actualDrawBuffers) + " Status: " + status);
		}

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
