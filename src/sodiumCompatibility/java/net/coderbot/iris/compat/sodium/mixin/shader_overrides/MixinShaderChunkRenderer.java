package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderConstants;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderParser;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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

	@Redirect(method = "createShader", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/gl/shader/ShaderLoader;loadShader(Lme/jellysquid/mods/sodium/client/gl/shader/ShaderType;Lnet/minecraft/resources/ResourceLocation;Lme/jellysquid/mods/sodium/client/gl/shader/ShaderConstants;)Lme/jellysquid/mods/sodium/client/gl/shader/GlShader;", ordinal = 0))
	private GlShader iris$redirectOriginalShader(ShaderType type, ResourceLocation name, ShaderConstants constants) {
		if (this.vertexType == IrisModelVertexFormats.MODEL_VERTEX_XHFP) {
			String shader = ShaderLoader.getShaderSource(name);
			shader = shader.replace("v_LightCoord = _vert_tex_light_coord", "v_LightCoord = (iris_LightmapTextureMatrix * vec4(_vert_tex_light_coord, 0, 1)).xy");

			StringTransformations transformations = new StringTransformations(shader);

			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "mat4 iris_LightmapTextureMatrix = mat4(vec4(0.00390625, 0.0, 0.0, 0.0), vec4(0.0, 0.00390625, 0.0, 0.0), vec4(0.0, 0.0, 0.00390625, 0.0), vec4(0.03125, 0.03125, 0.03125, 1.0));");

			return new GlShader(type, name, ShaderParser.parseShader(transformations.toString(), constants));
		} else {
			return ShaderLoader.loadShader(type, name, constants);
		}
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
		ProgramSamplers.clearActiveSamplers();
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
