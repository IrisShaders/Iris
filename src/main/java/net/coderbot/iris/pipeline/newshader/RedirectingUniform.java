package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.math.Matrix4f;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RedirectingUniform extends Uniform {
	private final Consumer<Matrix4f> setup;

	public RedirectingUniform(String string, int i, int j, ExtendedShader arg, Consumer<Matrix4f> setup) {
		super(string, i, j, arg);
		this.setup = setup;
	}

	public void accept(Matrix4f arg) {
		setup.accept(arg);
	}
}
