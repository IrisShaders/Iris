package net.irisshaders.iris.opengl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlSampler;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTextureView;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class GlShaderBindings implements AutoCloseable {
	final int program;
	final PatchedShader source;
	private final Map<String, Integer> bindings;
	private int references = 1;

	GlShaderBindings(String name, PatchedShader input) {
		this.source = input;
		this.bindings = GlShaderPatcher.getBindings(input.layout());
		var sources = this.source.sources();
		this.program = GlStateManager.glCreateProgram();
		var shaders = new ArrayList<Integer>();

		try {
			for (var entry : sources.entrySet()) {
				int type = switch (entry.getKey()) {
					case VERTEX -> GL20C.GL_VERTEX_SHADER;
					case FRAGMENT -> GL20C.GL_FRAGMENT_SHADER;
					case GEOMETRY -> GL32C.GL_GEOMETRY_SHADER;
					case TESS_CONTROL -> GL40C.GL_TESS_CONTROL_SHADER;
					case TESS_EVAL -> GL40C.GL_TESS_EVALUATION_SHADER;
					case COMPUTE -> GL43C.GL_COMPUTE_SHADER;
				};
				int shader = GlStateManager.glCreateShader(type);
				shaders.add(shader);
				GlStateManager.glShaderSource(shader, entry.getValue());
				GlStateManager.glCompileShader(shader);

				if (GlStateManager.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0) {
					throw new IllegalStateException(
						"failed to compile " + name + entry.getKey().extension + ": " + IrisRenderSystem.getShaderInfoLog(shader));
				}

				GlStateManager.glAttachShader(this.program, shader);
				GlStateManager.glDeleteShader(shader);
				shaders.removeLast();
			}

			GlStateManager.glLinkProgram(this.program);

			if (GlStateManager.glGetProgrami(this.program, GL20C.GL_LINK_STATUS) == 0) {
				throw new IllegalStateException("failed to link " + name + ": " + IrisRenderSystem.getProgramInfoLog(this.program));
			}

			this.assignBindings();
		} catch (RuntimeException | Error e) {
			GlStateManager.glDeleteProgram(this.program);
			throw e;
		} finally {
			for (int shader : shaders) {
				GlStateManager.glDeleteShader(shader);
			}
		}
	}

	static void bindTexture(int target, int id) {
		if (target == GL11C.GL_TEXTURE_2D) {
			GlStateManager._bindTexture(id);
		} else {
			GL11C.glBindTexture(target, id);
		}
	}

	private void assignBindings() {
		int previous = GlStateManager._getInteger(GL20C.GL_CURRENT_PROGRAM);
		GlStateManager._glUseProgram(this.program);

		try {
			for (var descriptor : this.source.layout().descriptors()) {
				int binding = this.bindings.get(descriptor.name());

				for (int i = 0; i < descriptor.count(); i++) {
					String name = descriptor.count() == 1 ? descriptor.name() : descriptor.name() + "[" + i + "]";

					switch (descriptor.kind()) {
						case UNIFORM_BUFFER -> {
							int index = IrisRenderSystem.getUniformBlockIndex(this.program, name);

							if (index == GL31C.GL_INVALID_INDEX && descriptor.count() == 1) {
								index = IrisRenderSystem.getUniformBlockIndex(this.program, name + "[0]");
							}

							if (index != GL31C.GL_INVALID_INDEX) {
								IrisRenderSystem.uniformBlockBinding(this.program, index, binding + i);
							}
						}
						case STORAGE_BUFFER -> {
							int index = GL43C.glGetProgramResourceIndex(this.program, GL43C.GL_SHADER_STORAGE_BLOCK, name);

							if (index == GL31C.GL_INVALID_INDEX && descriptor.count() == 1) {
								index = GL43C.glGetProgramResourceIndex(this.program, GL43C.GL_SHADER_STORAGE_BLOCK, name + "[0]");
							}

							if (index != GL31C.GL_INVALID_INDEX) {
								GL43C.glShaderStorageBlockBinding(this.program, index, binding + i);
							}
						}
						case SAMPLER, IMAGE ->
							GlStateManager._glUniform1i(GlStateManager._glGetUniformLocation(this.program, name), binding + i);
					}
				}
			}
		} finally {
			GlStateManager._glUseProgram(previous);
		}
	}

	void bind(ProgramResources resources, BufferUniformHolder uniforms, ProgramResources.Bindings resolved) {
		GlStateManager._glUseProgram(this.program);
		var buffers = resolved.buffers();

		if (uniforms.frameUniforms() != null) {
			buffers.put(ShaderResourceLayout.FRAME_BLOCK, List.of(uniforms.frameUniforms().snapshot()));
		}

		var nativePack = (GlRenderBackend.GlPackResources) resources.getPackSamplers().getResources();

		for (var descriptor : this.source.layout().descriptors()) {
			int binding = this.bindings.get(descriptor.name());

			switch (descriptor.kind()) {
				case UNIFORM_BUFFER, STORAGE_BUFFER -> {
					var slices = buffers.get(descriptor.name());

					if (slices == null && descriptor.name().equals("iris_SodiumPushConstants")) {
						continue;
					}

					if (slices == null) {
						throw new IllegalStateException("missing block " + descriptor.name());
					}

					for (int i = 0; i < slices.size(); i++) {
						var slice = slices.get(i);
						GL30C.glBindBufferRange(
							descriptor.kind() == ShaderResourceLayout.Kind.UNIFORM_BUFFER ? GL31C.GL_UNIFORM_BUFFER : GL43C.GL_SHADER_STORAGE_BUFFER,
							binding + i, ((GlBuffer) slice.buffer()).handle(), slice.offset(), slice.length());
					}
				}

				case SAMPLER -> {
					var reference = resources.getSamplerReferences().get(descriptor.name());
					var texture = resolved.textures().get(reference);
					GlStateManager._activeTexture(GL13C.GL_TEXTURE0 + binding);

					if (texture == null) {
						GL11C.glBindTexture(GL31C.GL_TEXTURE_BUFFER, Math.toIntExact(resolved.texels().get(reference)));
						IrisRenderSystem.bindSamplerToUnit(binding, 0);
						continue;
					}

					int target;
					int id;

					if (texture.image() != null) {
						var image = texture.image();
						target = image.getType().getGlType();
						id = Math.toIntExact(image.getView());
					} else {
						var view = (GlTextureView) texture.texture();

						target = (view.texture().usage() & 16) != 0 ? GL13C.GL_TEXTURE_CUBE_MAP : GL11C.GL_TEXTURE_2D;
						id = view.glId();
					}

					bindTexture(target, id);

					if (texture.image() == null) {
						var view = texture.texture();
						GlStateManager._texParameter(target, GL12C.GL_TEXTURE_BASE_LEVEL, view.baseMipLevel());
						GlStateManager._texParameter(target, GL12C.GL_TEXTURE_MAX_LEVEL, view.baseMipLevel() + view.mipLevels() - 1);
					}
					var external = texture.nativeSampler();
					int sampler = external == null ? nativePack.getSampler(texture.sampler()) : ((GlSampler) external).getId();
					IrisRenderSystem.bindSamplerToUnit(binding, sampler);
				}

				case IMAGE -> {
					var resource = resolved.images().get(descriptor.name()).getFirst();
					int image = resource.image() == null ? ((GlTextureView) resource.texture()).glId() : Math.toIntExact(
						resource.image().getView());
					int format = resources.getImageFormats().get(descriptor.name()).getGlFormat();
					IrisRenderSystem.bindImageTexture(binding, image, 0, descriptor.type().contains("3D"), 0, GL15C.GL_READ_WRITE, format);
				}
			}
		}
	}

	void bindPushConstants(GpuBufferSlice data) {
		var binding = this.bindings.get("iris_SodiumPushConstants");

		if (binding != null) {
			GL30C.glBindBufferRange(GL31C.GL_UNIFORM_BUFFER, binding, ((GlBuffer) data.buffer()).handle(), data.offset(), data.length());
		}
	}

	Map<String, List<GpuBufferSlice>> uploadUniforms(BufferUniformHolder uniforms) {
		if (!uniforms.data().hasRemaining()) {
			return Map.of();
		}

		var device = RenderSystem.getDevice();

		return Map.of(ShaderResourceLayout.UNIFORM_BLOCK,
			List.of(uniforms.upload(device.createCommandEncoder(), device.getDeviceInfo().limits().minUniformOffsetAlignment())));
	}

	boolean hasStorageWrites(ProgramResources resources) {
		return !resources.getStorageDeclarations().isEmpty() || !resources.getImageDeclarations().isEmpty();
	}

	GlShaderBindings retain() {
		if (this.references == 0) {
			throw new IllegalStateException("shader closed");
		}

		this.references++;
		return this;
	}

	@Override
	public void close() {
		if (this.references > 0 && --this.references == 0) {
			GlStateManager.glDeleteProgram(this.program);
		}
	}
}
