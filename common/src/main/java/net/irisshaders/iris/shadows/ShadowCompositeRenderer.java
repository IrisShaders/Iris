package net.irisshaders.iris.shadows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.CompositeProgram;
import net.irisshaders.iris.pipeline.programs.ComputeProgram;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.samplers.CustomTextureSamplerInterceptor;
import net.irisshaders.iris.samplers.IrisImages;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import net.irisshaders.iris.shaderpack.properties.ProgramDirectives;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.targets.CustomImage;
import net.irisshaders.iris.targets.RenderFramebuffer;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShadowCompositeRenderer {
	private final ShadowRenderTargets renderTargets;

	private final ImmutableList<Pass> passes;
	private final TextureAccess noiseTexture;
	private final Object2ObjectMap<String, TextureAccess> customTextureIds;
	private final ImmutableSet<Integer> flippedAtLeastOnceFinal;
	private final CustomUniforms customUniforms;
	private final Object2ObjectMap<String, TextureAccess> irisCustomTextures;
	private final IrisRenderingPipeline pipeline;
	private final Set<CustomImage> irisCustomImages;

	public ShadowCompositeRenderer(IrisRenderingPipeline pipeline, PackDirectives packDirectives, ProgramSource[] sources,
		ComputeSource[][] computes, ShadowRenderTargets renderTargets, ShaderStorageBufferHolder holder, TextureAccess noiseTexture,
		FrameUpdateNotifier updateNotifier, Object2ObjectMap<String, TextureAccess> customTextureIds, Set<CustomImage> customImages,
		ImmutableMap<Integer, Boolean> explicitPreFlips, Object2ObjectMap<String, TextureAccess> irisCustomTextures,
		CustomUniforms customUniforms) {
		this.pipeline = pipeline;
		this.noiseTexture = noiseTexture;
		this.renderTargets = renderTargets;
		this.customTextureIds = customTextureIds;
		this.irisCustomTextures = irisCustomTextures;
		this.irisCustomImages = customImages;
		this.customUniforms = customUniforms;

		final PackRenderTargetDirectives renderTargetDirectives = packDirectives.getRenderTargetDirectives();
		final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargetSettings = renderTargetDirectives.getRenderTargetSettings();

		final ImmutableList.Builder<Pass> passes = ImmutableList.builder();
		final ImmutableSet.Builder<Integer> flippedAtLeastOnce = new ImmutableSet.Builder<>();

		explicitPreFlips.forEach((buffer, shouldFlip) -> {
			if (shouldFlip) {
				renderTargets.flip(buffer);
			}
		});

		for (int i = 0, sourcesLength = sources.length; i < sourcesLength; i++) {
			ProgramSource source = sources[i];

			ImmutableSet<Integer> flipped = renderTargets.snapshot();
			ImmutableSet<Integer> flippedAtLeastOnceSnapshot = flippedAtLeastOnce.build();

			if (source == null || !source.isValid()) {
				if (computes.length > 0 && computes[i] != null) {
					ComputeOnlyPass pass = new ComputeOnlyPass();
					pass.computes = createComputes(computes[i], flipped, flippedAtLeastOnceSnapshot, renderTargets, holder);
					passes.add(pass);
				}
				continue;
			}

			Pass pass = new Pass();
			ProgramDirectives directives = source.getDirectives();

			pass.name = source.getName();
			pass.blendModeOverride = source.getDirectives().getBlendModeOverride().orElse(null);
			if (computes.length > 0) {
				pass.computes = createComputes(computes[i], flipped, flippedAtLeastOnceSnapshot, renderTargets, holder);
			} else {
				pass.computes = new ComputeProgram[0];
			}
			int[] drawBuffers = renderTargets.getDrawBuffers(
				directives.hasUnknownDrawBuffers() ? new int[]{0, 1} : directives.getDrawBuffers());

			var writesAlt = new HashSet<Integer>();

			for (int buffer : drawBuffers) {
				if (!flipped.contains(buffer)) {
					writesAlt.add(buffer);
				}
			}

			var attachments = new ColorAttachmentPlan.Builder().program(source.getName(), drawBuffers, writesAlt)
				.build(RenderSystem.getDevice().getDeviceInfo().limits().maxColorAttachments());
			RenderFramebuffer framebuffer = renderTargets.createMergedFramebuffer(attachments);
			pass.program = this.createProgram(source, flipped, flippedAtLeastOnceSnapshot, renderTargets,
				attachments.program(source.getName()), drawBuffers);

			pass.stageReadsFromAlt = flipped;
			pass.framebuffer = framebuffer;
			pass.viewportScale = directives.getViewportScale();
			pass.mipmappedBuffers = directives.getMipmappedBuffers();
			pass.flippedAtLeastOnce = flippedAtLeastOnceSnapshot;

			passes.add(pass);

			ImmutableMap<Integer, Boolean> explicitFlips = directives.getExplicitFlips();

			for (int buffer : drawBuffers) {

				if (explicitFlips.get(buffer) == Boolean.FALSE) {
					continue;
				}

				renderTargets.flip(buffer);
				flippedAtLeastOnce.add(buffer);
			}

			explicitFlips.forEach((buffer, shouldFlip) -> {
				if (shouldFlip) {
					renderTargets.flip(buffer);
					flippedAtLeastOnce.add(buffer);
				}
			});
		}

		this.passes = passes.build();
		this.flippedAtLeastOnceFinal = flippedAtLeastOnce.build();
	}

	public ImmutableSet<Integer> getFlippedAtLeastOnceFinal() {
		return this.flippedAtLeastOnceFinal;
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
		for (Pass pass : this.passes) {
			for (var compute : pass.computes) {
				if (compute != null) {
					compute.dispatch(this.renderTargets.getResolution(), this.renderTargets.getResolution());
				}
			}

			if (pass instanceof ComputeOnlyPass) {
				continue;
			}

			for (int index : pass.mipmappedBuffers) {
				this.renderTargets.get(index).generateMipmaps(pass.stageReadsFromAlt.contains(index));
			}

			var level = Minecraft.getInstance().level;
			pass.program.draw(pass.framebuffer.getColorViews(), pass.viewportScale, level == null ? 0 : level.getGameTime(),
				SystemTimeUniforms.COUNTER.getAsInt());
		}
	}

	private CompositeProgram createProgram(ProgramSource source, ImmutableSet<Integer> flipped,
		ImmutableSet<Integer> flippedAtLeastOnceSnapshot, ShadowRenderTargets targets, ColorAttachmentPlan.Program outputs,
		int[] drawBuffers) {
		var transformed = TransformPatcher.patchComposite(source.getName(), source.getVertexSource().orElseThrow(),
			source.getGeometrySource().orElse(null), source.getFragmentSource().orElseThrow(), TextureStage.SHADOWCOMP,
			this.pipeline.getTextureMap(), this.pipeline.getTextureOverrides(TextureStage.SHADOWCOMP));

		var base = source.getDirectives().getBlendModeOverride().map(BlendModeOverride::blendMode).orElse(null);
		var modes = new ArrayList<BlendMode>(Collections.nCopies(drawBuffers.length, base));

		for (var override : source.getDirectives().getBufferBlendOverrides()) {
			for (int i = 0; i < drawBuffers.length; i++) {
				if (drawBuffers[i] == override.index()) {
					modes.set(i, override.blendMode());
				}
			}
		}

		var program = new CompositeProgram(this.pipeline.getPackSamplers(), source.getName(), transformed, outputs,
			outputs.remapBlends(modes));
		program.getUniforms().useFrameUniforms(this.pipeline.getFrameUniforms());
		CommonUniforms.addDynamicUniforms(program.getUniforms(), FogMode.OFF);

		var samplers = new CustomTextureSamplerInterceptor(program.getSamplers(), this.customTextureIds, flippedAtLeastOnceSnapshot);
		IrisSamplers.addNoiseSampler(samplers, this.noiseTexture);
		IrisSamplers.addCustomTextures(samplers, this.irisCustomTextures);

		IrisSamplers.addShadowSamplers(samplers, targets, flipped, this.pipeline.hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
		IrisSamplers.addCustomImages(samplers, this.irisCustomImages);
		IrisImages.addCustomImages(program.getSamplers(), this.irisCustomImages);
		IrisImages.addShadowColorImages(program.getSamplers(), targets, flipped);
		program.getSamplers().addStorageBuffers(this.pipeline.getStorageBuffers());
		program.getSamplers().finish();

		return program;
	}

	private ComputeProgram[] createComputes(ComputeSource[] sources, ImmutableSet<Integer> flipped,
		ImmutableSet<Integer> flippedAtLeastOnceSnapshot, ShadowRenderTargets targets, ShaderStorageBufferHolder holder) {
		var programs = new ComputeProgram[sources.length];
		for (int i = 0; i < sources.length; i++) {
			var source = sources[i];
			if (source == null || source.getSource().isEmpty()) {
				continue;
			}

			var transformed = TransformPatcher.patchCompute(source.getName(), source.getSource().orElseThrow(), TextureStage.SHADOWCOMP,
				this.pipeline.getTextureMap(), this.pipeline.getTextureOverrides(TextureStage.SHADOWCOMP));

			var program = new ComputeProgram(this.pipeline.getPackSamplers(), source.getName(), transformed);
			program.getUniforms().useFrameUniforms(this.pipeline.getFrameUniforms());
			CommonUniforms.addDynamicUniforms(program.getUniforms(), FogMode.OFF);

			var samplers = new CustomTextureSamplerInterceptor(program.getSamplers(), this.customTextureIds, flippedAtLeastOnceSnapshot);
			IrisSamplers.addNoiseSampler(samplers, this.noiseTexture);
			IrisSamplers.addCustomTextures(samplers, this.irisCustomTextures);
			IrisSamplers.addShadowSamplers(samplers, targets, flipped, this.pipeline.hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
			IrisSamplers.addCustomImages(samplers, this.irisCustomImages);
			IrisImages.addCustomImages(program.getSamplers(), this.irisCustomImages);
			IrisImages.addShadowColorImages(program.getSamplers(), targets, flipped);
			program.getSamplers().addStorageBuffers(this.pipeline.getStorageBuffers());
			program.getSamplers().finish();
			program.setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups(),
				FilledIndirectPointer.basedOff(holder, source.getIndirectPointer()));
			programs[i] = program;
		}

		return programs;
	}

	public void destroy() {
		for (Pass renderPass : passes) {
			renderPass.destroy();
		}
	}

	private static class Pass {
		String name;
		BlendModeOverride blendModeOverride;
		ImmutableSet<Integer> flippedAtLeastOnce;
		ImmutableSet<Integer> stageReadsFromAlt;
		ImmutableSet<Integer> mipmappedBuffers;
		ViewportData viewportScale;
		ComputeProgram[] computes;
		private CompositeProgram program;
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
