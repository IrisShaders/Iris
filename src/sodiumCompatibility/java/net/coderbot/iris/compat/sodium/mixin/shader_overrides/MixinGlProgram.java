package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.platform.GlStateManager;
import me.jellysquid.mods.sodium.client.gl.GlObject;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderBindingContextExt;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.IntFunction;

@Mixin(GlProgram.class)
public class MixinGlProgram extends GlObject implements ShaderBindingContextExt {
	public <U extends GlUniform<?>> U bindUniformIfPresent(String name, IntFunction<U> factory) {
		int index = GlStateManager._glGetUniformLocation(this.handle(), name);
		if (index < 0) {
			return null;
		} else {
			return factory.apply(index);
		}
	}

	public GlUniformBlock bindUniformBlockIfPresent(String name, int bindingPoint) {
		int index = IrisRenderSystem.getUniformBlockIndex(this.handle(), name);
		if (index < 0) {
			return null;
		} else {
			IrisRenderSystem.uniformBlockBinding(this.handle(), index, bindingPoint);
			return new GlUniformBlock(bindingPoint);
		}
	}
}
