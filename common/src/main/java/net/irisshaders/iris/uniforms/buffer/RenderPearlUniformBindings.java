package net.irisshaders.iris.uniforms.buffer;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.UniformType;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanResourceLayout;

public final class RenderPearlUniformBindings {
	private RenderPearlUniformBindings() {
	}

	public static BindGroupLayout createLayout(VulkanResourceLayout resources) {
		var builder = BindGroupLayout.builder();

		for (var descriptor : resources.descriptors()) {
			if (descriptor.count() != 1) {
				throw new UnsupportedOperationException("RenderPearl cannot bind descriptor arrays: " + descriptor.name());
			}

			UniformType type = switch (descriptor.kind()) {
				case UNIFORM_BUFFER -> UniformType.UNIFORM_BUFFER;
				case SAMPLER -> {
					if (descriptor.type().contains("BUFFER")) {
						throw new UnsupportedOperationException("Texel buffer format must be supplied: " + descriptor.name());
					}

					yield UniformType.COMBINED_IMAGE_SAMPLER;
				}
				case IMAGE, STORAGE_BUFFER ->
					throw new UnsupportedOperationException("RenderPearl needs storage resource support: " + descriptor.name());
			};
			builder.withUniform(descriptor.name(), type);
		}

		return builder.build();
	}

	public static void bind(RenderPass pass, GpuBufferSlice uniforms) {
		pass.setUniform(VulkanResourceLayout.UNIFORM_BLOCK, uniforms);
	}
}
