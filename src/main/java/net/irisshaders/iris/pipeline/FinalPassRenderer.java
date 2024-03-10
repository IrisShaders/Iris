package net.irisshaders.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.gl.program.ProgramBuilder;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.program.ProgramUniforms;
import net.irisshaders.iris.gl.sampler.SamplerLimits;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.mixin.GlStateManagerAccessor;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.irisshaders.iris.pathways.FullScreenQuadRenderer;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.samplers.IrisImages;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import net.irisshaders.iris.shaderpack.properties.ProgramDirectives;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.Blaze3dRenderTargetExt;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class FinalPassRenderer {
	private final RenderTargets renderTargets;

	@Nullable
	private final Pass finalPass;
	private final ImmutableList<SwapPass> swapPasses;
	private final GlFramebuffer baseline;
	private final GlFramebuffer colorHolder;
	private final Object2ObjectMap<String, TextureAccess> irisCustomTextures;
	private final Set<GlImage> customImages;
	private final TextureAccess noiseTexture;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;
	private final Object2ObjectMap<String, TextureAccess> customTextureIds;
	private final CustomUniforms customUniforms;
	private final WorldRenderingPipeline pipeline;
	private int lastColorTextureId;
	private int lastColorTextureVersion;

	// TODO: The length of this argument list is getting a bit ridiculous
	public FinalPassRenderer(WorldRenderingPipeline pipeline, ProgramSet pack, RenderTargets renderTargets, TextureAccess noiseTexture, ShaderStorageBufferHolder holder,
							 FrameUpdateNotifier updateNotifier, ImmutableSet<Integer> flippedBuffers,
							 CenterDepthSampler centerDepthSampler,
							 Supplier<ShadowRenderTargets> shadowTargetsSupplier,
							 Object2ObjectMap<String, TextureAccess> customTextureIds,
							 Object2ObjectMap<String, TextureAccess> irisCustomTextures, Set<GlImage> customImages, ImmutableSet<Integer> flippedAtLeastOnce
		, CustomUniforms customUniforms) {
		this.pipeline = pipeline;
		this.updateNotifier = updateNotifier;
		this.centerDepthSampler = centerDepthSampler;
		this.customTextureIds = customTextureIds;
		this.irisCustomTextures = irisCustomTextures;
		this.customImages = customImages;

		final PackRenderTargetDirectives renderTargetDirectives = pack.getPackDirectives().getRenderTargetDirectives();
		final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargetSettings =
			renderTargetDirectives.getRenderTargetSettings();

		this.noiseTexture = noiseTexture;
		this.renderTargets = renderTargets;
		this.customUniforms = customUniforms;
		this.finalPass = pack.getCompositeFinal().map(source -> {
			Pass pass = new Pass();
			ProgramDirectives directives = source.getDirectives();

			pass.program = createProgram(source, flippedBuffers, flippedAtLeastOnce, shadowTargetsSupplier);
			pass.computes = createComputes(pack.getFinalCompute(), flippedBuffers, flippedAtLeastOnce, shadowTargetsSupplier, holder);
			pass.stageReadsFromAlt = flippedBuffers;
			pass.mipmappedBuffers = directives.getMipmappedBuffers();

			return pass;
		}).orElse(null);

		IntList buffersToBeCleared = pack.getPackDirectives().getRenderTargetDirectives().getBuffersToBeCleared();

		// The name of this method might seem a bit odd here, but we want a framebuffer with color attachments that line
		// up with whatever was written last (since we're reading from these framebuffers) instead of trying to create
		// a framebuffer with color attachments different from what was written last (as we do with normal composite
		// passes that write to framebuffers).
		this.baseline = renderTargets.createGbufferFramebuffer(flippedBuffers, new int[]{0});
		this.colorHolder = new GlFramebuffer();
		this.lastColorTextureId = Minecraft.getInstance().getMainRenderTarget().getColorTextureId();
		this.lastColorTextureVersion = ((Blaze3dRenderTargetExt) Minecraft.getInstance().getMainRenderTarget()).iris$getColorBufferVersion();
		this.colorHolder.addColorAttachment(0, lastColorTextureId);

		// TODO: We don't actually fully swap the content, we merely copy it from alt to main
		// This works for the most part, but it's not perfect. A better approach would be creating secondary
		// framebuffers for every other frame, but that would be a lot more complex...
		ImmutableList.Builder<SwapPass> swapPasses = ImmutableList.builder();

		flippedBuffers.forEach((i) -> {
			int target = i;

			if (buffersToBeCleared.contains(target)) {
				return;
			}

			SwapPass swap = new SwapPass();
			RenderTarget target1 = renderTargets.getOrCreate(target);
			swap.target = target;
			swap.width = target1.getWidth();
			swap.height = target1.getHeight();
			swap.from = renderTargets.createColorFramebuffer(ImmutableSet.of(), new int[]{target});
			// NB: This is handled in RenderTargets now.
			//swap.from.readBuffer(target);
			swap.targetTexture = renderTargets.get(target).getMainTexture();

			swapPasses.add(swap);
		});

		this.swapPasses = swapPasses.build();

		GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
	}

	private static void setupMipmapping(RenderTarget target, boolean readFromAlt) {
		if (target == null) return;

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

		int filter = GL20C.GL_LINEAR_MIPMAP_LINEAR;
		if (target.getInternalFormat().getPixelFormat().isInteger()) {
			filter = GL20C.GL_NEAREST_MIPMAP_NEAREST;
		}

		IrisRenderSystem.texParameteri(texture, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, filter);
	}

	private static void resetRenderTarget(RenderTarget target) {
		if (target == null) return;
		// Resets the sampling mode of the given render target and then unbinds it to prevent accidental sampling of it
		// elsewhere.
		int filter = GL20C.GL_LINEAR;
		if (target.getInternalFormat().getPixelFormat().isInteger()) {
			filter = GL20C.GL_NEAREST;
		}

		IrisRenderSystem.texParameteri(target.getMainTexture(), GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, filter);
		IrisRenderSystem.texParameteri(target.getAltTexture(), GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, filter);

		RenderSystem.bindTexture(0);
	}

	public void renderFinalPass() {
		RenderSystem.disableBlend();
		RenderSystem.depthMask(false);

		final com.mojang.blaze3d.pipeline.RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
		final int baseWidth = main.width;
		final int baseHeight = main.height;

		// Note that since DeferredWorldRenderingPipeline uses the depth texture of the main Minecraft framebuffer,
		// we'll be writing to that depth buffer directly automatically and won't need to futz around with copying
		// depth buffer content.
		//
		// Previously, we had our own depth texture and then copied its content to the main Minecraft framebuffer.
		// This worked with vanilla, but broke with mods that used the stencil buffer.
		//
		// This approach is a fairly succinct solution to the issue of having to deal with the main Minecraft
		// framebuffer potentially having a depth-stencil buffer or similar - we'll automatically enable that to
		// work properly since we re-use the depth buffer instead of trying to make our own.
		//
		// This is not a concern for depthtex1 / depthtex2 since the copy call extracts the depth values, and the
		// shader pack only ever uses them to read the depth values.
		if (((Blaze3dRenderTargetExt) main).iris$getColorBufferVersion() != lastColorTextureVersion || main.getColorTextureId() != lastColorTextureId) {
			lastColorTextureVersion = ((Blaze3dRenderTargetExt) main).iris$getColorBufferVersion();
			this.lastColorTextureId = main.getColorTextureId();
			colorHolder.addColorAttachment(0, lastColorTextureId);
		}

		if (this.finalPass != null) {
			// If there is a final pass, we use the shader-based full screen quad rendering pathway instead
			// of just copying the color buffer.

			colorHolder.bind();

			FullScreenQuadRenderer.INSTANCE.begin();

			for (ComputeProgram computeProgram : finalPass.computes) {
				if (computeProgram != null) {
					computeProgram.use();
					this.customUniforms.push(computeProgram);
					computeProgram.dispatch(baseWidth, baseHeight);
				}
			}

			IrisRenderSystem.memoryBarrier(GL43C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL43C.GL_TEXTURE_FETCH_BARRIER_BIT | GL43C.GL_SHADER_STORAGE_BARRIER_BIT);

			if (!finalPass.mipmappedBuffers.isEmpty()) {
				RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

				for (int index : finalPass.mipmappedBuffers) {
					setupMipmapping(renderTargets.get(index), finalPass.stageReadsFromAlt.contains(index));
				}
			}

			finalPass.program.use();

			// program is the identifier for final :shrug:
			this.customUniforms.push(finalPass.program);

			FullScreenQuadRenderer.INSTANCE.renderQuad();

			FullScreenQuadRenderer.INSTANCE.end();
		} else {
			// If there are no passes, we somehow need to transfer the content of the Iris color render targets into
			// the main Minecraft framebuffer.
			//
			// Thus, the following call transfers the content of colortex0 into the main Minecraft framebuffer.
			//
			// Note that glCopyTexSubImage2D is not as strict as glBlitFramebuffer, so we don't have to worry about
			// colortex0 having a weird format. This should just work.
			//
			// We could have used a shader here, but it should be about the same performance either way:
			// https://stackoverflow.com/a/23994979/18166885
			this.baseline.bindAsReadBuffer();

			IrisRenderSystem.copyTexSubImage2D(main.getColorTextureId(), GL11C.GL_TEXTURE_2D, 0, 0, 0, 0, 0, baseWidth, baseHeight);
		}

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		for (int i = 0; i < renderTargets.getRenderTargetCount(); i++) {
			// Reset mipmapping states at the end of the frame.
			resetRenderTarget(renderTargets.get(i));
		}

		for (SwapPass swapPass : swapPasses) {
			// NB: We need to use bind(), not bindAsReadBuffer()... Previously we used bindAsReadBuffer() here which
			//     broke TAA on many packs and on many drivers.
			//
			// Note that glCopyTexSubImage2D reads from the current GL_READ_BUFFER (given by glReadBuffer()) for the
			// current framebuffer bound to GL_FRAMEBUFFER, but that is distinct from the current GL_READ_FRAMEBUFFER,
			// which is what bindAsReadBuffer() binds.
			//
			// Also note that RenderTargets already calls readBuffer(0) for us.
			swapPass.from.bind();

			RenderSystem.bindTexture(swapPass.targetTexture);
			GlStateManager._glCopyTexSubImage2D(GL20C.GL_TEXTURE_2D, 0, 0, 0, 0, 0, swapPass.width, swapPass.height);
		}

		// Make sure to reset the viewport to how it was before... Otherwise weird issues could occur.
		// Also bind the "main" framebuffer if it isn't already bound.
		main.bindWrite(true);
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
		GlStateManager._glUseProgram(0);

		for (int i = 0; i < SamplerLimits.get().getMaxTextureUnits(); i++) {
			// Unbind all textures that we may have used.
			// NB: This is necessary for shader pack reloading to work properly
			if (GlStateManagerAccessor.getTEXTURES()[i].binding != 0) {
				RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + i);
				RenderSystem.bindTexture(0);
			}
		}

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);
	}

	public void recalculateSwapPassSize() {
		for (SwapPass swapPass : swapPasses) {
			RenderTarget target = renderTargets.get(swapPass.target);
			renderTargets.destroyFramebuffer(swapPass.from);
			swapPass.from = renderTargets.createColorFramebuffer(ImmutableSet.of(), new int[]{swapPass.target});
			swapPass.width = target.getWidth();
			swapPass.height = target.getHeight();
			swapPass.targetTexture = target.getMainTexture();
		}
	}

	// TODO: Don't just copy this from DeferredWorldRenderingPipeline
	private Program createProgram(ProgramSource source, ImmutableSet<Integer> flipped, ImmutableSet<Integer> flippedAtLeastOnceSnapshot,
								  Supplier<ShadowRenderTargets> shadowTargetsSupplier) {
		// TODO: Properly handle empty shaders
		Map<PatchShaderType, String> transformed = TransformPatcher.patchComposite(
			source.getName(),
			source.getVertexSource().orElseThrow(NullPointerException::new),
			source.getGeometrySource().orElse(null),
			source.getFragmentSource().orElseThrow(NullPointerException::new), TextureStage.COMPOSITE_AND_FINAL, pipeline.getTextureMap());
		String vertex = transformed.get(PatchShaderType.VERTEX);
		String geometry = transformed.get(PatchShaderType.GEOMETRY);
		String fragment = transformed.get(PatchShaderType.FRAGMENT);

		ShaderPrinter.printProgram(source.getName()).addSources(transformed).print();

		Objects.requireNonNull(flipped);

		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), vertex, geometry, fragment,
				IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS);
		} catch (ShaderCompileException e) {
			throw e;
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed for final!", e);
		}

		CommonUniforms.addDynamicUniforms(builder, FogMode.OFF);
		this.customUniforms.assignTo(builder);

		ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureIds, flippedAtLeastOnceSnapshot);

		IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, () -> flipped, renderTargets, true, pipeline);
		IrisSamplers.addCustomImages(customTextureSamplerInterceptor, customImages);
		IrisImages.addRenderTargetImages(builder, () -> flipped, renderTargets);
		IrisImages.addCustomImages(builder, customImages);

		IrisSamplers.addCustomTextures(builder, irisCustomTextures);
		IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, noiseTexture);
		IrisSamplers.addCompositeSamplers(customTextureSamplerInterceptor, renderTargets);

		if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
			IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowTargetsSupplier.get(), null, pipeline.hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
			IrisImages.addShadowColorImages(builder, shadowTargetsSupplier.get(), null);
		}

		// TODO: Don't duplicate this with CompositeRenderer
		centerDepthSampler.setUsage(builder.addDynamicSampler(centerDepthSampler::getCenterDepthTexture, "iris_centerDepthSmooth"));

		Program build = builder.build();

		// tell the customUniforms that those locations belong to this pass
		// this is just an object to index the internal map
		this.customUniforms.mapholderToPass(builder, build);

		return build;
	}

	private ComputeProgram[] createComputes(ComputeSource[] compute, ImmutableSet<Integer> flipped, ImmutableSet<Integer> flippedAtLeastOnceSnapshot, Supplier<ShadowRenderTargets> shadowTargetsSupplier, ShaderStorageBufferHolder holder) {
		ComputeProgram[] programs = new ComputeProgram[compute.length];
		for (int i = 0; i < programs.length; i++) {
			ComputeSource source = compute[i];
			if (source == null || !source.getSource().isPresent()) {
				continue;
			} else {
				// TODO: Properly handle empty shaders
				Objects.requireNonNull(flipped);
				ProgramBuilder builder;

				try {
					String transformed = TransformPatcher.patchCompute(source.getName(), source.getSource().orElse(null), TextureStage.COMPOSITE_AND_FINAL, pipeline.getTextureMap());

					ShaderPrinter.printProgram(source.getName()).addSource(PatchShaderType.COMPUTE, transformed).print();

					builder = ProgramBuilder.beginCompute(source.getName(), transformed, IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS);
				} catch (ShaderCompileException e) {
					throw e;
				} catch (RuntimeException e) {
					// TODO: Better error handling
					throw new RuntimeException("Shader compilation failed for final compute " + source.getName() + "!", e);
				}

				ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureIds, flippedAtLeastOnceSnapshot);

				CommonUniforms.addDynamicUniforms(builder, FogMode.OFF);
				customUniforms.assignTo(builder);

				IrisSamplers.addRenderTargetSamplers(customTextureSamplerInterceptor, () -> flipped, renderTargets, true, pipeline);
				IrisSamplers.addCustomTextures(builder, irisCustomTextures);
				IrisSamplers.addCustomImages(customTextureSamplerInterceptor, customImages);

				IrisImages.addRenderTargetImages(builder, () -> flipped, renderTargets);
				IrisImages.addCustomImages(builder, customImages);

				IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, noiseTexture);
				IrisSamplers.addCompositeSamplers(customTextureSamplerInterceptor, renderTargets);

				if (IrisSamplers.hasShadowSamplers(customTextureSamplerInterceptor)) {
					IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, shadowTargetsSupplier.get(), null, pipeline.hasFeature(FeatureFlags.SEPARATE_HARDWARE_SAMPLERS));
					IrisImages.addShadowColorImages(builder, shadowTargetsSupplier.get(), null);
				}

				// TODO: Don't duplicate this with FinalPassRenderer
				centerDepthSampler.setUsage(builder.addDynamicSampler(centerDepthSampler::getCenterDepthTexture, "iris_centerDepthSmooth"));

				programs[i] = builder.buildCompute();

				this.customUniforms.mapholderToPass(builder, programs[i]);

				programs[i].setWorkGroupInfo(source.getWorkGroupRelative(), source.getWorkGroups(), FilledIndirectPointer.basedOff(holder, source.getIndirectPointer()));
			}
		}


		return programs;
	}

	public void destroy() {
		if (finalPass != null) {
			finalPass.destroy();
		}
		colorHolder.destroy();
	}

	private static final class Pass {
		Program program;
		ComputeProgram[] computes;
		ImmutableSet<Integer> stageReadsFromAlt;
		ImmutableSet<Integer> mipmappedBuffers;

		private void destroy() {
			this.program.destroy();
		}
	}

	private static final class SwapPass {
		public int target;
		public int width;
		public int height;
		GlFramebuffer from;
		int targetTexture;
	}
}
