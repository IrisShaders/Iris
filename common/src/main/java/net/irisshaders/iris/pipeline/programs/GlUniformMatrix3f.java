package net.irisshaders.iris.pipeline.programs;

import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.joml.Matrix3fc;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class GlUniformMatrix3f extends GlUniform<Matrix3fc> {
	public GlUniformMatrix3f(int index) {
		super(index);
	}

	public void set(Matrix3fc value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buf = stack.callocFloat(9);
			value.get(buf);
			GL30C.glUniformMatrix3fv(this.index, false, buf);
		}
	}
}
