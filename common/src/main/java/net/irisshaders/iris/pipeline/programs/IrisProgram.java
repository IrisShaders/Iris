package net.irisshaders.iris.pipeline.programs;

import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;

import java.util.HashMap;

public interface IrisProgram {
	void iris$setupState(HashMap<String, GlRenderPass.TextureViewAndSampler> samplers, GpuTextureView albedoTex);

	void iris$clearState();

	int iris$getBlockIndex(int program, CharSequence uniformBlockName);

	boolean iris$isSetUp();
}
