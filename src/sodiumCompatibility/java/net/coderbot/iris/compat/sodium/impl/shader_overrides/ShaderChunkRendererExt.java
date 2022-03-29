package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import net.caffeinemc.gfx.api.shader.Program;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderInterface;

public interface ShaderChunkRendererExt<T> {
	IrisChunkProgramOverrides iris$getOverrides();

    Program<T> getIrisProgram(boolean isShadowPass, ChunkRenderPass pass);
}
