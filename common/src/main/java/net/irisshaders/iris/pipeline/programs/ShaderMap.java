package net.irisshaders.iris.pipeline.programs;

import net.minecraft.client.renderer.ShaderInstance;

import java.util.function.Function;

/**
 * A specialized map mapping {@link ShaderKey} to {@link ShaderInstance}.
 * Avoids much of the complexity / overhead of an EnumMap while ultimately
 * fulfilling the same function.
 */
public class ShaderMap {
	private final ShaderInstance[] shaders;

	public ShaderMap(Function<ShaderKey, ShaderInstance> factory) {
		ShaderKey[] ids = ShaderKey.values();

		this.shaders = new ShaderInstance[ids.length];

		for (int i = 0; i < ids.length; i++) {
			this.shaders[i] = factory.apply(ids[i]);
		}
	}

	public ShaderInstance getShader(ShaderKey id) {
		return shaders[id.ordinal()];
	}
}
