package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkProgram;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderFogComponent;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class IrisChunkProgram extends ChunkProgram {
    // Uniform variable binding indexes
    private final int uModelViewMatrix;
    private final int uNormalMatrix;

    @Nullable
    private final ProgramUniforms irisProgramUniforms;

    @Nullable
    private final ProgramSamplers irisProgramSamplers;

    public IrisChunkProgram(RenderDevice owner, ResourceLocation name, int handle,
							@Nullable ProgramUniforms irisProgramUniforms, @Nullable ProgramSamplers irisProgramSamplers) {
        super(owner, name, handle, ChunkShaderFogComponent.None::new);
        this.uModelViewMatrix = this.getUniformLocation("u_ModelViewMatrix");
        this.uNormalMatrix = this.getUniformLocation("u_NormalMatrix");
        this.irisProgramUniforms = irisProgramUniforms;
        this.irisProgramSamplers = irisProgramSamplers;
    }

    public void setup(PoseStack poseStack, float modelScale, float textureScale) {
        super.setup(poseStack, modelScale, textureScale);

        if (irisProgramUniforms != null) {
            irisProgramUniforms.update();
        }

        if (irisProgramSamplers != null) {
            irisProgramSamplers.update();
        }

        Matrix4f modelViewMatrix = poseStack.last().pose();
        Matrix4f normalMatrix = poseStack.last().pose().copy();
        normalMatrix.invert();
        normalMatrix.transpose();

        uniformMatrix(uModelViewMatrix, modelViewMatrix);
        uniformMatrix(uNormalMatrix, normalMatrix);
    }

    @Override
    public int getUniformLocation(String name) {
        // NB: We pass through calls involving u_ModelViewProjectionMatrix, u_ModelScale, and u_TextureScale, since
        //     currently patched Iris shader programs use those.

        if ("u_BlockTex".equals(name) || "u_LightTex".equals(name)) {
            // Not relevant for Iris shader programs
            return -1;
        }

        try {
            return super.getUniformLocation(name);
        } catch (NullPointerException e) {
            // Suppress getUniformLocation
            return -1;
        }
    }

    private void uniformMatrix(int location, Matrix4f matrix) {
        if (location == -1) {
            return;
        }

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FloatBuffer buffer = memoryStack.mallocFloat(16);

            matrix.store(buffer);

            GL20C.glUniformMatrix4fv(location, false, buffer);
        }
    }
}
