package net.irisshaders.iris.pipeline.programs;

import net.minecraft.client.renderer.CompiledShaderProgram;

import java.util.function.Supplier;

public record ShaderSupplier(int id, Supplier<CompiledShaderProgram> shader) {
}
