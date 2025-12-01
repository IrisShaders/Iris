package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.opengl.GlProgram;

import java.util.function.Supplier;

public record ShaderSupplier(ShaderKey key, PartialShader id, Supplier<GlProgram> shader) {
}
