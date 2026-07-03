package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;

import java.util.HashMap;
import java.util.Map;

public interface IrisProgram {
	void iris$setupState(HashMap<String, GlRenderPass.TextureViewAndSampler> samplers, GpuTextureView albedoTex);

	void iris$clearState();

	int iris$getBlockIndex(int program, CharSequence uniformBlockName);

	boolean iris$isSetUp();

	/**
	 * Binds any mod-registered custom uniform blocks (IrisShaders/Iris#2974) that this draw's render
	 * pass supplies, into this substituted program. Optional by design: only blocks actually present
	 * in {@code passUniforms} are bound, so draws that share this program but don't use the block
	 * (e.g. vanilla terrain) are left untouched. Default no-op.
	 */
	default void iris$bindCustomUniformBlocks(Map<String, GpuBufferSlice> passUniforms) {
	}
}
