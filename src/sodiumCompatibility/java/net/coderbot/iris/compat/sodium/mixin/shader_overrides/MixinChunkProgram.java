package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkProgram;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.FloatBuffer;

/**
 * Modifies {@link ChunkProgram} to handle cases where uniforms might not be present in the target program.
 */
@Mixin(ChunkProgram.class)
public class MixinChunkProgram {
    @Redirect(method = "setup", remap = false,
            at = @At(value = "INVOKE", target = "org/lwjgl/opengl/GL20C.glUniform1i (II)V", remap = false))
    private void iris$redirectUniform1i(int location, int value) {
        if (location == -1) {
            return;
        }

        IrisRenderSystem.uniform1i(location, value);
    }

    @Redirect(method = "setup", remap = false,
            at = @At(value = "INVOKE", target = "org/lwjgl/opengl/GL20C.glUniform3f (IFFF)V", remap = false))
    private void iris$redirectUniform3f(int location, float v1, float v2, float v3) {
        if (location == -1) {
            return;
        }

        IrisRenderSystem.uniform3f(location, v1, v2, v3);
    }

    @Redirect(method = "setup", remap = false,
            at = @At(value = "INVOKE", target = "org/lwjgl/opengl/GL20C.glUniform2f (IFF)V", remap = false))
    private void iris$redirectUniform2f(int location, float v1, float v2) {
        if (location == -1) {
            return;
        }

        IrisRenderSystem.uniform2f(location, v1, v2);
    }

    @Redirect(method = "setup", remap = false,
            at = @At(value = "INVOKE",
                    target = "org/lwjgl/opengl/GL20C.glUniformMatrix4fv (IZLjava/nio/FloatBuffer;)V",
                    remap = false))
    private void iris$redirectUniformMatrix4fv(int location, boolean transpose, FloatBuffer buffer) {
        if (location == -1) {
            return;
        }

        IrisRenderSystem.uniformMatrix4fv(location, transpose, buffer);
    }
}
