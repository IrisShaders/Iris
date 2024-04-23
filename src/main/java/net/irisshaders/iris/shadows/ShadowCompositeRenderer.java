package net.irisshaders.iris.shadows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.gl.program.ProgramBuilder;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.program.ProgramUniforms;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.pathways.FullScreenQuadRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.samplers.IrisImages;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import net.irisshaders.iris.shaderpack.properties.ProgramDirectives;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ShadowCompositeRenderer {
	private final ShadowRenderTargets renderTargets;

	private final ImmutableList<Pass> passes;
	private final TextureAccess noiseTexture;
	private final FrameUpdateNotifier updateNotifier;
	private final Object2ObjectMap<String, TextureAccess> customTextureIds;
	private final ImmutableSet<Integer> flippedAtLeastOnceFinal;
	private final CustomUniforms customUniforms;
	private final Object2ObjectMap<String, TextureAccess> irisCustomTextures;
	private final WorldRenderingPipeline pipeline;
	private final Set<GlImage> irisCustomImages;

	public ShadowCompositeRenderer(WorldRenderingPipeline pipeline, PackDirectives packDirectives, ProgramSource[] sources, ComputeSource[][] computes, ShadowRenderTargets renderTargets, ShaderStorageBufferHolder holder,
								   TextureAccess noiseTexture, FrameUpdateNotifier updateNotifier,
								   Object2ObjectMap<String, TextureAccess> customTextureIds, Set<GlImage> customImages, ImmutableMap<Integer, Boolean> explicitPreFlips, Object2ObjectMap<String, TextureAccess> irisCustomTextures, CustomUniforms customUniforms) {
		this.pipeline = pipeline;
		this.noiseTexture = noiseTexture;
		this.updateNotifier = updateNotifier;
		this.renderTargets = renderTargets;
		this.customTextureIds = customTextureIds;
		this.irisCustomTextures = irisCustomTextures;
		this.irisCustomImages = customImages;
		this.customUniforms = customUniforms;

		final PackRenderTargetDirectives renderTargetDirectives = packDirectives.getRenderTargetDirectives();
		final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargetSettings =
			renderTargetDirectives.getRenderTargetSettings();

		final ImmutableList.Builder<Pass> passes = ImmutableList.builder();
		final ImmutableSet.Builder<Integer> flippedAtLeastOnce = new ImmutableSet.Builder<>();

		explicitPreFlips.forEach((buffer, shouldFlip) -> {
			if (shouldFlip) {
				renderTargets.flip(buffer);
				// NB: Flipping deferred_pre or composite_pre does NOT cause the "flippedAtLeastOnce" flag to trigger
			}
		});

		for (int i = 0, sourcesLength = sources.length; i < sourcesLength; i++) {
			ProgramSource source = sources[i];

			ImmutableSet<Integer> flipped = renderTargets.snapshot();
			ImmutableSet<Integer> flippedAtLeastOnceSnapshot = flippedAtLeastOnce.build();

			if (source == null || !source.isValid()) {
				if (computes[i] != null) {
					ComputeOnlyPass pass = new ComputeOnlyPass();
					pass.computes = createComputes(computes[i], flipped, flippedAtLeastOnceSnapshot, renderTargets, holder);
					passes.add(pass);
				}
				continue;
			}

			Pass pass = new Pass();
			ProgramDirectives directives = source.getDirectives();

			pass.program = createProgram(source, flipped, flippedAtLeastOnceSnapshot, renderTargets);
			pass.computes = createComputes(computes[i], flipped, flippedAtLeastOnceSnapshot, renderTargets, holder);
			int[] drawBuffers = source.getDirectives().hasUnknownDrawBuffers() ? new int[]{0, 1} : source.getDirectives().getDrawBuffers();

			GlFramebuffer framebuffer = renderTargets.createColorFramebuffer(flipped, drawBuffers);

			pass.stageReadsFromAlt = flipped;
			pass.framebuffer = framebuffer;
			pass.viewportScale = directives.getViewportScale();
			pass.mipmappedBuffers = directives.getMipmappedBuffers();
			pass.flippedAtLeastOnce = flippedAtLeastOnceSnapshot;

			passes.add(pass);

			ImmutableMap<Integer, Boolean> explicitFlips = directives.getExplicitFlips();

			// Flip the buffers that this shader wrote to
			for (int buffer : drawBuffers) {
				// compare with boxed Boolean objects to avoid NPEs
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

		GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
	}

	private static void setupMipmapping(net.irisshaders.iris.targets.RenderTarget target, boolean readFromAlt) {
		int texture = readFromAlt ? target.getAltTexture() : target.getMainTexture();

		// TODO: Only generate the mipmap if a valid mipmap hasn't been generated or if we've written to the buffer
		// (since the last mipmap was generated)
		//
		// NB: We leave mipmapping enabled even if the buffer is written to again, this appears to match the
		// behavior of ShadersMod/OptiFine, however I'm not sure if it's desired behavior. It's possible that a
		// program could use mipmapped sampling with a stale mipmap, which probably isn't great. However, the
		// sampling mode is always reset between frames, so this only persists after the first program to use
		// mipmapping on this buffer.
		//
		// Also note that this only applies to one of the two buffers in a render target buffer pair - making it
		// unlikely that this issue occurs in practice with most shader packs.
		IrisRenderSystem.generateMipmaps(texture, GL20C.GL_TEXTURE_2D);
		IrisRenderSystem.texParameteri(texture, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, target.getInternalFormat().getPixelFormat().isInteger() ? GL20C.GL_NEAREST_MIPMAP_NEAREST : GL20C.GL_LINEAR_MIPMAP_LINEAR);
	}

	private static void resetRenderTarget(RenderTarget target) {
		// Resets the sampling mode of the given render target and then unbinds it to prevent accidental sampling of it
		// elsewhere.

		int filter = GL20C.GL_LINEAR;
		if (target.getInternalFormat().getPixelFormat().isInteger()) {
			filter = GL20C.GL_NEAREST;
		}

		IrisRenderSystem.texParameteri(target.getMainTexture(), GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, filter);
		IrisRenderSystem.texParameteri(target.getAltTexture(), GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, filter);
	}

	public ImmutableSet<Integer> getFlippedAtLeastOnceFinal() {
		return this.flippedAtLeastOnceFinal;
	}

	public void renderAll() {
		RenderSystem.disableBlend();

		FullScreenQuadRenderer.INSTANCE.begin();

		for (Pass renderPass : passes) {
			boolean ranCompute = false;
			for (ComputeProgram computeProgram : renderPass.computes) {
				if (computeProgram != null) {
					ranCompute = true;
					computeProgram.use();
					this.customUniforms.push(computeProgram);
					com.mojang.blaze3d.pipeline.RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
					computeProgram.dispatch(main.width, main.height);
				}
			}

			if (ranCompute) {
				IrisRenderSystem.memoryBarrier(GL43C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL43C.GL_TEXTURE_FETCH_BARRIER_BIT | GL43C.GL_SHADER_STORAGE_BARRIER_BIT);
			}

			Program.unbind();

			if (renderPass instanceof ComputeOnlyPass) {
				continue;
			}

			if (!renderPass.mipmappedBuffers.isEmpty()) {
				RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

				for (int index : renderPass.mipmappedBuffers) {
					setupMipmapping(renderTargets.get(index), renderPass.stageReadsFromAlt.contains(index));
				}
			}

			float scaledWidth = renderTargets.getResolution() * renderPass.viewportScale.scale();
			float scaledHeight = renderTargets.getResolution() * renderPass.viewportScale.scale();
			int beginWidth = (int) (renderTargets.getResolution() * renderPass.viewportScale.viewportX());
			int beginHeight = (int) (renderTargets.getResolution() * renderPass.viewportScale.viewportY());
			RenderSystem.viewport(beginWidth, beginHeight, (int) scaledWidth, (int) scaledHeight);

			renderPass.framebuffer.bind();
			renderPass.program.use();

			this.customUniforms.push(renderPass.program);

			FullScreenQuadRenderer.INSTANCE.renderQuad();
		}

		FullScreenQuadRenderer.INSTANCE.end();

		// Make sure to reset the viewport to how it was before... Otherwise weird issues could occur.
		ProgramUniforms.clearActiveUniforms();
		GlStateManager._glUseProgram(0);

		for (int i = 0; i < renderTargets.getRenderTargetCount(); i++) {
			// Reset mipmapping states at the end of the frame.
			if (renderTargets.get(i) != null) {
				resetRenderTarget(renderTargets.get(i));
			}
		}

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);
	}

	// TODO: Don't just copy this from DeferredWorldRenderingPipeline
	private Program createProgram(ProgramSource source, ImmutableSet<Integer> flipped, ImmutableSet<Integer> flippedAtLeastOnceSnapshot,
								  ShadowRenderTargets targets) {
		// TODO: Properly handle empty shaders
		Map<PatchShaderType, String> transformed = TransformPatcher.patchComposite(
			source.getName(),
			source.getVertexSource().orElseThrow(NullPointerException::new),
			source.getGeometrySource().orElse(null),
			source.getFragmentSource().orElseThrow(NullPointerException::new), TextureStage.SHADOWCOMP, pipeline.getTextureMap());
		String vertex = transformed.get(PatchShaderType.VERTEX);
		String geometry = transformed.get(PatchShaderType.GEOMETRY);
		String fragment = transformed.get(PatchShaderType.FRAGMENT);
		ShaderPrinter.printProgram(source.getName()).addSources(transformed).print();

		Objects.requireNonNull(flipped);
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), vertex, geometry, fragment,
				IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS);
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed for shadow composite " + source.getName() + "!", e);
		}

		ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureIds, flippedAtLeastOnceSnapshot);

		CommonUniforms.addDynamicUniforms(builder, FogMode.OFF);
		this.customUniforms.assignTo(builder);

		IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, noiseTexture);
		IrisSamplers.addCustomTextures(customTextureSamplerInterceptor, irisCustomTextures);

		IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, targets, flipped, pipeline.hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
		IrisImages.addShadowColorImages(builder, targets, flipped);
		IrisImages.addCustomImages(builder, irisCustomImages);
		IrisSamplers.addCustomImages(builder, irisCustomImages);
		Program build = builder.build();
		this.customUniforms.mapholderToPass(builder, build);

		return build;
	}

	private ComputeProgram[] createComputes(ComputeSource[] sources, ImmutableSet<Integer> flipped, ImmutableSet<Integer> flippedAtLeastOnceSnapshot,
											ShadowRenderTargets targets, ShaderStorageBufferHolder holder) {
		ComputeProgram[] programs = new ComputeProgram[sources.length];
		for (int i = 0; i < programs.length; i++) {
			ComputeSource source = sources[i];
			if (source == null || !source.getSource().isPresent()) {
				continue;
			} else {
				Objects.requireNonNull(flipped);
				ProgramBuilder builder;

				try {
					String transformed = TransformPatcher.patchCompute(source.getName(), source.getSource().orElse(null), TextureStage.SHADOWCOMP, pipeline.getTextureMap());

					ShaderPrinter.printProgram(source.getName()).addSource(PatchShaderType.COMPUTE, transformed).print();

					builder = ProgramBuilder.beginCompute(source.getName(), transformed, IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS);
				} catch (RuntimeException e) {
					// TODO: Better error handling
					throw new RuntimeException("Shader compilation failed for shadowcomp compute " + source.getName() + "!", e);
				}

				ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureIds, flippedAtLeastOnceSnapshot);

				CommonUniforms.addDynamicUniforms(builder, FogMode.OFF);
				this.customUniforms.assignTo(builder);
				IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, noiseTexture);
				IrisSamplers.addCustomTextures(customTextureSamplerInterceptor, irisCustomTextures);

				IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, targets, flipped, pipeline.hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
				IrisImages.addShadowColorImages(builder, targets, flipped);

				IrisImages.addCustomImages(builder, irisCustomImages);
				IrisSamplers.addCustomImages(builder, irisCustomImages);
				programs[i] = builder.buildCompute();

				this.customUniforms.mapholderToPass(builder, programs[i]);


				programs[i].setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups(), FilledIndirectPointer.basedOff(holder, source.getIndirectPointer()));
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
		Program program;
		GlFramebuffer framebuffer;
		ImmutableSet<Integer> flippedAtLeastOnce;
		ImmutableSet<Integer> stageReadsFromAlt;
		ImmutableSet<Integer> mipmappedBuffers;
		ViewportData viewportScale;
		ComputeProgram[] computes;

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
