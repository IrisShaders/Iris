package net.irisshaders.iris.pipeline.programs;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout;

import java.util.List;

public interface IrisProgram {
	void iris$setupState(List<BindGroupLayout.UniformDescription> samplers);

	void iris$clearState();

	int iris$getBlockIndex(int program, CharSequence uniformBlockName);

	boolean iris$isSetUp();
}
