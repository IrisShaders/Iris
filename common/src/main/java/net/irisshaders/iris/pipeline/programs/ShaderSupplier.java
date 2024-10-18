package net.irisshaders.iris.pipeline.programs;

import net.minecraft.client.renderer.CompiledShaderProgram;

import java.util.function.Supplier;

public record ShaderSupplier(ShaderKey key, int id, Supplier<CompiledShaderProgram> shader) {
}
