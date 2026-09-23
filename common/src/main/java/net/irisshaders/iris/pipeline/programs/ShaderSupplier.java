package net.irisshaders.iris.pipeline.programs;

import java.util.function.Supplier;

public record ShaderSupplier(ShaderKey key, Supplier<ExtendedShader> shader) {
}
