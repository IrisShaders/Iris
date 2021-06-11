package net.coderbot.iris.shadows;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.coderbot.iris.rendertarget.DepthTexture;
import net.minecraft.client.render.Camera;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

public class EmptyShadowMapRenderer implements ShadowMapRenderer {
	private final ShadowRenderTargets targets;

	public EmptyShadowMapRenderer(int size) {
		this.targets = new ShadowRenderTargets(size, new InternalTextureFormat[]{
				InternalTextureFormat.RGBA,
				InternalTextureFormat.RGBA
		});

		// NB: We don't use getDepthTextureNoTranslucents
		GlStateManager.bindTexture(targets.getDepthTexture().getTextureId());

		// We have to do this or else sampling a sampler2DShadow produces "undefined" results.
		//
		// For example, if this call is omitted under Mesa then it will appear as if the whole world is in shadow at all
		// times.
		//
		// TODO: Do not require OpenGL 3.0 and only enable this if shadowHardwareFiltering is enabled
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);

		// The shadow texture should be smoothed.
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
		GL20C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_LINEAR);

		GlStateManager.bindTexture(0);

		targets.getFramebuffer().bind();

		// Hopefully I'm not clobbering any other OpenGL state here...
		GL20C.glClearDepth(1.0);
		GL20C.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GL20C.glClear(GL20C.GL_DEPTH_BUFFER_BIT | GL20C.GL_COLOR_BUFFER_BIT);

		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
	}

	@Override
	public void renderShadows(WorldRendererAccessor worldRenderer, Camera playerCamera) {
		// No-op
	}

	public int getDepthTextureId() {
		return targets.getDepthTexture().getTextureId();
	}

	@Override
	public int getDepthTextureNoTranslucentsId() {
		return targets.getDepthTexture().getTextureId();
	}

	@Override
	public int getColorTexture0Id() {
		return targets.getColorTextureId(0);
	}

	@Override
	public int getColorTexture1Id() {
		return targets.getColorTextureId(1);
	}

	public void destroy() {
		this.targets.destroy();
	}
}
