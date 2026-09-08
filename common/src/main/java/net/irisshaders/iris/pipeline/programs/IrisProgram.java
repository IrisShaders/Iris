package net.irisshaders.iris.pipeline.programs;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.util.TextureViewAndSampler;

import java.util.HashMap;
import java.util.List;

public interface IrisProgram {
	void iris$setupState(List<BindGroupLayout.UniformDescription> samplers);

	void iris$clearState();

	int iris$getBlockIndex(int program, CharSequence uniformBlockName);

	boolean iris$isSetUp();
}
