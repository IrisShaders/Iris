package net.irisshaders.iris.pipeline.programs;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A specialized map mapping {@link ShaderKey} to {@link com.mojang.blaze3d.opengl.GlProgram}.
 * Avoids much of the complexity / overhead of an EnumMap while ultimately
 * fulfilling the same function.
 */
public class ShaderLoadingMap {
	private final ShaderSupplier[] shaders;

	public ShaderLoadingMap(Function<ShaderKey, ShaderSupplier> factory) {
		ShaderKey[] ids = ShaderKey.values();

		this.shaders = new ShaderSupplier[ids.length];

		for (int i = 0; i < ids.length; i++) {
			this.shaders[i] = factory.apply(ids[i]);
		}
	}

	public void forAllShaders(BiConsumer<ShaderKey, ShaderSupplier> consumer) {
		for (int i = 0; i < ShaderKey.values().length; i++) {
			consumer.accept(ShaderKey.values()[i], this.shaders[i]);
		}
	}
}
