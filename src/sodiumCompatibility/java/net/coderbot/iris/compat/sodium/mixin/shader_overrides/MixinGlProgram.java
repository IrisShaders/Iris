package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.GlObject;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderBindingContextExt;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.IntFunction;

@Mixin(GlProgram.class)
public class MixinGlProgram extends GlObject implements ShaderBindingContextExt {
	public <U extends GlUniform<?>> U bindUniformIfPresent(String name, IntFunction<U> factory) {
		int index = GL20C.glGetUniformLocation(this.handle(), name);
		if (index < 0) {
			return null;
		} else {
			return factory.apply(index);
		}
	}

	public GlUniformBlock bindUniformBlockIfPresent(String name, int bindingPoint) {
		int index = GL32C.glGetUniformBlockIndex(this.handle(), name);
		if (index < 0) {
			return null;
		} else {
			GL32C.glUniformBlockBinding(this.handle(), index, bindingPoint);
			return new GlUniformBlock(bindingPoint);
		}
	}
}
