package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;

public interface ShaderChunkRendererExt {
	GlProgram<IrisChunkShaderInterface> iris$getOverride();
}
