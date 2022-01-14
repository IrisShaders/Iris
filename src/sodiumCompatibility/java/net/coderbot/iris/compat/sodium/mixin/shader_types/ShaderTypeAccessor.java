package net.coderbot.iris.compat.sodium.mixin.shader_types;

import me.jellysquid.mods.sodium.opengl.shader.ShaderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShaderType.class)
public interface ShaderTypeAccessor {
    @Invoker(value = "<init>")
    static ShaderType createShaderType(String name, int ordinal, int glId) {
        throw new AssertionError();
    }
}
