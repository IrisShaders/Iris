package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

public class BufferSegmentRenderer {
    private final BufferBuilder fakeBufferBuilder;
    private final BufferBuilderExt fakeBufferBuilderExt;

    public BufferSegmentRenderer() {
        this.fakeBufferBuilder = new BufferBuilder(0);
        this.fakeBufferBuilderExt = (BufferBuilderExt) this.fakeBufferBuilder;
    }

    /**
     * Sets up the render type, draws the buffer, and then tears down the render type.
     */
    public void draw(BufferSegment segment) {
        segment.getRenderType().setupRenderState();
		setupShader(segment.getRenderType().mode());
        drawInner(segment);
		clearShader();
        segment.getRenderType().clearRenderState();
    }


	/**
     * Like draw(), but it doesn't setup / tear down the render type.
     */
    public void drawInner(BufferSegment segment) {
		fakeBufferBuilderExt.setupBufferSlice(segment.getSlice(), segment.getDrawState());
		BufferUploader._endInternal(fakeBufferBuilder);
		fakeBufferBuilderExt.teardownBufferSlice();
	}

	public static void clearShader() {
		if (RenderSystem.getShader() != null) {
			RenderSystem.getShader().clear();
		}
	}

	public static void setupShader(VertexFormat.Mode mode) {
		ShaderInstance shader = RenderSystem.getShader();

		if (shader == null) return;

		for (int lvInt4 = 0; lvInt4 < 12; ++lvInt4) {
			int lvInt5 = RenderSystem.getShaderTexture(lvInt4);
			shader.setSampler("Sampler" + lvInt4, lvInt5);
		}
		if (shader.MODEL_VIEW_MATRIX != null) {
			shader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
		}
		if (shader.PROJECTION_MATRIX != null) {
			shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
		}
		if (shader.INVERSE_VIEW_ROTATION_MATRIX != null) {
			shader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
		}
		if (shader.COLOR_MODULATOR != null) {
			shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
		}
		if (shader.FOG_START != null) {
			shader.FOG_START.set(RenderSystem.getShaderFogStart());
		}
		if (shader.FOG_END != null) {
			shader.FOG_END.set(RenderSystem.getShaderFogEnd());
		}
		if (shader.FOG_COLOR != null) {
			shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
		}
		if (shader.FOG_SHAPE != null) {
			shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
		}
		if (shader.TEXTURE_MATRIX != null) {
			shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
		}
		if (shader.GAME_TIME != null) {
			shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
		}
		if (shader.SCREEN_SIZE != null) {
			Window lvWindow4 = Minecraft.getInstance().getWindow();
			shader.SCREEN_SIZE.set((float)lvWindow4.getWidth(), (float)lvWindow4.getHeight());
		}
		if (shader.LINE_WIDTH != null && (mode == VertexFormat.Mode.LINES || mode == VertexFormat.Mode.LINE_STRIP)) {
			shader.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
		}
		RenderSystem.setupShaderLights(shader);
		shader.apply();
	}
}
