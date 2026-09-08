package net.irisshaders.iris.mixinterface;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout;

import java.util.List;

public interface GlProgramBindings {
	void iris$setupBindings(List<BindGroupLayout.UniformDescription> descriptions, int pushConstantsSize);
}
