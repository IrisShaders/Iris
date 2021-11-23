package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkProgram;
import net.coderbot.iris.texunits.TextureUnit;
import org.lwjgl.opengl.GL20C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.FloatBuffer;

/**
 * Modifies {@link ChunkProgram} to handle cases where uniforms might not be present in the target program, and to use
 * the correct texture units for terrain and lightmap textures.
 */
@Mixin(ChunkProgram.class)
public class MixinChunkProgram {
    @ModifyConstant(method = "setup", remap = false, constant = @Constant(intValue = 0))
    private int iris$replaceTerrainTextureUnit(int unit) {
        return TextureUnit.TERRAIN.getSamplerId();
    }

    @ModifyConstant(method = "setup", remap = false, constant = @Constant(intValue = 2))
    private int iris$replaceLightmapTextureUnit(int unit) {
        return TextureUnit.LIGHTMAP.getSamplerId();
    }

    @Redirect(method = "setup", remap = false,
            at = @At(value = "INVOKE", target = "org/lwjgl/opengl/GL20C.glUniform1i (II)V", remap = false))
    private void iris$redirectUniform1i(int location, int value) {
        if (location == -1) {
            return;
        }

        GL20C.glUniform1i(location, value);
    }

    @Redirect(method = "setup", remap = false,
            at = @At(value = "INVOKE", target = "org/lwjgl/opengl/GL20C.glUniform3f (IFFF)V", remap = false))
    private void iris$redirectUniform3f(int location, float v1, float v2, float v3) {
        if (location == -1) {
            return;
        }

        GL20C.glUniform3f(location, v1, v2, v3);
    }

    @Redirect(method = "setup", remap = false,
            at = @At(value = "INVOKE", target = "org/lwjgl/opengl/GL20C.glUniform2f (IFF)V", remap = false))
    private void iris$redirectUniform2f(int location, float v1, float v2) {
        if (location == -1) {
            return;
        }

        GL20C.glUniform2f(location, v1, v2);
    }

    @Redirect(method = "setup", remap = false,
            at = @At(value = "INVOKE",
                    target = "org/lwjgl/opengl/GL20C.glUniformMatrix4fv (IZLjava/nio/FloatBuffer;)V",
                    remap = false))
    private void iris$redirectUniformMatrix4fv(int location, boolean transpose, FloatBuffer buffer) {
        if (location == -1) {
            return;
        }

        GL20C.glUniformMatrix4fv(location, transpose, buffer);
    }
}
