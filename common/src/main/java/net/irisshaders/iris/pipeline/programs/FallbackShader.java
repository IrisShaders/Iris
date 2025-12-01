package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.opengl.Uniform;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.compat.SkipList;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.mixinterface.ShaderInstanceInterface;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GL46C;

import java.io.IOException;
import java.util.ArrayList;
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

		List<RenderPipeline.UniformDescription> uniforms = new ArrayList<>(pipeline.getUniforms());

		uniforms.add(new RenderPipeline.UniformDescription("DynamicTransforms", UniformType.UNIFORM_BUFFER));
		uniforms.add(new RenderPipeline.UniformDescription("CloudInfo", UniformType.UNIFORM_BUFFER));
		uniforms.add(new RenderPipeline.UniformDescription("Projection", UniformType.UNIFORM_BUFFER));
		uniforms.add(new RenderPipeline.UniformDescription("Fog", UniformType.UNIFORM_BUFFER));
		uniforms.add(new RenderPipeline.UniformDescription("Globals", UniformType.UNIFORM_BUFFER));
		uniforms.add(new RenderPipeline.UniformDescription("Lighting", UniformType.UNIFORM_BUFFER));

		setupUniforms(uniforms, pipeline.getSamplers());

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
	public void iris$setupState() {
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
