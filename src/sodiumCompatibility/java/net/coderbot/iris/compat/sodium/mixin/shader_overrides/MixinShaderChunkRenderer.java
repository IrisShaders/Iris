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
public abstract class MixinShaderChunkRenderer implements ShaderChunkRendererExt {
    @Unique
    private IrisChunkProgramOverrides irisChunkProgramOverrides;

    @Shadow(remap = false)
	@Final
	protected TerrainVertexType vertexType;

	@Shadow
	@Final
	protected RenderDevice device;

	@Shadow
	protected abstract Program<ChunkShaderInterface> getProgram(ChunkRenderPass pass);

	@Shadow
	private static ShaderConstants getShaderConstants(ChunkRenderPass pass, TerrainVertexType vertexType) {
		return null;
	}

	@Shadow
	public abstract void delete();

	@Shadow
	protected abstract Pipeline<ChunkShaderInterface, ShaderChunkRenderer.BufferTarget> createPipeline(ChunkRenderPass pass);

	@Shadow
	@Final
	private Map<ChunkRenderPass, Program<ChunkShaderInterface>> programs;

	@Shadow
	@Final
	private Map<ChunkRenderPass, Pipeline<ChunkShaderInterface, ShaderChunkRenderer.BufferTarget>> pipelines;

	@Shadow
	@Final
	protected VertexFormat<TerrainMeshAttribute> vertexFormat;
	@Unique
	private final Map<ChunkRenderPass, Pipeline<ChunkShaderInterface, ShaderChunkRenderer.BufferTarget>> shadowPipelines = new Object2ObjectOpenHashMap();

	@Unique
	private final Map<ChunkRenderPass, Program<ChunkShaderInterface>> shadowPrograms = new Object2ObjectOpenHashMap();

	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void iris$onInit(RenderDevice device, TerrainVertexType vertexType, CallbackInfo ci) {
        irisChunkProgramOverrides = new IrisChunkProgramOverrides();
    }

	/**
	 * @author IMS
	 */
	@Overwrite(remap = false)
	protected Pipeline<ChunkShaderInterface, ShaderChunkRenderer.BufferTarget> getPipeline(ChunkRenderPass pass) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return this.shadowPipelines.computeIfAbsent(pass, this::createShadowPipeline);
		} else {
			return this.pipelines.computeIfAbsent(pass, this::createPipeline);
		}
	}

	private Pipeline<ChunkShaderInterface, ShaderChunkRenderer.BufferTarget> createShadowPipeline(ChunkRenderPass pass) {
		Program<ChunkShaderInterface> program = this.getIrisProgram(true, pass);
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

	/**
	 * @author IMS
	 */
	@Overwrite(remap = false)
	private Program<ChunkShaderInterface> createProgram(ChunkRenderPass pass) {
		return this.getIrisProgram(false, pass);
	}

	private Program<ChunkShaderInterface> getIrisProgram(boolean isShadowPass, ChunkRenderPass pass) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return irisChunkProgramOverrides.getProgramOverride(isShadowPass, device, pass, vertexType);
		} else {
			ShaderConstants constants = getShaderConstants(pass, this.vertexType);
			String vertShader = ShaderParser.parseShader(ShaderLoader.MINECRAFT_ASSETS, new ResourceLocation("sodium", "blocks/block_layer_opaque.vsh"), constants);
			String fragShader = ShaderParser.parseShader(ShaderLoader.MINECRAFT_ASSETS, new ResourceLocation("sodium", "blocks/block_layer_opaque.fsh"), constants);
			ShaderDescription desc = ShaderDescription.builder().addShaderSource(ShaderType.VERTEX, vertShader).addShaderSource(ShaderType.FRAGMENT, fragShader).addAttributeBinding("a_Position", 1).addAttributeBinding("a_Color", 2).addAttributeBinding("a_TexCoord", 3).addAttributeBinding("a_LightCoord", 4).addFragmentBinding("fragColor", 0).build();
			return this.device.createProgram(desc, ChunkShaderInterface::new);
		}
	}

	@Redirect(method = "delete", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/gfx/api/device/RenderDevice;deleteProgram(Lnet/caffeinemc/gfx/api/shader/Program;)V"))
	private void checkHandleFirst(RenderDevice instance, Program program) {
		if (((GlObjectExt) program).isHandleValid()) {
			instance.deleteProgram(program);
		}
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

	@Override
	public IrisChunkProgramOverrides iris$getOverrides() {
		return irisChunkProgramOverrides;
	}
}
