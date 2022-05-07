package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat4v;

public class IrisShaderFogComponent {
	private final GlUniformFloat4v uFogColor;
	private final GlUniformFloat uFogStart;
	private final GlUniformFloat uFogEnd;

	public IrisShaderFogComponent(ShaderBindingContextExt context) {
		this.uFogColor = context.bindUniformIfPresent("u_FogColor", GlUniformFloat4v::new);
		this.uFogStart = context.bindUniformIfPresent("u_FogStart", GlUniformFloat::new);
		this.uFogEnd = context.bindUniformIfPresent("u_FogEnd", GlUniformFloat::new);
	}

	public void setup() {
		if (this.uFogColor != null) {
			this.uFogColor.set(RenderSystem.getShaderFogColor());
		}

		if (this.uFogStart != null) {
			this.uFogStart.setFloat(RenderSystem.getShaderFogStart());
		}

		if (this.uFogEnd != null) {
			this.uFogEnd.setFloat(RenderSystem.getShaderFogEnd());
		}
	}
}
