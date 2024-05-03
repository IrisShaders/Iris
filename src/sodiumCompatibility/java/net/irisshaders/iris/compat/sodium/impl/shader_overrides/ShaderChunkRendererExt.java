package net.irisshaders.iris.compat.sodium.impl.shader_overrides;

import net.caffeinemc.mods.sodium.client.gl.shader.GlProgram;

public interface ShaderChunkRendererExt {
	GlProgram<IrisChunkShaderInterface> iris$getOverride();
}
