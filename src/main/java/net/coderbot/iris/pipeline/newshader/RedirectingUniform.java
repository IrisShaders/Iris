package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.math.Matrix4f;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RedirectingUniform<T> extends Uniform {
	private final Consumer<T> setup;
	private final TriConsumer<T, T, T> setupFloat;
	private final Runnable upload;

	public RedirectingUniform(String string, int i, int j, ExtendedShader arg, Consumer<T> setup, Runnable upload) {
		super(string, i, j, arg);
		this.setup = setup;
		this.setupFloat = (a, b, c) -> {};
		this.upload = upload;
	}

	public RedirectingUniform(String string, int i, int j, ExtendedShader arg, TriConsumer<T, T, T> setup, Runnable upload) {
		super(string, i, j, arg);
		this.setup = a -> {};
		this.setupFloat = setup;
		this.upload = upload;
	}

	public void accept(T arg) {
		setup.accept(arg);
	}

	public void accept(T arg, T arg2, T arg3) {
		setupFloat.accept(arg, arg2, arg3);
	}

	@Override
	public void upload() {
		if (upload != null) {
			upload.run();
		}
	}
}
