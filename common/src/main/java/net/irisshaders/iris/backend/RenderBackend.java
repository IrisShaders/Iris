package net.irisshaders.iris.backend;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import org.joml.Vector2i;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public interface RenderBackend extends AutoCloseable {
	WorldPassCommands createRenderPass(CommandEncoderBackend encoder, RenderPassDescriptor descriptor);

	WorldPassCommands adaptRenderPass(RenderPassBackend pass, RenderPass.RenderArea area);

	default IrisRenderPass createRenderPass(RenderPass pass) {
		return pass.iris$getIrisPass();
	}

	public default IrisRenderPass createRenderPass(String name, FramebufferDescription description, List<GpuTextureView> colors,
		GpuTextureView depth, Vector2i viewport) {
		description.validate(colors, depth, viewport);
		var builder = RenderPassDescriptor.builder(() -> name).withRenderArea(new RenderPass.RenderArea(0, 0, viewport.x, viewport.y));

		for (var color : colors) {
			builder.withColorAttachment(color, Optional.empty());
		}

		if (depth != null) {
			builder.withDepthAttachment(depth, OptionalDouble.empty());
		}

		return this.createRenderPass(((FrontendCommandEncoder) RenderSystem.getDevice().createCommandEncoder()).backend(), builder.build());
	}

	NonRenderCommands commands();

	SyncCommand createSync();

	PackResources createPackResources(PackSamplers pack);

	GraphicsShader createGraphicsShader(String name, PatchedShader source, ProgramResources resources, BufferUniformHolder uniforms);

	ComputePipeline createComputePipeline(String name, PatchedShader source, ProgramResources resources, BufferUniformHolder uniforms);

	IrisImage createImage(String name, TextureType type, GpuFormat format, int width, int height, int depth, boolean storage);

	GpuBuffer createStorageBuffer(String name, long size, byte[] content);

	void retire(Runnable destructor);

	default void finishCompilation() {
	}

	default void resetBindings() {
	}

	default int getMipmapUsage(GpuFormat format) {
		return 0;
	}

	@Override
	default void close() {
	}

}
