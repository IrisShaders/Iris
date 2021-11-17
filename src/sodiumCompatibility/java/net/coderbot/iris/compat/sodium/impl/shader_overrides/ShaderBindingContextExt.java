package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;

import java.util.function.IntFunction;

public interface ShaderBindingContextExt {
	<U extends GlUniform<?>> U bindUniformIfPresent(String var1, IntFunction<U> var2);
	GlUniformBlock bindUniformBlockIfPresent(String var1, int var2);
}
