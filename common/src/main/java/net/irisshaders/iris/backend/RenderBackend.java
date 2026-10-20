package net.irisshaders.iris.backend;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.ProgramSamplers;
import net.irisshaders.iris.uniforms.buffer.DrawUniforms;
import org.joml.Vector2i;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public interface RenderBackend extends AutoCloseable {
	public WorldPassCommands createRenderPass(RenderPassDescriptor descriptor);

	WorldPassCommands adaptRenderPass(RenderPassBackend pass, RenderPass.RenderArea area);

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

		return this.createRenderPass(builder.build());
	}

	void generateMipmaps(GpuTexture texture, boolean linear);

	void generateDepthMips(GpuTexture texture);

	void clearColors(List<GpuTextureView> textures, Vector4f color);

	PackResources createPackResources(PackSamplers pack);

	GraphicsShader createGraphicsShader(String name, PatchedShader source, ProgramSamplers resources, DrawUniforms uniforms);

	ComputePipeline createComputePipeline(String name, PatchedShader source, ProgramSamplers resources, DrawUniforms uniforms);

	IrisImage createImage(String name, TextureType type, GpuFormat format, int width, int height, int depth, boolean storage);

	GpuBuffer createStorageBuffer(String name, long size, byte[] content);

	void retire(Runnable destructor);

	default void sync(long stage, long reads, long writes) {
	}

	void finishCompilation();

	default void resetBindings() {
	}

	default int getMipmapUsage(GpuFormat format) {
		return 0;
	}

	public default GpuFormat getRenderTargetFormat(GpuFormat format) {
		return format;
	}

	@Override
	void close();

}
