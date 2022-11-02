package net.coderbot.iris.pipeline.newshader.uniforms;

import com.mojang.blaze3d.shaders.Uniform;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.minecraft.client.renderer.GameRenderer;

public class RedirectingUniform4F extends Uniform {
	private final int shader;
	private final int location;
	private final boolean invalidUniform;
	private float x;
	private float y;
	private float z;
	private float w;

	public RedirectingUniform4F(int shader, int location) {
		super("fake", 0, 0, GameRenderer.getRendertypeCrumblingShader());
		close();

		this.shader = shader;
		this.location = location;

		invalidUniform = location < 0;
	}

	public void setOverride(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	@Override
	public void upload() {
		if (!invalidUniform) {
			IrisRenderSystem.uniform4f(location, x, y, z, w);
		}
	}

	@Override
	public int getLocation() {
		return location;
	}

	@Override
	public void close() {
	}
}
