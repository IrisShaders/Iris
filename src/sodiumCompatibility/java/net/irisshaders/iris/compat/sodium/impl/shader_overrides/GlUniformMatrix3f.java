package net.irisshaders.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.joml.Matrix3f;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class GlUniformMatrix3f extends GlUniform<Matrix3f> {
	public GlUniformMatrix3f(int index) {
		super(index);
	}

	@Override
	public void set(Matrix3f value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buf = stack.callocFloat(12);
			value.get(buf);

			GL30C.glUniformMatrix3fv(this.index, false, buf);
		}
	}
}
