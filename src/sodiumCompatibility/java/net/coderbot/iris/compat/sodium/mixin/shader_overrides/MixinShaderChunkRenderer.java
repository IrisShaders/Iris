package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.caffeinemc.gfx.api.array.VertexArrayDescription;
import net.caffeinemc.gfx.api.array.VertexArrayResourceBinding;
import net.caffeinemc.gfx.api.array.attribute.VertexAttributeBinding;
import net.caffeinemc.gfx.api.array.attribute.VertexFormat;
import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.gfx.api.pipeline.Pipeline;
import net.caffeinemc.gfx.api.shader.Program;
import net.caffeinemc.gfx.api.shader.ShaderDescription;
import net.caffeinemc.gfx.api.shader.ShaderType;
import net.caffeinemc.sodium.render.chunk.draw.ShaderChunkRenderer;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.sodium.render.shader.ShaderConstants;
import net.caffeinemc.sodium.render.shader.ShaderLoader;
import net.caffeinemc.sodium.render.shader.ShaderParser;
import net.caffeinemc.sodium.render.terrain.format.TerrainMeshAttribute;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.GlObjectExt;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

/**
 * Overrides shaders in {@link ShaderChunkRenderer} with our own as needed.
 */
@Mixin(ShaderChunkRenderer.class)
public abstract class MixinShaderChunkRenderer<T> implements ShaderChunkRendererExt {
	@Shadow
	@Final
	protected RenderDevice device;

	@Shadow
	@Final
	private Map<ChunkRenderPass, Pipeline<T, ShaderChunkRenderer.BufferTarget>> pipelines;

	@Shadow
	@Final
	private Map<ChunkRenderPass, Program<T>> programs;

	@Shadow
	protected abstract Pipeline<T, ShaderChunkRenderer.BufferTarget> createPipeline(ChunkRenderPass pass);

	@Shadow
	@Final
	protected VertexFormat<TerrainMeshAttribute> vertexFormat;
	@Unique
	private final Map<ChunkRenderPass, Pipeline<T, ShaderChunkRenderer.BufferTarget>> shadowPipelines = new Object2ObjectOpenHashMap();

	@Unique
	private final Map<ChunkRenderPass, Program<T>> shadowPrograms = new Object2ObjectOpenHashMap();

	@Redirect(method = "delete", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/gfx/api/device/RenderDevice;deleteProgram(Lnet/caffeinemc/gfx/api/shader/Program;)V"))
	private void checkHandleFirst(RenderDevice instance, Program program) {
		if (((GlObjectExt) program).isHandleValid()) {
			instance.deleteProgram(program);
		}
	}

	@Unique
	private IrisChunkProgramOverrides<ChunkShaderInterface> irisChunkProgramOverrides;

	@Shadow
	public abstract void delete();

	@Shadow
	@Final
	protected TerrainVertexType vertexType;

	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
	private void iris$onInit(RenderDevice device, TerrainVertexType vertexType, CallbackInfo ci) {
		irisChunkProgramOverrides = new IrisChunkProgramOverrides<>();
	}

	/**
	 * @author
	 */
	@Overwrite(remap = false)
	protected Pipeline<T, ShaderChunkRenderer.BufferTarget> getPipeline(ChunkRenderPass pass) {
		if (Iris.getPipelineManager().isSodiumShaderReloadNeeded()) {
			for (var pipeline : this.pipelines.values()) {
				this.device.deletePipeline(pipeline);
			}

			this.pipelines.clear();

			for (var program : this.programs.values()) {
				if (((GlObjectExt) program).isHandleValid()) {
					this.device.deleteProgram(program);
				}
			}

			this.programs.clear();

			deletePrograms();
		}

		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return this.shadowPipelines.computeIfAbsent(pass, this::createShadowPipeline);
		} else {
			return this.pipelines.computeIfAbsent(pass, this::createPipeline);
		}
	}

	private Pipeline<T, ShaderChunkRenderer.BufferTarget> createShadowPipeline(ChunkRenderPass pass) {
		Program<T> program = this.getIrisProgram(true, pass);
		VertexArrayDescription<ShaderChunkRenderer.BufferTarget> vertexArray = new VertexArrayDescription(ShaderChunkRenderer.BufferTarget.values(), List.of(new VertexArrayResourceBinding(ShaderChunkRenderer.BufferTarget.VERTICES, new VertexAttributeBinding[]{new VertexAttributeBinding(1, this.vertexFormat.getAttribute(TerrainMeshAttribute.POSITION)), new VertexAttributeBinding(2, this.vertexFormat.getAttribute(TerrainMeshAttribute.COLOR)), new VertexAttributeBinding(3, this.vertexFormat.getAttribute(TerrainMeshAttribute.BLOCK_TEXTURE)), new VertexAttributeBinding(4, this.vertexFormat.getAttribute(TerrainMeshAttribute.LIGHT_TEXTURE)),new VertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
			this.vertexFormat.getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
			new VertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
			new VertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.TANGENT)),
			new VertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL))})));
		return this.device.createPipeline(pass.pipelineDescription(), program, vertexArray);
	}

	@Inject(method = "delete", at = @At("HEAD"), remap = false)
	private void iris$onDelete(CallbackInfo ci) {
		deletePrograms();
	}

	private void deletePrograms() {
		for (var pipeline : this.shadowPipelines.values()) {
			this.device.deletePipeline(pipeline);
		}

		this.shadowPipelines.clear();

		for (var program : this.shadowPrograms.values()) {
			if (((GlObjectExt) program).isHandleValid()) {
				this.device.deleteProgram(program);
			}
		}

		this.shadowPrograms.clear();

		irisChunkProgramOverrides.deleteShaders(this.device);
	}

	public Program<T> getIrisProgram(boolean isShadowPass, ChunkRenderPass pass) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			// todo: terrible cast
			return (Program<T>) irisChunkProgramOverrides.getProgramOverride(isShadowPass, device, pass, vertexType);
		} else {
			return null;
		}
	}
}
