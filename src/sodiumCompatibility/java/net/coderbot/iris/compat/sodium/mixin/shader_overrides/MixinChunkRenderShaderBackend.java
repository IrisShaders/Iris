package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderConstants;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkProgram;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkRenderShaderBackend;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ChunkRenderBackendExt;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
	protected ChunkProgram activeProgram;

	@Shadow
	public void begin(PoseStack poseStack) {
		throw new AssertionError();
	}

	@Shadow
	@Final
	protected ChunkVertexType vertexType;

	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
	private void iris$onInit(ChunkVertexType vertexType, CallbackInfo ci) {
		irisChunkProgramOverrides = new IrisChunkProgramOverrides();
	}

	@Redirect(method = "createShader", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/gl/shader/ShaderLoader;loadShader(Lme/jellysquid/mods/sodium/client/gl/device/RenderDevice;Lme/jellysquid/mods/sodium/client/gl/shader/ShaderType;Lnet/minecraft/resources/ResourceLocation;Ljava/util/List;)Lme/jellysquid/mods/sodium/client/gl/shader/GlShader;", ordinal = 0))
	private GlShader iris$redirectOriginalShader(RenderDevice device, ShaderType type, ResourceLocation name, List<String> constants) {
		if (this.vertexType == IrisModelVertexFormats.MODEL_VERTEX_XHFP) {
			String shader = getShaderSource(getShaderPath(name));
			shader = shader.replace("v_LightCoord = a_LightCoord", "v_LightCoord = (iris_LightmapTextureMatrix * vec4(a_LightCoord, 0, 1)).xy");

			StringTransformations transformations = new StringTransformations(shader);

			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "mat4 iris_LightmapTextureMatrix = mat4(vec4(0.00390625, 0.0, 0.0, 0.0), vec4(0.0, 0.00390625, 0.0, 0.0), vec4(0.0, 0.0, 0.00390625, 0.0), vec4(0.03125, 0.03125, 0.03125, 1.0));");

			return new GlShader(device, type, name, transformations.toString(), ShaderConstants.fromStringList(constants));
		} else {
			return ShaderLoader.loadShader(device, type, name, constants);
		}
	}

	private static String getShaderPath(ResourceLocation name) {
		return String.format("/assets/%s/shaders/%s", name.getNamespace(), name.getPath());
	}

	private static String getShaderSource(String path) {
		try {
			InputStream in = ShaderLoader.class.getResourceAsStream(path);
			Throwable var2 = null;

			String var3;
			try {
				if (in == null) {
					throw new RuntimeException("Shader not found: " + path);
				}

				var3 = IOUtils.toString(in, StandardCharsets.UTF_8);
			} catch (Throwable var13) {
				var2 = var13;
				throw var13;
			} finally {
				if (in != null) {
					if (var2 != null) {
						try {
							in.close();
						} catch (Throwable var12) {
							var2.addSuppressed(var12);
						}
					} else {
						in.close();
					}
				}

			}

			return var3;
		} catch (IOException var15) {
			throw new RuntimeException("Could not read shader sources", var15);
		}
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

		Iris.getPipelineManager().getPipeline().ifPresent(WorldRenderingPipeline::beginSodiumTerrainRendering);
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
		ProgramSamplers.clearActiveSamplers();
		Iris.getPipelineManager().getPipeline().ifPresent(WorldRenderingPipeline::endSodiumTerrainRendering);
	}

	@Inject(method = "delete", at = @At("HEAD"), remap = false)
	private void iris$onDelete(CallbackInfo ci) {
		irisChunkProgramOverrides.deleteShaders();
	}
}
