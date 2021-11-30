package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overrides shaders in {@link ShaderChunkRenderer} with our own as needed.
 */
@Mixin(ShaderChunkRenderer.class)
public class MixinShaderChunkRenderer implements ShaderChunkRendererExt {
    @Unique
    private IrisChunkProgramOverrides irisChunkProgramOverrides;

    @Unique
    private GlProgram<IrisChunkShaderInterface> override;

    @Shadow(remap = false)
    private GlProgram<ChunkShaderInterface> activeProgram;

    @Shadow(remap = false)
	@Final
	protected ChunkVertexType vertexType;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void iris$onInit(RenderDevice device, ChunkVertexType vertexType, CallbackInfo ci) {
        irisChunkProgramOverrides = new IrisChunkProgramOverrides();
    }

	@Inject(method = "begin", at = @At("HEAD"), cancellable = true, remap = false)
	private void iris$begin(BlockRenderPass pass, CallbackInfo ci) {
		this.override = irisChunkProgramOverrides.getProgramOverride(pass, this.vertexType);

		if (this.override == null) {
			return;
		}

		// Override with our own behavior
		ci.cancel();

		// Set a sentinel value here, so we can catch it in RegionChunkRenderer and handle it appropriately.
		activeProgram = null;

		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			// No back face culling during the shadow pass
			// TODO: Hopefully this won't be necessary in the future...
			RenderSystem.disableCull();
		}

		override.bind();
		override.getInterface().setup();

		// TODO: Fallback framebuffers
		irisChunkProgramOverrides.bindFramebuffer(pass);
	}

    @Inject(method = "end", at = @At("HEAD"), remap = false, cancellable = true)
    private void iris$onEnd(CallbackInfo ci) {
        ProgramUniforms.clearActiveUniforms();

        if (override != null) {
        	// TODO: Fallback framebuffer handling
			irisChunkProgramOverrides.unbindFramebuffer();

			override.getInterface().restore();
			override.unbind();
			override = null;
			ci.cancel();
		}
    }

    @Inject(method = "delete", at = @At("HEAD"), remap = false)
    private void iris$onDelete(CallbackInfo ci) {
        irisChunkProgramOverrides.deleteShaders();
    }

	@Override
	public GlProgram<IrisChunkShaderInterface> iris$getOverride() {
		return override;
	}
}
