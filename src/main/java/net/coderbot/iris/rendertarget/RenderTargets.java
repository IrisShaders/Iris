package net.coderbot.iris.rendertarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;

import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

public class RenderTargets {
	/**
	 * The maximum number of render targets supported by Iris.
	 */
	public static int MAX_RENDER_TARGETS = 8;

	private final RenderTarget[] targets;
	private final DepthTexture depthTexture;
	private final DepthTexture noTranslucents;

	private final List<GlFramebuffer> ownedFramebuffers;

	private int cachedWidth;
	private int cachedHeight;

	private boolean destroyed = false;

	public RenderTargets(Framebuffer reference, PackRenderTargetDirectives directives) {
		this(reference.textureWidth, reference.textureHeight, directives.getRenderTargetSettings());
	}

	public RenderTargets(int width, int height, Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargets) {
		targets = new RenderTarget[MAX_RENDER_TARGETS];

		renderTargets.forEach((index, settings) -> {
			// TODO: Handle render targets above 8
			// TODO: Handle mipmapping?
			targets[index] = RenderTarget.builder().setDimensions(width, height).setInternalFormat(settings.getRequestedFormat()).build();
		});

		this.depthTexture = new DepthTexture(width, height);
		this.noTranslucents = new DepthTexture(width, height);

		this.cachedWidth = width;
		this.cachedHeight = height;

		this.ownedFramebuffers = new ArrayList<>();

		// NB: Make sure all buffers are cleared so that they don't contain undefined
		// data. Otherwise very weird things can happen.
		//
		// TODO: Make this respect the clear color of each buffer, destroy these framebuffers afterwards.
		RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);

		createFramebufferWritingToMain(new int[] {0,1,2,3,4,5,6,7}).bind();
		RenderSystem.clear(GL20C.GL_COLOR_BUFFER_BIT, false);

		createFramebufferWritingToAlt(new int[] {0,1,2,3,4,5,6,7}).bind();
		RenderSystem.clear(GL20C.GL_COLOR_BUFFER_BIT, false);

		// Make sure to rebind the vanilla framebuffer.
		MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
	}

	public void destroy() {
		destroyed = true;

		for (GlFramebuffer owned : ownedFramebuffers) {
			owned.destroy();
		}

		for (RenderTarget target : targets) {
			target.destroy();
		}

		depthTexture.destroy();
		noTranslucents.destroy();
	}

	public RenderTarget get(int index) {
		if (destroyed) {
			throw new IllegalStateException("Tried to use destroyed RenderTargets");
		}

		return targets[index];
	}

	public DepthTexture getDepthTexture() {
		if (destroyed) {
			throw new IllegalStateException("Tried to use destroyed RenderTargets");
		}

		return depthTexture;
	}

	public DepthTexture getDepthTextureNoTranslucents() {
		if (destroyed) {
			throw new IllegalStateException("Tried to use destroyed RenderTargets");
		}

		return noTranslucents;
	}

	public void resizeIfNeeded(int newWidth, int newHeight) {
		if (newWidth == cachedWidth && newHeight == cachedHeight) {
			// No resize needed
			return;
		}

		Iris.logger.info("Resizing render targets to " + newWidth + "x" + newHeight);
		cachedWidth = newWidth;
		cachedHeight = newHeight;

		for (RenderTarget target : targets) {
			target.resize(newWidth, newHeight);
		}

		depthTexture.resize(newWidth, newHeight);
		noTranslucents.resize(newWidth, newHeight);
	}

	public GlFramebuffer createFramebufferWritingToMain(int[] drawBuffers) {
		return createFullFramebuffer(false, drawBuffers);
	}

	public GlFramebuffer createFramebufferWritingToAlt(int[] drawBuffers) {
		return createFullFramebuffer(true, drawBuffers);
	}

	public GlFramebuffer createGbufferFramebuffer(ImmutableSet<Integer> flipped, int[] drawBuffers) {
		boolean[] stageWritesToAlt = new boolean[RenderTargets.MAX_RENDER_TARGETS];

		flipped.forEach(index -> stageWritesToAlt[index] = true);

		GlFramebuffer framebuffer =  createColorFramebuffer(stageWritesToAlt, drawBuffers);

		framebuffer.addDepthAttachment(this.getDepthTexture().getTextureId());

		return framebuffer;
	}

	private GlFramebuffer createFullFramebuffer(boolean clearsAlt, int[] drawBuffers) {
		boolean[] stageWritesToAlt = new boolean[RenderTargets.MAX_RENDER_TARGETS];

		Arrays.fill(stageWritesToAlt, clearsAlt);

		GlFramebuffer framebuffer =  createColorFramebuffer(stageWritesToAlt, drawBuffers);

		framebuffer.addDepthAttachment(this.getDepthTexture().getTextureId());

		return framebuffer;
	}

	public GlFramebuffer createBaselineShadowFramebuffer() {
		boolean[] stageWritesToAlt = new boolean[2];

		Arrays.fill(stageWritesToAlt, false);

		GlFramebuffer framebuffer =  createColorFramebuffer(stageWritesToAlt, new int[] {0, 1});

		framebuffer.addDepthAttachment(this.getDepthTexture().getTextureId());

		return framebuffer;
	}

	public GlFramebuffer createColorFramebuffer(boolean[] stageWritesToAlt, int[] drawBuffers) {
		GlFramebuffer framebuffer = new GlFramebuffer();
		ownedFramebuffers.add(framebuffer);

		for (int i = 0; i < stageWritesToAlt.length; i++) {
			RenderTarget target = this.get(i);

			int textureId = stageWritesToAlt[i] ? target.getAltTexture() : target.getMainTexture();

			framebuffer.addColorAttachment(i, textureId);
		}

		if (!framebuffer.isComplete()) {
			throw new IllegalStateException("Unexpected error while creating framebuffer");
		}

		framebuffer.drawBuffers(drawBuffers);

		return framebuffer;
	}

	public int getCurrentWidth() {
		return cachedWidth;
	}

	public int getCurrentHeight() {
		return cachedHeight;
	}
}
