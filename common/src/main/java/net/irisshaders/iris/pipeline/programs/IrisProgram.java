package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.textures.GpuTextureView;

public interface IrisProgram {
	void iris$setupState(GpuTextureView albedoTex);

	void iris$clearState();

	int iris$getBlockIndex(int program, CharSequence uniformBlockName);

	boolean iris$isSetUp();
}
