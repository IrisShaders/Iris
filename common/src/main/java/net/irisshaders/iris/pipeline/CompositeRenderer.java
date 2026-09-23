package net.irisshaders.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
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
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import net.irisshaders.iris.shaderpack.properties.ProgramDirectives;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.BufferFlipper;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.targets.CustomImage;
import net.irisshaders.iris.targets.RenderFramebuffer;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class CompositeRenderer {
	public static final RenderPipeline COMPOSITE_PIPELINE = RenderPipeline.builder()
		.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(ColorTargetState.DEFAULT)
		.withLocation(Identifier.fromNamespaceAndPath("iris", "composite"))
		.withVertexShader(Identifier.fromNamespaceAndPath("iris", "core/screenquad")).withFragmentShader("core/blit_screen")
		.withBindGroupLayout(BindGroupLayouts.IN_SAMPLER).withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
		.withPrimitiveTopology(PrimitiveTopology.QUADS).build();

	private final RenderTargets renderTargets;

	private final ImmutableList<Pass> passes;
	private final TextureAccess noiseTexture;
	private final CenterDepthSampler centerDepthSampler;
	private final Object2ObjectMap<String, TextureAccess> customTextureIds;
	private final ImmutableSet<Integer> flippedAtLeastOnceFinal;
	private final CustomUniforms customUniforms;
	private final Object2ObjectMap<String, TextureAccess> irisCustomTextures;
	private final Set<CustomImage> customImages;
	private final TextureStage textureStage;
	private final IrisRenderingPipeline pipeline;
	private final CompositePass compositePass;

	public CompositeRenderer(IrisRenderingPipeline pipeline, CompositePass compositePass, PackDirectives packDirectives,
		ProgramSource[] sources, ComputeSource[][] computes, RenderTargets renderTargets, ShaderStorageBufferHolder holder,
		TextureAccess noiseTexture, FrameUpdateNotifier updateNotifier, CenterDepthSampler centerDepthSampler, BufferFlipper bufferFlipper,
		Supplier<ShadowRenderTargets> shadowTargetsSupplier, TextureStage textureStage,
		Object2ObjectMap<String, TextureAccess> customTextureIds, Object2ObjectMap<String, TextureAccess> irisCustomTextures,
		Set<CustomImage> customImages, ImmutableMap<Integer, Boolean> explicitPreFlips, CustomUniforms customUniforms) {
		this.pipeline = pipeline;
		this.compositePass = compositePass;
		this.noiseTexture = noiseTexture;
		this.centerDepthSampler = centerDepthSampler;
		this.renderTargets = renderTargets;
		this.customTextureIds = customTextureIds;
		this.customUniforms = customUniforms;
		this.irisCustomTextures = irisCustomTextures;
		this.customImages = customImages;
		this.textureStage = textureStage;

		final PackRenderTargetDirectives renderTargetDirectives = packDirectives.getRenderTargetDirectives();
		final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargetSettings = renderTargetDirectives.getRenderTargetSettings();

		final ImmutableList.Builder<Pass> passes = ImmutableList.builder();
		final ImmutableSet.Builder<Integer> flippedAtLeastOnce = new ImmutableSet.Builder<>();

		explicitPreFlips.forEach((buffer, shouldFlip) -> {
			if (shouldFlip) {
				bufferFlipper.flip(buffer);
			}
		});

		for (int i = 0; i < sources.length; i++) {
			ProgramSource source = sources[i];

			ImmutableSet<Integer> flipped = bufferFlipper.snapshot();
			ImmutableSet<Integer> flippedAtLeastOnceSnapshot = flippedAtLeastOnce.build();

			if (source == null || !source.isValid()) {
				if (computes.length != 0 && computes[i] != null && computes[i].length > 0) {
					ComputeOnlyPass pass = new ComputeOnlyPass();
					pass.name = computes[i].length > 0 ? Arrays.stream(computes[i]).filter(Objects::nonNull).findFirst()
						.map(ComputeSource::getName).orElse("unknown") : "unknown";
					pass.computes = createComputes(computes[i], flipped, flippedAtLeastOnceSnapshot, shadowTargetsSupplier, holder);
					passes.add(pass);
				}
				continue;
			}

			Pass pass = new Pass();
			ProgramDirectives directives = source.getDirectives();

			pass.name = source.getName();

			var writesToAlt = new HashSet<Integer>();

			for (int target : directives.getDrawBuffers()) {
				if (!flipped.contains(target)) {
					writesToAlt.add(target);
				}
			}

			pass.attachments = new ColorAttachmentPlan.Builder().program(source.getName(), directives, writesToAlt)
				.build(renderTargets.maxDrawBuffers());
			pass.program = this.createProgram(source, flipped, flippedAtLeastOnceSnapshot, shadowTargetsSupplier,
				pass.attachments.program(source.getName()));
			pass.blendModeOverride = source.getDirectives().getBlendModeOverride().orElse(null);
			if (computes.length != 0) {
				pass.computes = createComputes(computes[i], flipped, flippedAtLeastOnceSnapshot, shadowTargetsSupplier, holder);
			} else {
				pass.computes = new ComputeProgram[0];
			}
			int[] drawBuffers = directives.getDrawBuffers();

			int passWidth = 0;
			int passHeight = 0;

			ImmutableMap<Integer, Boolean> explicitFlips = directives.getExplicitFlips();

			RenderFramebuffer framebuffer = renderTargets.createMergedFramebuffer(pass.attachments, false);

			for (int buffer : drawBuffers) {
				RenderTarget target = renderTargets.get(buffer);
				if ((passWidth > 0 && passWidth != target.getWidth()) || (passHeight > 0 && passHeight != target.getHeight())) {
					throw new IllegalStateException("Pass sizes must match for drawbuffers " + Arrays.toString(
						drawBuffers) + "\nOriginal width: " + passWidth + " New width: " + target.getWidth() + " Original height: " + passHeight + " New height: " + target.getHeight());
				}
				passWidth = target.getWidth();
				passHeight = target.getHeight();

				if (explicitFlips.get(buffer) == Boolean.FALSE) {
					continue;
				}

				bufferFlipper.flip(buffer);
				flippedAtLeastOnce.add(buffer);
			}

			explicitFlips.forEach((buffer, shouldFlip) -> {
				if (shouldFlip) {
					bufferFlipper.flip(buffer);
					flippedAtLeastOnce.add(buffer);
				}
			});

			pass.drawBuffers = directives.getDrawBuffers();
			pass.viewWidth = passWidth;
			pass.viewHeight = passHeight;
			pass.stageReadsFromAlt = flipped;
			pass.framebuffer = framebuffer;
			pass.viewportScale = directives.getViewportScale();
			pass.mipmappedBuffers = directives.getMipmappedBuffers();
			pass.flippedAtLeastOnce = flippedAtLeastOnceSnapshot;

			passes.add(pass);
		}

		this.passes = passes.build();
		this.flippedAtLeastOnceFinal = flippedAtLeastOnce.build();
	}

	private static void setupMipmapping(RenderTarget target, boolean readFromAlt) {
		if (target == null) {
			return;
		}

		target.generateMipmaps(readFromAlt);
	}

	private boolean hasComputes(ComputeSource[][] computes) {
		boolean hasCompute = false;

		for (int i = 0; i < computes.length; i++) {
			if (computes[i].length > 0) {
				for (int j = 0; j < computes[i].length; j++) {
					if (computes[i][j] != null) {
						hasCompute = true;
						break;
					}
				}
			}
		}

		return hasCompute;
	}

	public ImmutableSet<Integer> getFlippedAtLeastOnceFinal() {
		return this.flippedAtLeastOnceFinal;
	}

	public void recalculateSizes() {
		for (Pass pass : passes) {
			if (pass instanceof ComputeOnlyPass) {
				continue;
			}
			int passWidth = 0;
			int passHeight = 0;
			for (int buffer : pass.drawBuffers) {
				RenderTarget target = renderTargets.get(buffer);
				if ((passWidth > 0 && passWidth != target.getWidth()) || (passHeight > 0 && passHeight != target.getHeight())) {
					throw new IllegalStateException("Pass widths must match");
				}
				passWidth = target.getWidth();
				passHeight = target.getHeight();
			}
			renderTargets.destroyFramebuffer(pass.framebuffer);
			pass.framebuffer = this.renderTargets.createMergedFramebuffer(pass.attachments, false);
			pass.viewWidth = passWidth;
			pass.viewHeight = passHeight;
		}
	}

	public void prepare() {
		for (Pass pass : this.passes) {
			for (ComputeProgram compute : pass.computes) {
				if (compute != null) {
					compute.prepare();
				}
			}
			if (!(pass instanceof ComputeOnlyPass)) {
				pass.program.prepare(pass.framebuffer.getColorViews());
			}
		}
	}

	public void renderAll() {
		boolean previousIgnore = ImmediateState.temporarilyIgnorePass;
		ImmediateState.temporarilyIgnorePass = true;

		try {
			for (Pass pass : this.passes) {
				for (ComputeProgram compute : pass.computes) {
					if (compute != null) {
						compute.dispatch(this.renderTargets.getCurrentWidth(), this.renderTargets.getCurrentHeight());
					}
				}

				if (pass instanceof ComputeOnlyPass) {
					continue;
				}

				for (int index : pass.mipmappedBuffers) {
					setupMipmapping(this.renderTargets.get(index), pass.stageReadsFromAlt.contains(index));
				}

				var level = Minecraft.getInstance().level;
				pass.program.draw(pass.framebuffer.getColorViews(), pass.viewportScale, level == null ? 0 : level.getGameTime(),
					SystemTimeUniforms.COUNTER.getAsInt());
			}
		} finally {
			ImmediateState.temporarilyIgnorePass = previousIgnore;
		}
	}

	private CompositeProgram createProgram(ProgramSource source, ImmutableSet<Integer> flipped,
		ImmutableSet<Integer> flippedAtLeastOnceSnapshot, Supplier<ShadowRenderTargets> shadowTargetsSupplier,
		ColorAttachmentPlan.Program outputs) {

		Map<PatchShaderType, String> transformed = TransformPatcher.patchComposite(source.getName(),
			source.getVertexSource().orElseThrow(NullPointerException::new), source.getGeometrySource().orElse(null),
			source.getFragmentSource().orElseThrow(NullPointerException::new), textureStage, pipeline.getTextureMap(),
			pipeline.getTextureOverrides(textureStage));

		var program = new CompositeProgram(this.pipeline.getPackSamplers(), source.getName(), transformed, outputs,
			outputs.blends(source.getDirectives(), null));
		var builder = program.getSamplers();
		builder.addStorageBuffers(this.pipeline.getStorageBuffers());

		program.getUniforms().useFrameUniforms(this.pipeline.getFrameUniforms());
		CommonUniforms.addDynamicUniforms(program.getUniforms(), FogMode.OFF);

		CustomTextureSamplerInterceptor customTextureSamplerInterceptor = new CustomTextureSamplerInterceptor(builder,
			this.customTextureIds, flippedAtLeastOnceSnapshot);
		IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, () -> flipped, renderTargets, true, pipeline);
		IrisSamplers.addCustomTextures(builder, irisCustomTextures);
		IrisSamplers.addCustomImages(customTextureSamplerInterceptor, customImages);
		IrisImages.addCustomImages(builder, customImages);
		IrisImages.addRenderTargetImages(builder, () -> flipped, this.renderTargets);

		if (IrisImages.hasShadowImages(builder)) {
			IrisImages.addShadowColorImages(builder, shadowTargetsSupplier.get(), null);
		}

		IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, noiseTexture);
		IrisSamplers.addCompositeSamplers(customTextureSamplerInterceptor, renderTargets);

		if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
			IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowTargetsSupplier.get(), null,
				this.pipeline.hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
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
					String transformed = TransformPatcher.patchCompute(source.getName(), source.getSource().orElse(null), this.textureStage,
						this.pipeline.getTextureMap(), this.pipeline.getTextureOverrides(this.textureStage));

					program = new ComputeProgram(this.pipeline.getPackSamplers(), source.getName(), transformed);
					builder = program.getSamplers();
					builder.addStorageBuffers(this.pipeline.getStorageBuffers());
				} catch (ShaderCompileException e) {
					throw e;
				} catch (RuntimeException e) {

					throw new RuntimeException("Shader compilation failed for compute " + source.getName() + "!", e);
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
						this.pipeline.hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
					IrisImages.addShadowColorImages(builder, shadowTargetsSupplier.get(), null);
				}

				this.centerDepthSampler.setUsage(builder.addTextureSampler(
					new SamplerHolder.TextureBinding("iris_centerDepthSmooth", this.centerDepthSampler::getCenterDepthTextureView,
						() -> GlSampler.NEAREST), false, "iris_centerDepthSmooth"));

				builder.finish();
				programs[i] = program;

				programs[i].setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups(),
					FilledIndirectPointer.basedOff(holder, source.getIndirectPointer()));
			}
		}

		return programs;
	}

	public void destroy() {
		for (Pass renderPass : passes) {
			renderPass.destroy();
		}
	}

	private static class Pass {
		int[] drawBuffers;
		int viewWidth;
		int viewHeight;
		String name;
		BlendModeOverride blendModeOverride;
		ComputeProgram[] computes;
		ImmutableSet<Integer> flippedAtLeastOnce;
		ImmutableSet<Integer> stageReadsFromAlt;
		ImmutableSet<Integer> mipmappedBuffers;
		ViewportData viewportScale;
		private CompositeProgram program;
		private ColorAttachmentPlan attachments;
		private RenderFramebuffer framebuffer;

		protected void destroy() {
			this.program.destroy();
			for (ComputeProgram compute : this.computes) {
				if (compute != null) {
					compute.destroy();
				}
			}
		}
	}

	private static class ComputeOnlyPass extends Pass {
		@Override
		protected void destroy() {
			for (ComputeProgram compute : this.computes) {
				if (compute != null) {
					compute.destroy();
				}
			}
		}
	}
}
