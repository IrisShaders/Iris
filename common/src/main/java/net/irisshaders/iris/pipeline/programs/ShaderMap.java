package net.irisshaders.iris.pipeline.programs;

import java.util.function.Consumer;
import java.util.function.Function;

public class ShaderMap {
	private final ExtendedShader[] shaders = new ExtendedShader[ShaderKey.values().length];

	public ShaderMap(ShaderLoadingMap loadingMap, Function<ShaderSupplier, Boolean> deletionFunction,
		Consumer<ExtendedShader> programConsumer) {
		loadingMap.forAllShaders((key, supplier) -> {
			if (supplier == null || deletionFunction.apply(supplier)) {
				return;
			}

			var program = supplier.shader().get();
			this.shaders[key.ordinal()] = program;
			programConsumer.accept(program);
		});
	}

	public ExtendedShader getShader(ShaderKey key) {
		return this.shaders[key.ordinal()];
	}
}
