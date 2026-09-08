package net.irisshaders.iris.pipeline.programs;

import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import com.mojang.renderpearl.util.TextureViewAndSampler;
import net.irisshaders.iris.compat.SkipList;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.mixinterface.ShaderInstanceInterface;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.BindGroupLayouts;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GL46C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FallbackShader extends GlProgram implements IrisProgram {
	private final IrisRenderingPipeline parent;
	private final BlendModeOverride blendModeOverride;
	private final GlFramebuffer writingToBeforeTranslucent;
	private final GlFramebuffer writingToAfterTranslucent;

	private final int FOG_DENSITY;
	private final int FOG_IS_EXP2;
	private final int gtexture;
	private final int overlay;
	private final int lightmap;
	private boolean isSetUp;

	public FallbackShader(int programId, RenderPipeline pipeline, String string, VertexFormat vertexFormat,
						  GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent,
						  BlendModeOverride blendModeOverride, float alphaValue, IrisRenderingPipeline parent) throws IOException {
		super(programId, string);
		((ShaderInstanceInterface) this).setShouldSkip(SkipList.NONE);


		boolean has1 = false, has2 = false, has0 = false;
		if (vertexFormat.contains("UV0")) {
			has0 = true;
		}

		if (vertexFormat.contains("UV1")) {
			has1 = true;
		}

		if (vertexFormat.contains("UV2")) {
			has2 = true;
		}

		BindGroupLayout samplr = null;

		if (has0) {
			if (has1) {
				if (has2) {
					samplr = BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2;
				} else {
					samplr = BindGroupLayouts.SAMPLER0_SAMPLER1;
				}
			} else if (has2) {
				samplr = BindGroupLayouts.SAMPLER0_SAMPLER2;
			} else {
				samplr = BindGroupLayouts.SAMPLER0;
			}
		}

		List<BindGroupLayout.UniformDescription> layouts = new ArrayList<>();
		if (samplr != null) layouts.addAll(samplr.uniforms());
		layouts.addAll(BindGroupLayouts.DYNAMIC_TRANSFORMS.uniforms());
		layouts.addAll(BindGroupLayouts.CLOUD_INFO.uniforms());
		layouts.addAll(BindGroupLayouts.PROJECTION.uniforms());
		layouts.addAll(BindGroupLayouts.GLOBALS.uniforms());
		layouts.addAll(BindGroupLayouts.FOG.uniforms());

		super.setupBindGroupLayouts(layouts);

		this.parent = parent;
		this.blendModeOverride = blendModeOverride;
		this.writingToBeforeTranslucent = writingToBeforeTranslucent;
		this.writingToAfterTranslucent = writingToAfterTranslucent;

		this.FOG_DENSITY = GlStateManager._glGetUniformLocation(programId, "FogDensity");
		this.FOG_IS_EXP2 = GlStateManager._glGetUniformLocation(programId, "FogIsExp2");

		this.gtexture = GlStateManager._glGetUniformLocation(programId, "gtexture");
		this.overlay = GlStateManager._glGetUniformLocation(programId, "overlay");
		this.lightmap = GlStateManager._glGetUniformLocation(programId, "lightmap");

		GlStateManager._glUseProgram(programId);


		int ALPHA_TEST_VALUE = GlStateManager._glGetUniformLocation(programId, "AlphaTestValue");

		if (ALPHA_TEST_VALUE > -1) {
			GL46C.glUniform1f(ALPHA_TEST_VALUE, alphaValue);
		}
	}

	@Override
	public void iris$clearState() {
		if (this.blendModeOverride != null) {
			BlendModeOverride.restore();
		}

		isSetUp = false;
	}

	@Override
	public int iris$getBlockIndex(int program, CharSequence uniformBlockName) {
		return GL31C.glGetUniformBlockIndex(program, uniformBlockName);
	}

	@Override
	public boolean iris$isSetUp() {
		return isSetUp;
	}

	@Override
	public void iris$setupState(List<BindGroupLayout.UniformDescription> samplers) {
		isSetUp = true;
		DepthColorStorage.unlockDepthColor();

		GlStateManager._glUseProgram(getProgramId());

		if (FOG_DENSITY > -1 && FOG_IS_EXP2 > -1) {
			float fogDensity = CapturedRenderingState.INSTANCE.getFogDensity();

			if (fogDensity >= 0.0) {
				GL46C.glUniform1f(FOG_DENSITY, fogDensity);
				GL46C.glUniform1i(FOG_IS_EXP2, 1);
			} else {
				GL46C.glUniform1f(FOG_DENSITY, 0.0f);
				GL46C.glUniform1i(FOG_IS_EXP2, 0);
			}
		}

		GlStateManager._glUniform1i(gtexture, 0);
		GlStateManager._glUniform1i(overlay, 1);
		GlStateManager._glUniform1i(lightmap, 2);

		if (this.blendModeOverride != null) {
			this.blendModeOverride.apply();
		}

		if (parent.isBeforeTranslucent) {
			writingToBeforeTranslucent.bind();
		} else {
			writingToAfterTranslucent.bind();
		}
	}
}
