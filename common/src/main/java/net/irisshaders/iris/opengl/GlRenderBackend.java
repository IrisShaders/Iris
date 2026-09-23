package net.irisshaders.iris.opengl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.opengl.FrameBufferAttachment;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import net.irisshaders.iris.backend.ComputePipeline;
import net.irisshaders.iris.backend.GraphicsShader;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.backend.NonRenderCommands;
import net.irisshaders.iris.backend.PackResources;
import net.irisshaders.iris.backend.RenderBackend;
import net.irisshaders.iris.backend.SyncCommand;
import net.irisshaders.iris.backend.WorldPassCommands;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GlRenderBackend implements RenderBackend, NonRenderCommands {
	private final Map<PatchedShader, GlShaderBindings> shaders = new LinkedHashMap<>(16, 0.75f, true);
	private boolean closed;

	public static int getFramebuffer(List<GpuTextureView> colors, GpuTextureView depth) {
		var backend = IrisRenderSystem.getGlDevice();
		List<FrameBufferAttachment> attachments = colors.stream().map(v -> (FrameBufferAttachment) v).toList();

		return backend.frameBufferCache().getFbo(backend.directStateAccess(), attachments, (FrameBufferAttachment) depth);
	}

	private static void clearAttachment(GpuFormat format, Vector4f color) {
		int framebuffer = GlStateManager.getFrameBuffer(GL30C.GL_DRAW_FRAMEBUFFER);

		switch (format == GpuFormat.RGB10A2_UINT ? GpuFormat.R32_UINT.componentType() : format.componentType()) {
			case UINT_8, UINT_16, UINT_32 -> {
				int[] value = new int[4];

				for (int i = 0; i < value.length; i++) {
					value[i] = (int) (long) Math.clamp(color.get(i), 0, 4294967295.0);
				}

				IrisRenderSystem.clearBufferuiv(framebuffer, GL11C.GL_COLOR, 0, value);
			}
			case SINT_8, SINT_16, SINT_32 -> IrisRenderSystem.clearBufferiv(framebuffer, GL11C.GL_COLOR, 0,
				new int[]{(int) color.x, (int) color.y, (int) color.z, (int) color.w});
			default -> GlStateManager._clearBuffer(0, color);
		}
	}

	@Override
	public WorldPassCommands createRenderPass(CommandEncoderBackend encoder, RenderPassDescriptor descriptor) {
		return new GlWorldCommands(encoder, descriptor);
	}

	@Override
	public WorldPassCommands adaptRenderPass(RenderPassBackend pass, RenderArea area) {
		return new GlWorldCommands(((FrontendCommandEncoder) RenderSystem.getDevice().createCommandEncoder()).backend(), pass, area, false);
	}

	@Override
	public NonRenderCommands commands() {
		return this;
	}

	@Override
	public SyncCommand createSync() {
		return new GlSyncCommand();
	}

	@Override
	public void dispatch(ComputePipeline pipeline, Vector3i groups, FilledIndirectPointer indirect) {
		((GlComputePipeline) pipeline).dispatch(groups, indirect);
	}

	@Override
	public PackResources createPackResources(PackSamplers pack) {
		return new GlPackResources();
	}

	@Override
	public GraphicsShader createGraphicsShader(String name, PatchedShader source, ProgramResources resources,
		BufferUniformHolder uniforms) {
		return new GlGraphicsShader(this.getShader(name, source), resources, uniforms);
	}

	private GlShaderBindings getShader(String name, PatchedShader source) {
		if (this.closed) {
			throw new IllegalStateException("render backend is closed");
		}

		var shader = this.shaders.get(source);

		if (shader == null) {
			shader = new GlShaderBindings(name, source);
			this.shaders.put(source, shader);

			while (this.shaders.size() > 256) {
				var iterator = this.shaders.values().iterator();
				var oldest = iterator.next();
				iterator.remove();
				oldest.close();
			}
		}

		return shader;
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;
		this.shaders.values().forEach(GlShaderBindings::close);
		this.shaders.clear();
	}

	@Override
	public ComputePipeline createComputePipeline(String name, PatchedShader source, ProgramResources resources,
		BufferUniformHolder uniforms) {
		return new GlComputePipeline(this.getShader(name, source), resources, uniforms);
	}

	@Override
	public IrisImage createImage(String name, TextureType type, GpuFormat format, int width, int height, int depth, boolean storage) {
		return new GlImage(type, format, width, height, depth);
	}

	@Override
	public GpuBuffer createStorageBuffer(String name, long size, byte[] content) {
		if (size <= 0 || content != null && content.length > size || size > GL32C.glGetInteger64(GL43C.GL_MAX_SHADER_STORAGE_BLOCK_SIZE)) {
			throw new IllegalArgumentException("invalid storage buffer size: " + name);
		}

		var buffer = RenderSystem.getDevice().createBuffer(() -> name,
			ShaderStorageBuffer.USAGE_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC | GpuBuffer.USAGE_INDIRECT_PARAMETERS,
			size);
		GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, ((GlBuffer) buffer).handle());
		IrisRenderSystem.clearBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, GL30C.GL_R32UI, 0, size, GL30C.GL_RED_INTEGER,
			GL11C.GL_UNSIGNED_INT, new int[]{0});

		if (content != null && content.length != 0) {
			var data = MemoryUtil.memAlloc(content.length);

			try {
				data.put(content).flip();
				RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(0, content.length), data);
			} finally {
				MemoryUtil.memFree(data);
			}
		}

		return buffer;
	}

	@Override
	public void retire(Runnable destructor) {
		RenderSystem.queueFencedTask(destructor);
	}

	@Override
	public void resetBindings() {
		for (int unit = 0; unit < 16; unit++) {
			GlStateManager._activeTexture(GL13C.GL_TEXTURE0 + unit);
			GlStateManager._bindTexture(0);
		}

		GlStateManager._activeTexture(GL13C.GL_TEXTURE0);
	}

	@Override
	public void generateMipmaps(GpuTexture texture, boolean linear) {
		int id = ((GlTexture) texture).glId();
		IrisRenderSystem.texParameteri(id, GL11C.GL_TEXTURE_2D, GL12C.GL_TEXTURE_BASE_LEVEL, 0);
		IrisRenderSystem.texParameteri(id, GL11C.GL_TEXTURE_2D, GL12C.GL_TEXTURE_MAX_LEVEL, texture.getMipLevels() - 1);
		IrisRenderSystem.generateMipmaps(id, GL11C.GL_TEXTURE_2D);
	}

	@Override
	public void clearColors(List<GpuTextureView> textures, Vector4f color) {
		for (var texture : textures) {
			if (texture.mipLevels() == 1) {
				this.clearView(texture, color);
				continue;
			}

			for (int level = texture.baseMipLevel(); level < texture.baseMipLevel() + texture.mipLevels(); level++) {
				try (var view = RenderSystem.getDevice().createTextureView(texture.texture(), level, 1)) {
					this.clearView(view, color);
				}
			}
		}
	}

	private void clearView(GpuTextureView view, Vector4f color) {
		try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
			RenderPassDescriptor.builder(() -> "Iris color clear").withColorAttachment(view, Optional.empty())
				.withRenderArea(new RenderArea(0, 0, view.getWidth(0), view.getHeight(0))).build())) {
			GlStateManager._colorMask(0, 15);
			clearAttachment(view.texture().getFormat(), color);
		}
	}

	@Override
	public void generateDepthMips(GpuTexture texture) {
		this.generateMipmaps(texture, true);
	}

	static final class GlPackResources implements PackResources {
		private final Map<GlSampler.Description, Integer> samplers = new HashMap<>();

		int getSampler(GlSampler.Description state) {
			return this.samplers.computeIfAbsent(state, key -> {
				int id = IrisRenderSystem.genSampler();
				int mag = key.linear() ? GL11C.GL_LINEAR : GL11C.GL_NEAREST;
				int min = !key.mipmapped() ? mag : key.linearMips() ? (key.linear() ? GL11C.GL_LINEAR_MIPMAP_LINEAR : GL11C.GL_NEAREST_MIPMAP_LINEAR) : (key.linear() ? GL11C.GL_LINEAR_MIPMAP_NEAREST : GL11C.GL_NEAREST_MIPMAP_NEAREST);
				IrisRenderSystem.samplerParameteri(id, GL11C.GL_TEXTURE_MIN_FILTER, min);
				IrisRenderSystem.samplerParameteri(id, GL11C.GL_TEXTURE_MAG_FILTER, mag);

				for (int axis : new int[]{GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_TEXTURE_WRAP_T, GL12C.GL_TEXTURE_WRAP_R}) {
					IrisRenderSystem.samplerParameteri(id, axis, key.clamp() ? GL12C.GL_CLAMP_TO_EDGE : GL11C.GL_REPEAT);
				}

				IrisRenderSystem.samplerParameteri(id, GL14C.GL_TEXTURE_COMPARE_MODE,
					key.hardwareShadow() ? GL30C.GL_COMPARE_REF_TO_TEXTURE : GL11C.GL_NONE);
				IrisRenderSystem.samplerParameteri(id, GL14C.GL_TEXTURE_COMPARE_FUNC, GL11C.GL_LEQUAL);

				return id;
			});
		}

		@Override
		public void beginFrame() {
		}

		@Override
		public void close() {
			this.samplers.values().forEach(IrisRenderSystem::destroySampler);
			this.samplers.clear();
		}
	}
}
