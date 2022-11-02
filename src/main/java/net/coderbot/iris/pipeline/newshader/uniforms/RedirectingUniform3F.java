package net.coderbot.iris.pipeline.newshader.uniforms;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class RedirectingUniform3F extends Uniform {
	private final int shader;
	private final int location;
	private final boolean invalidUniform;
	private float x;
	private float y;
	private float z;

	public RedirectingUniform3F(int shader, int location) {
		super("fake", 0, 0, GameRenderer.getRendertypeCrumblingShader());
		close();

		this.shader = shader;
		this.location = location;

		invalidUniform = location < 0;
	}

	public void setOverride(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void upload() {
		if (!invalidUniform) {
			IrisRenderSystem.uniform3f(location, x, y, z);
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
