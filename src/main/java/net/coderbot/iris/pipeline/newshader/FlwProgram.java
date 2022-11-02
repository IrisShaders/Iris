package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.pipeline.newshader.flw.FlwProgramType;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;
import org.jetbrains.annotations.Nullable;

import java.nio.FloatBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FlwProgram {
	final Program program;
	@Nullable
	final BlendModeOverride override;
	final GlFramebuffer framebuffer;

	private Matrix4f viewProjection;
	private Matrix4f view;
	private Matrix4f projection;

	public FlwProgram(ProgramSet programSet, FrameUpdateNotifier notifier, String vertex, String fragment, float alphaDiscard,
					  Consumer<SamplerHolder> initSamplers, Consumer<ImageHolder> initImages,
					  @Nullable  BlendModeOverride override,
					  GlFramebuffer framebuffer) {
		vertex = TriforcePatcher.patchFlywheel(vertex, ShaderType.VERTEX, FlwProgramType.TRANSFORMED, alphaDiscard);
		fragment = TriforcePatcher.patchFlywheel(fragment, ShaderType.FRAGMENT, FlwProgramType.TRANSFORMED, alphaDiscard);

		ProgramBuilder builder = ProgramBuilder.begin("<iris patched shaders for flywheel>", vertex, null, fragment,
			IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		CommonUniforms.addCommonUniforms(builder, programSet.getPack().getIdMap(), programSet.getPackDirectives(),
			notifier, FogMode.PER_VERTEX);
		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(builder);

		initSamplers.accept(builder);
		initImages.accept(builder);

		builder.uniformMatrix(UniformUpdateFrequency.PER_FRAME, "uViewProjection", () -> viewProjection);
		builder.uniformMatrix(UniformUpdateFrequency.PER_FRAME, "uView", () -> view);
		builder.uniformMatrix(UniformUpdateFrequency.PER_FRAME, "uProjection", () -> projection);
		builder.uniformJomlMatrix(UniformUpdateFrequency.PER_FRAME, "uNormal", new ViewToNormalMatrix(() -> view));

		this.program = builder.build();
		this.override = override;
		this.framebuffer = framebuffer;
	}

	public void bind(Matrix4f viewProjection, Matrix4f view) {
		this.viewProjection = viewProjection;
		this.view = view;
		this.projection = RenderSystem.getProjectionMatrix();

		program.use();

		if (override != null) {
			override.apply();
		}

		framebuffer.bind();
	}

	public static class ViewToNormalMatrix implements Supplier<net.coderbot.iris.vendored.joml.Matrix4f> {
		private final Supplier<Matrix4f> parent;

		ViewToNormalMatrix(Supplier<Matrix4f> parent) {
			this.parent = parent;
		}

		@Override
		public net.coderbot.iris.vendored.joml.Matrix4f get() {
			// PERF: Don't copy + allocate this matrix every time?
			Matrix4f copy = parent.get().copy();

			FloatBuffer buffer = FloatBuffer.allocate(16);

			copy.store(buffer);
			buffer.rewind();

			net.coderbot.iris.vendored.joml.Matrix4f matrix4f = new net.coderbot.iris.vendored.joml.Matrix4f(buffer);
			matrix4f.invert();
			matrix4f.transpose();

			return matrix4f;
		}
	}
}
