package net.coderbot.iris.pipeline.newshader.uniforms;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.coderbot.iris.Iris;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class RedirectingUniformMatrix extends Uniform {
	private final int shader;
	private final int location;
	private final boolean invalidUniform;
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

	public RedirectingUniformMatrix(int shader, int location) {
		super("fake", 0, 0, GameRenderer.getRendertypeCrumblingShader());
		super.close();

		this.shader = shader;
		this.location = location;

		invalidUniform = location < 0;
	}

	public void setOverride(Matrix4f matrix) {
		matrix.store(buffer);
		buffer.rewind();
	}

	@Override
	public void upload() {
		if (!invalidUniform) {
			RenderSystem.glUniformMatrix4(location, false, buffer);
		}
	}

	@Override
	public int getLocation() {
		return location;
	}

	@Override
	public void close() {
		MemoryUtil.memFree(buffer);
	}
}
