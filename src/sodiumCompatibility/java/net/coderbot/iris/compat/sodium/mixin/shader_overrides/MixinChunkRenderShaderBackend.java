package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkProgram;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkRenderShaderBackend;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ChunkRenderBackendExt;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the Iris shader program overrides to Sodium's chunk rendering pipeline.
 */
@Mixin(ChunkRenderShaderBackend.class)
public class MixinChunkRenderShaderBackend implements ChunkRenderBackendExt {
    @Unique
    private IrisChunkProgramOverrides irisChunkProgramOverrides;

    @Unique
    private RenderDevice device;

    @Unique
    private ChunkProgram override;

    @Shadow(remap = false)
    private ChunkProgram activeProgram;

    @Shadow
    private void begin(PoseStack poseStack) {
        throw new AssertionError();
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void iris$onInit(ChunkVertexType vertexType, CallbackInfo ci) {
        irisChunkProgramOverrides = new IrisChunkProgramOverrides();
    }

    @Inject(method = "createShaders", at = @At("HEAD"), remap = false)
    private void iris$onCreateShaders(RenderDevice device, CallbackInfo ci) {
        this.device = device;
		WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipelineNullable();
		SodiumTerrainPipeline sodiumTerrainPipeline = null;

		if (worldRenderingPipeline != null) {
			sodiumTerrainPipeline = worldRenderingPipeline.getSodiumTerrainPipeline();
		}

        irisChunkProgramOverrides.createShaders(sodiumTerrainPipeline, device);
    }

    @Override
    public void iris$begin(PoseStack poseStack, BlockRenderPass pass) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            // No back face culling during the shadow pass
            // TODO: Hopefully this won't be necessary in the future...
            RenderSystem.disableCull();
        }

        this.override = irisChunkProgramOverrides.getProgramOverride(device, pass);

        begin(poseStack);
    }

    @Inject(method = "begin",
            at = @At(value = "FIELD",
                    target = "me/jellysquid/mods/sodium/client/render/chunk/shader/ChunkRenderShaderBackend.activeProgram" +
                                ": Lme/jellysquid/mods/sodium/client/render/chunk/shader/ChunkProgram;",
                    args = "opcode=PUTFIELD",
                    remap = false,
                    shift = At.Shift.AFTER))
    private void iris$applyOverride(PoseStack poseStack, CallbackInfo ci) {
        if (override != null) {
            this.activeProgram = override;
        }
    }

    @Inject(method = "end", at = @At("RETURN"))
    private void iris$onEnd(PoseStack poseStack, CallbackInfo ci) {
        ProgramUniforms.clearActiveUniforms();
    }

    @Inject(method = "delete", at = @At("HEAD"), remap = false)
    private void iris$onDelete(CallbackInfo ci) {
        irisChunkProgramOverrides.deleteShaders();
    }
}
