package net.irisshaders.iris.pipeline.programs;

import java.util.function.Consumer;
import java.util.function.Function;

public class ShaderMap {
	private final WorldProgram[] shaders = new WorldProgram[ShaderKey.values().length];

	public ShaderMap(ShaderLoadingMap loadingMap, Function<ShaderSupplier, Boolean> deletionFunction,
		Consumer<WorldProgram> programConsumer) {
		loadingMap.forAllShaders((key, supplier) -> {
			if (supplier == null || deletionFunction.apply(supplier)) {
				return;
			}

			var program = supplier.shader().get();
			shaders[key.ordinal()] = program;
			programConsumer.accept(program);
		});
	}

	public WorldProgram getShader(ShaderKey key) {
		return shaders[key.ordinal()];
	}
}
