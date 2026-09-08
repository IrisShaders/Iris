package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import com.mojang.renderpearl.backend.opengl.VertexArray;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.GlRenderPipelineAccess;
import net.irisshaders.iris.mixinterface.ShaderInstanceInterface;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.pipeline.programs.ShaderOverrides;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;

import java.util.ArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.irisshaders.iris.compat.SkipList.ALWAYS;
import static net.irisshaders.iris.compat.SkipList.NONE;
import static net.irisshaders.iris.compat.SkipList.shouldSkipList;
import static net.irisshaders.iris.pipeline.programs.ShaderOverrides.isBlockEntities;

@Mixin(RenderSystem.class)
public abstract class MixinShaderManager_Overrides {
	@Unique
	private static Set<RenderPipeline> missingShaders = new HashSet<>();
	@Unique
	private static final Map<CompiledRenderPipeline, Map<GlProgram, Map<List<VertexFormat>, FrontendRenderPipeline>>> iris$overrides = new IdentityHashMap<>();

	@Inject(method = "getCompiledPipelineNullable", at = @At(value = "RETURN"), cancellable = true)
	private static void redirectIrisProgram(RenderPipeline renderPipeline, CallbackInfoReturnable<CompiledRenderPipeline> cir) {
		if (renderPipeline == CompositeRenderer.COMPOSITE_PIPELINE) return;
		if (renderPipeline == RenderPipelines.ANIMATE_SPRITE_BLIT || renderPipeline == RenderPipelines.ANIMATE_SPRITE_INTERPOLATE) return;

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof IrisRenderingPipeline irisPipeline && irisPipeline.shouldOverrideShaders() && !ImmediateState.bypass) {
			RenderPipeline newProgram = renderPipeline;

			ShaderKey shaderKey = IrisPipelines.getPipeline(irisPipeline, newProgram);
			GlProgram program = shaderKey == null ? null : irisPipeline.getShaderMap().getShader(shaderKey);

			var oldProgram = (GlRenderPipeline) ((FrontendRenderPipeline) cir.getReturnValue()).backendRenderPipeline();

			var old2 = ((FrontendRenderPipeline) cir.getReturnValue());
			if (program != null) {
				GlDevice device = (GlDevice) ((GpuDeviceAccessor) RenderSystem.getDevice()).getBackend();
				List<VertexFormat> vertexFormats = new ArrayList<>(renderPipeline.getVertexFormatBindings());
				FrontendRenderPipeline cached = iris$overrides
					.computeIfAbsent(old2, unused -> new IdentityHashMap<>())
					.computeIfAbsent(program, unused -> new HashMap<>())
					.get(vertexFormats);
				if (cached != null) {
					cir.setReturnValue(cached);
					return;
				}

				BackendRenderPipeline.CreateInfo createInfo = iris$createInfo(((GlRenderPipelineAccess) oldProgram).getCreateInfo(), program, vertexFormats);
				VertexArray vertexArray = VertexArray.createSource(GL.getCapabilities(), new HashSet<>()).apply(program, createInfo);
				FrontendRenderPipeline replacement = new FrontendRenderPipeline(old2.name(), new GlRenderPipeline(device, createInfo, program, vertexArray),
					vertexFormats, old2.uniformIndices(), old2.uniforms(), old2.colorTargetStates(), old2.wantsDepthTexture(), old2.pushConstantSize());
				iris$overrides.get(old2).get(program).put(vertexFormats, replacement);
				cir.setReturnValue(replacement);
			} else if (missingShaders.add(renderPipeline)) {
				if (renderPipeline.getLocation().getNamespace().equals("minecraft")) {
					Iris.logger.fatal("Missing program " + renderPipeline.getLocation() + " in override list. This is likely an Iris bug!!!", new Throwable());
				} else {
					Iris.logger.error("Missing program " + renderPipeline.getLocation() + " in override list. This is not a critical problem, but it could lead to weird rendering.", new Throwable());
				}
			}
		}
	}

	@Unique
	private static BackendRenderPipeline.CreateInfo iris$createInfo(BackendRenderPipeline.CreateInfo original, GlProgram program, List<VertexFormat> vertexFormats) {
		List<BackendRenderPipeline.CreateInfo.VertexBuffer> vertexBuffers = new ArrayList<>();
		List<BackendRenderPipeline.CreateInfo.AttribBinding> attribBindings = new ArrayList<>();

		for (int slot = 0; slot < vertexFormats.size(); slot++) {
			VertexFormat format = vertexFormats.get(slot);
			if (format == null) {
				continue;
			}

			vertexBuffers.add(new BackendRenderPipeline.CreateInfo.VertexBuffer(slot, format.getVertexSize(), format.getStepRate()));
			for (var element : format.getElements()) {
				int location = GL33C.glGetAttribLocation(program.getProgramId(), "iris_" + element.name());
				if (location == -1) {
					location = GL33C.glGetAttribLocation(program.getProgramId(), element.name());
				}

				if (location != -1) {
					attribBindings.add(new BackendRenderPipeline.CreateInfo.AttribBinding(slot, location, element.offset(), element.format()));
				}
			}
		}

		return new BackendRenderPipeline.CreateInfo(original.name(), original.shaders(), vertexBuffers, attribBindings, original.uniforms(),
			original.pushConstantsSize(), original.depthStencilState(), original.polygonMode(), original.cull(), original.colorTargetStates(), original.primitiveTopology());
	}

	/*@Inject(method = "compilePipeline", at = @At("RETURN"))
	private static void iris$setSkip(ShaderProgram shaderProgram, ShaderProgramConfig shaderProgramConfig, CompiledShader compiledShader, CompiledShader compiledShader2, CallbackInfoReturnable<CompiledShaderProgram> cir) {
		CompiledShaderProgram p = cir.getReturnValue();
		MethodHandle shouldSkip = shouldSkipList.computeIfAbsent(p.getClass(), x -> {
			try {
				MethodHandle iris$skipDraw = MethodHandles.lookup().findVirtual(x, "iris$skipDraw", MethodType.methodType(boolean.class));
				Iris.logger.warn("Class " + x.getName() + " has opted out of being rendered with shaders.");
				return iris$skipDraw;
			} catch (NoSuchMethodException | IllegalAccessException e) {
				return NONE;
			}
		});


		if (Iris.getIrisConfig().shouldSkip(shaderProgram.configId())) {
			shouldSkip = ALWAYS;
		}

		((ShaderInstanceInterface) p).setShouldSkip(shouldSkip);
	}*/

}
