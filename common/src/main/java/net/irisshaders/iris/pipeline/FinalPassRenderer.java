package net.irisshaders.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.irisshaders.iris.pipeline.programs.CompositeProgram;
import net.irisshaders.iris.pipeline.programs.ComputeProgram;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.samplers.CustomTextureSamplerInterceptor;
import net.irisshaders.iris.samplers.IrisImages;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import net.irisshaders.iris.shaderpack.properties.ProgramDirectives;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.targets.CustomImage;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class FinalPassRenderer {
	private final RenderTargets renderTargets;

	@Nullable
	private final Pass finalPass;
	private final CompositeProgram fallbackProgram;
	private final ImmutableList<SwapPass> swapPasses;
	private final Object2ObjectMap<String, TextureAccess> irisCustomTextures;
	private final Set<CustomImage> customImages;
	private final TextureAccess noiseTexture;
	private final CenterDepthSampler centerDepthSampler;
	private final Object2ObjectMap<String, TextureAccess> customTextureIds;
	private final CustomUniforms customUniforms;
	private final IrisRenderingPipeline pipeline;

	public FinalPassRenderer(IrisRenderingPipeline pipeline, ProgramSet pack, RenderTargets renderTargets, TextureAccess noiseTexture,
		ShaderStorageBufferHolder holder, FrameUpdateNotifier updateNotifier, ImmutableSet<Integer> flippedBuffers,
		CenterDepthSampler centerDepthSampler, Supplier<ShadowRenderTargets> shadowTargetsSupplier,
		Object2ObjectMap<String, TextureAccess> customTextureIds, Object2ObjectMap<String, TextureAccess> irisCustomTextures,
		Set<CustomImage> customImages, ImmutableSet<Integer> flippedAtLeastOnce, CustomUniforms customUniforms) {
		this.pipeline = pipeline;
		this.centerDepthSampler = centerDepthSampler;
		this.customTextureIds = customTextureIds;
		this.irisCustomTextures = irisCustomTextures;
		this.customImages = customImages;

		final PackRenderTargetDirectives renderTargetDirectives = pack.getPackDirectives().getRenderTargetDirectives();
		final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargetSettings = renderTargetDirectives.getRenderTargetSettings();

		this.noiseTexture = noiseTexture;
		this.renderTargets = renderTargets;
		this.customUniforms = customUniforms;
		this.finalPass = pack.get(ProgramId.Final).map(source -> {
			Pass pass = new Pass();
			ProgramDirectives directives = source.getDirectives();

			pass.program = createProgram(source, flippedBuffers, flippedAtLeastOnce, shadowTargetsSupplier);
			pass.computes = createComputes(pack.getFinalCompute(), flippedBuffers, flippedAtLeastOnce, shadowTargetsSupplier, holder);
			pass.stageReadsFromAlt = flippedBuffers;
			pass.mipmappedBuffers = directives.getMipmappedBuffers();

			return pass;
		}).orElse(null);

		if (this.finalPass == null) {
			var outputs = new ColorAttachmentPlan.Builder().program("final_copy", new int[]{0}, Set.of()).build(1);
			this.fallbackProgram = new CompositeProgram(pipeline.getPackSamplers(), "final_copy", Map.of(PatchShaderType.VERTEX,
				"#version 330\nin vec3 Position;in vec2 UV0;out vec2 uv;void main(){gl_Position=vec4(Position.xy*2.0-1.0,0,1);" + "uv=UV0;}",
				PatchShaderType.FRAGMENT,
				"#version 330\nin vec2 uv;uniform sampler2D colortex0;layout(location=0)out vec4 color;void " + "main(){color=texture(colortex0,uv);}"),
				outputs.program("final_copy"), Collections.singletonList(null));
			IrisSamplers.addRenderTargetSamplers(this.fallbackProgram.getSamplers(), () -> flippedBuffers, renderTargets, true, pipeline);
			this.fallbackProgram.getSamplers().finish();
		} else {
			this.fallbackProgram = null;
		}

		IntList buffersToBeCleared = pack.getPackDirectives().getRenderTargetDirectives().getBuffersToBeCleared();

		ImmutableList.Builder<SwapPass> swapPasses = ImmutableList.builder();

		flippedBuffers.forEach((i) -> {
			int target = i;

			if (buffersToBeCleared.contains(target)) {
				return;
			}

			SwapPass swap = new SwapPass();
			RenderTarget target1 = renderTargets.getOrCreate(target);
			swap.target = target;

			swapPasses.add(swap);
		});

		this.swapPasses = swapPasses.build();
	}

	private static void setupMipmapping(RenderTarget target, boolean readFromAlt) {
		if (target == null) {
			return;
		}

		target.generateMipmaps(readFromAlt);
	}

	private static void resetRenderTarget(RenderTarget target) {
		if (target == null) {
			return;
		}

		target.turnOffMips(true);
		target.turnOffMips(false);

	}

	public void prepare() {
		if (this.finalPass != null) {
			for (ComputeProgram compute : this.finalPass.computes) {
				if (compute != null) {
					compute.prepare();
				}
			}
		}
		var main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		var program = this.finalPass == null ? this.fallbackProgram : this.finalPass.program;
		program.prepare(List.of(main.getColorTextureView()));
	}

	public void renderFinalPass() {
		var main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		var program = this.finalPass == null ? this.fallbackProgram : this.finalPass.program;

		if (this.finalPass != null) {
			for (ComputeProgram compute : this.finalPass.computes) {
				if (compute != null) {
					compute.dispatch(main.width, main.height);
				}
			}

			for (int index : this.finalPass.mipmappedBuffers) {
				setupMipmapping(this.renderTargets.get(index), this.finalPass.stageReadsFromAlt.contains(index));
			}
		}

		var level = Minecraft.getInstance().level;
		program.draw(List.of(main.getColorTextureView()), ViewportData.defaultValue(), level == null ? 0 : level.getGameTime(),
			SystemTimeUniforms.COUNTER.getAsInt());

		for (int i = 0; i < renderTargets.getRenderTargetCount(); i++) {
			resetRenderTarget(renderTargets.get(i));
		}

		for (SwapPass swap : this.swapPasses) {
			var target = this.renderTargets.get(swap.target);
			Iris.getRenderBackend().commands()
				.copyTexture(target.getTextureView(true, false).texture(), target.getTextureView(false, false).texture(), target.getWidth(),
					target.getHeight());
		}
	}

	public void recalculateSwapPassSize() {
	}

	private CompositeProgram createProgram(ProgramSource source, ImmutableSet<Integer> flipped,
		ImmutableSet<Integer> flippedAtLeastOnceSnapshot, Supplier<ShadowRenderTargets> shadowTargetsSupplier) {

		Map<PatchShaderType, String> transformed = TransformPatcher.patchComposite(source.getName(),
			source.getVertexSource().orElseThrow(NullPointerException::new), source.getGeometrySource().orElse(null),
			source.getFragmentSource().orElseThrow(NullPointerException::new), TextureStage.COMPOSITE_AND_FINAL, pipeline.getTextureMap(),
			pipeline.getTextureOverrides(TextureStage.COMPOSITE_AND_FINAL));

		var outputs = new ColorAttachmentPlan.Builder().program(source.getName(), new int[]{0}, Set.of()).build(1);
		var program = new CompositeProgram(this.pipeline.getPackSamplers(), source.getName(), transformed,
			outputs.program(source.getName()), Collections.singletonList(null));
		var builder = program.getSamplers();
		builder.addStorageBuffers(this.pipeline.getStorageBuffers());

		program.getUniforms().useFrameUniforms(this.pipeline.getFrameUniforms());
		CommonUniforms.addDynamicUniforms(program.getUniforms(), FogMode.OFF);

		CustomTextureSamplerInterceptor customTextureSamplerInterceptor = new CustomTextureSamplerInterceptor(builder,
			this.customTextureIds, flippedAtLeastOnceSnapshot);

		IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, () -> flipped, renderTargets, true, pipeline);
		IrisSamplers.addCustomImages(customTextureSamplerInterceptor, customImages);
		IrisImages.addCustomImages(builder, customImages);
		IrisImages.addRenderTargetImages(builder, () -> flipped, this.renderTargets);

		if (IrisImages.hasShadowImages(builder)) {
			IrisImages.addShadowColorImages(builder, shadowTargetsSupplier.get(), null);
		}

		IrisSamplers.addCustomTextures(builder, irisCustomTextures);
		IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, noiseTexture);
		IrisSamplers.addCompositeSamplers(customTextureSamplerInterceptor, renderTargets);

		if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
			IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowTargetsSupplier.get(), null,
				pipeline.hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
		}

		this.centerDepthSampler.setUsage(builder.addTextureSampler(
			new SamplerHolder.TextureBinding("iris_centerDepthSmooth", this.centerDepthSampler::getCenterDepthTextureView,
				() -> GlSampler.NEAREST), false, "iris_centerDepthSmooth"));
		builder.finish();

		return program;
	}

	private ComputeProgram[] createComputes(ComputeSource[] compute, ImmutableSet<Integer> flipped,
		ImmutableSet<Integer> flippedAtLeastOnceSnapshot, Supplier<ShadowRenderTargets> shadowTargetsSupplier,
		ShaderStorageBufferHolder holder) {
		ComputeProgram[] programs = new ComputeProgram[compute.length];
		for (int i = 0; i < programs.length; i++) {
			ComputeSource source = compute[i];
			if (source == null || source.getSource().isEmpty()) {
			} else {

				Objects.requireNonNull(flipped);

				ProgramResources builder;
				ComputeProgram program;

				try {
					String transformed = TransformPatcher.patchCompute(source.getName(), source.getSource().orElse(null),
						TextureStage.COMPOSITE_AND_FINAL, pipeline.getTextureMap(),
						pipeline.getTextureOverrides(TextureStage.COMPOSITE_AND_FINAL));

					program = new ComputeProgram(this.pipeline.getPackSamplers(), source.getName(), transformed);
					builder = program.getSamplers();
					builder.addStorageBuffers(this.pipeline.getStorageBuffers());
				} catch (ShaderCompileException e) {
					throw e;
				} catch (RuntimeException e) {

					throw new RuntimeException("Shader compilation failed for final compute " + source.getName() + "!", e);
				}

				CustomTextureSamplerInterceptor customTextureSamplerInterceptor = new CustomTextureSamplerInterceptor(builder,
					this.customTextureIds, flippedAtLeastOnceSnapshot);
				program.getUniforms().useFrameUniforms(this.pipeline.getFrameUniforms());
				CommonUniforms.addDynamicUniforms(program.getUniforms(), FogMode.OFF);

				IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, () -> flipped, renderTargets, true, pipeline);
				IrisSamplers.addCustomTextures(builder, irisCustomTextures);
				IrisSamplers.addCustomImages(customTextureSamplerInterceptor, customImages);

				IrisImages.addRenderTargetImages(builder, () -> flipped, renderTargets);
				IrisImages.addCustomImages(builder, customImages);

				IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, noiseTexture);
				IrisSamplers.addCompositeSamplers(customTextureSamplerInterceptor, renderTargets);

				if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
					IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowTargetsSupplier.get(), null,
						pipeline.hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
					IrisImages.addShadowColorImages(builder, shadowTargetsSupplier.get(), null);
				}

				centerDepthSampler.setUsage(
					builder.addDynamicSampler(centerDepthSampler::getCenterDepthTexture, GlSampler.NEAREST, "iris_centerDepthSmooth"));

				builder.finish();
				programs[i] = program;

				programs[i].setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups(),
					FilledIndirectPointer.basedOff(holder, source.getIndirectPointer()));
			}
		}

		return programs;
	}

	public void destroy() {
		if (finalPass != null) {
			finalPass.destroy();
		}

		if (this.fallbackProgram != null) {
			this.fallbackProgram.destroy();
		}
	}

	private static final class Pass {
		ComputeProgram[] computes;
		ImmutableSet<Integer> stageReadsFromAlt;
		ImmutableSet<Integer> mipmappedBuffers;
		private CompositeProgram program;

		private void destroy() {
			this.program.destroy();

			for (var compute : this.computes) {
				if (compute != null) {
					compute.destroy();
				}
			}
		}
	}

	private static final class SwapPass {
		public int target;
	}
}
