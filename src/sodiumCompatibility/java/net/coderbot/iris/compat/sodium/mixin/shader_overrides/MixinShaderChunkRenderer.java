package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.gfx.opengl.shader.GlProgram;
import net.caffeinemc.sodium.render.chunk.draw.ShaderChunkRenderer;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
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
	@Final
	protected TerrainVertexType vertexType;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void iris$onInit(RenderDevice device, TerrainVertexType vertexType, CallbackInfo ci) {
        irisChunkProgramOverrides = new IrisChunkProgramOverrides();
    }

	@Inject(method = "begin", at = @At("HEAD"), cancellable = true, remap = false)
	private void iris$begin(BlockRenderPass pass, CallbackInfo ci) {
		this.override = irisChunkProgramOverrides.getProgramOverride(pass, this.vertexType);

		irisChunkProgramOverrides.bindFramebuffer(pass);

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
	}

    @Inject(method = "end", at = @At("HEAD"), remap = false, cancellable = true)
    private void iris$onEnd(CallbackInfo ci) {
        ProgramUniforms.clearActiveUniforms();
		irisChunkProgramOverrides.unbindFramebuffer();

        if (override != null) {
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
