package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import net.irisshaders.iris.mixin.VKOnly_FrontendDeviceAccessor;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanResourceLayout;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.VK10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class VulkanCommandBridge {
	private VulkanCommandBridge() {
	}

	public static VulkanDevice device(GpuDevice frontend) {
		if (!(frontend instanceof VKOnly_FrontendDeviceAccessor access) || !(access.iris$getBackend() instanceof VulkanDevice device)) {
			throw new IllegalArgumentException("Expected Minecraft's Vulkan device");
		}

		return device;
	}

	public static VulkanCommandEncoder encoder(CommandEncoder encoder) {
		if (!(encoder instanceof FrontendCommandEncoder frontend) || !(frontend.backend() instanceof VulkanCommandEncoder backend)) {
			throw new IllegalArgumentException("Expected Minecraft's Vulkan encoder");
		}

		if (frontend.isInRenderPass()) {
			throw new IllegalStateException("Iris native work must be recorded outside a Minecraft render pass");
		}

		return backend;
	}

	public static void draw(CommandEncoder frontend, int uniformAlignment, VulkanGraphicsPipeline pipeline, BufferUniformHolder data,
		VulkanResourceSets uniformSets, VulkanSamplerHeap.Snapshot heap, GpuTextureView output, GpuBuffer vertices) {
		draw(frontend, uniformAlignment, pipeline, data, uniformSets, Map.of(), heap, output, vertices);
	}

	public static void draw(CommandEncoder frontend, int uniformAlignment, VulkanGraphicsPipeline pipeline, BufferUniformHolder data,
		VulkanResourceSets uniformSets, Map<String, List<GpuBufferSlice>> existingBlocks, VulkanSamplerHeap.Snapshot heap,
		GpuTextureView output, GpuBuffer vertices) {
		draw(frontend, uniformAlignment, pipeline, data, uniformSets, existingBlocks, heap, List.of(output), vertices);
	}

	public static void draw(CommandEncoder frontend, int uniformAlignment, VulkanGraphicsPipeline pipeline, BufferUniformHolder data,
		VulkanResourceSets uniformSets, Map<String, List<GpuBufferSlice>> existingBlocks, VulkanSamplerHeap.Snapshot heap,
		List<GpuTextureView> outputs, GpuBuffer vertices) {
		drawBatch(frontend, uniformAlignment, List.of(new Draw(pipeline, data, uniformSets, existingBlocks, heap, vertices)), outputs);
	}

	public static void drawBatch(CommandEncoder frontend, int uniformAlignment, List<Draw> draws, List<GpuTextureView> outputs) {
		if (draws.isEmpty()) {
			throw new IllegalArgumentException("No draws in rendering scope");
		}

		VulkanCommandEncoder encoder = encoder(frontend);

		if (outputs.isEmpty()) {
			throw new IllegalArgumentException("No output attachments");
		}

		var output = outputs.getFirst();
		int[] formats = draws.getFirst().pipeline().colorFormats();

		if (outputs.size() != formats.length) {
			throw new IllegalArgumentException("Pipeline and scope attachment counts differ");
		}

		long[] views = new long[outputs.size()];

		for (int i = 0; i < outputs.size(); i++) {
			var view = outputs.get(i);

			if (!(view instanceof VulkanGpuTextureView nativeView) || view.isClosed() || view.texture().isClosed() ||
				view.mipLevels() != 1 || !view.texture().getFormat().hasColorAspect() ||
				(view.texture().usage() & GpuTexture.USAGE_RENDER_ATTACHMENT) == 0 ||
				VulkanConst.toVk(view.texture().getFormat()) != formats[i] || view.getWidth(0) != output.getWidth(0) ||
				view.getHeight(0) != output.getHeight(0)) {
				throw new IllegalArgumentException("Invalid Vulkan color attachments");
			}

			views[i] = nativeView.vkImageView();
		}

		var snapshots = new ArrayList<VulkanResourceSets.Snapshot>();
		boolean queued = false;

		try {
			for (var draw : draws) {
				if (!Arrays.equals(draws.getFirst().pipeline().colorFormats(), draw.pipeline().colorFormats())) {
					throw new IllegalArgumentException("Incompatible merged pipeline formats");
				}

				if (!(draw.vertices() instanceof VulkanGpuBuffer) || draw.vertices().isClosed()) {
					throw new IllegalArgumentException("Invalid Vulkan vertex buffer");
				}

				if (draw.indices() != null && (!(draw.indices() instanceof VulkanGpuBuffer) || draw.indices().isClosed() ||
					(draw.indices().usage() & GpuBuffer.USAGE_INDEX) == 0 || draw.indices().size() < 12)) {
					throw new IllegalArgumentException("Invalid Vulkan fullscreen quad index buffer");
				}

				var uniformSets = draw.uniformSets();

				if (uniformSets == null && (!draw.existingBlocks().isEmpty() || draw.data() != null && draw.data().data().hasRemaining())) {
					throw new IllegalArgumentException("No resource layout for supplied blocks");
				}

				VulkanResourceSets.Snapshot uniforms = null;

				if (uniformSets != null) {
					Map<String, List<VulkanResourceSets.BufferSlice>> slices = new LinkedHashMap<>();

					for (var entry : draw.existingBlocks().entrySet()) {
						slices.put(entry.getKey(), entry.getValue().stream().map(VulkanCommandBridge::bufferSlice).toList());
					}

					if (draw.data() != null && draw.data().data().hasRemaining()) {
						if (slices.containsKey(VulkanResourceLayout.UNIFORM_BLOCK)) {
							throw new IllegalArgumentException("Generated pack uniforms cannot be overridden");
						}

						slices.put(VulkanResourceLayout.UNIFORM_BLOCK,
							List.of(bufferSlice(draw.data().upload(frontend, uniformAlignment))));
					}

					uniforms = uniformSets.snapshot(slices, draw.images());
				}

				if (draw.data() != null && draw.data().frameUniforms() != null) {
					draw.data().frameUniforms().snapshot();
				}

				snapshots.add(uniforms);
			}

			var command = encoder.allocateAndBeginTransientCommandBuffer();

			for (int i = 0; i < draws.size(); i++) {
				var draw = draws.get(i);
				draw.pipeline().validateDraw(command, draw.heap(), snapshots.get(i), ((VulkanGpuBuffer) draw.vertices()).vkBuffer());
			}

			try (var stack = MemoryStack.stackPush()) {
				CompositeSynchronization.beforeDraw(command, stack, outputs);

				VulkanGraphicsPipeline.beginRendering(command, stack, views, output.getWidth(0), output.getHeight(0),
					VK10.VK_IMAGE_LAYOUT_GENERAL);
				try {
					for (int i = 0; i < draws.size(); i++) {
						var draw = draws.get(i);

						if (draw.data() != null && draw.data().frameUniforms() != null) {
							draw.pipeline().bindFrame(command, draw.data().frameUniforms().snapshot());
						}

						draw.pipeline().recordDraw(command, draw.heap(), snapshots.get(i), output.getWidth(0), output.getHeight(0),
							((VulkanGpuBuffer) draw.vertices()).vkBuffer(), draw.viewport(),
							draw.indices() == null ? 0 : ((VulkanGpuBuffer) draw.indices()).vkBuffer());
					}
				} finally {
					KHRDynamicRendering.vkCmdEndRenderingKHR(command);
				}

				CompositeSynchronization.after(command, stack);

				int result = VK10.vkEndCommandBuffer(command);

				if (result != VK10.VK_SUCCESS) {
					throw new IllegalStateException("Could not finish Iris pass: " + result);
				}
			}

			encoder.execute(command);
			queued = true;
		} finally {
			for (var uniforms : snapshots) {
				if (uniforms == null) {
					continue;
				}

				if (queued) {
					encoder.queueForDestroy(uniforms::close);
				} else {
					uniforms.close();
				}
			}
		}
	}

	public static VulkanResourceSets.BufferSlice bufferSlice(GpuBufferSlice slice) {
		if (!(slice.buffer() instanceof VulkanGpuBuffer buffer) || buffer.isClosed() ||
			(buffer.usage() & (GpuBuffer.USAGE_UNIFORM | ShaderStorageBuffer.USAGE_STORAGE)) == 0 || slice.offset() < 0 ||
			slice.length() <= 0 || slice.offset() > buffer.size() - slice.length()) {
			throw new IllegalArgumentException("Invalid Vulkan buffer slice");
		}

		return new VulkanResourceSets.BufferSlice(buffer.vkBuffer(), slice.offset(), slice.length());
	}

	public static void retire(CommandEncoder encoder, Runnable destructor) {
		encoder(encoder).queueForDestroy(destructor::run);
	}

	public record Draw(VulkanGraphicsPipeline pipeline, BufferUniformHolder data, VulkanResourceSets uniformSets,
		Map<String, List<GpuBufferSlice>> existingBlocks, VulkanSamplerHeap.Snapshot heap, GpuBuffer vertices, ViewportData viewport,
		Map<String, List<Long>> images, GpuBuffer indices) {
		public Draw(VulkanGraphicsPipeline pipeline, BufferUniformHolder data, VulkanResourceSets uniformSets,
			Map<String, List<GpuBufferSlice>> existingBlocks, VulkanSamplerHeap.Snapshot heap, GpuBuffer vertices, ViewportData viewport,
			Map<String, List<Long>> images) {
			this(pipeline, data, uniformSets, existingBlocks, heap, vertices, viewport, images, null);
		}

		public Draw(VulkanGraphicsPipeline pipeline, BufferUniformHolder data, VulkanResourceSets uniformSets,
			Map<String, List<GpuBufferSlice>> existingBlocks, VulkanSamplerHeap.Snapshot heap, GpuBuffer vertices) {
			this(pipeline, data, uniformSets, existingBlocks, heap, vertices, ViewportData.defaultValue());
		}

		public Draw(VulkanGraphicsPipeline pipeline, BufferUniformHolder data, VulkanResourceSets uniformSets,
			Map<String, List<GpuBufferSlice>> existingBlocks, VulkanSamplerHeap.Snapshot heap, GpuBuffer vertices, ViewportData viewport) {
			this(pipeline, data, uniformSets, existingBlocks, heap, vertices, viewport, Map.of());
		}

		public Draw {
			Objects.requireNonNull(viewport);

			if (!Float.isFinite(viewport.scale()) || viewport.scale() < 0 || !Float.isFinite(viewport.viewportX()) ||
				!Float.isFinite(viewport.viewportY())) {
				throw new IllegalArgumentException("Invalid composite viewport");
			}

			Objects.requireNonNull(pipeline);
			Objects.requireNonNull(heap);

			var copy = new LinkedHashMap<String, List<GpuBufferSlice>>();
			existingBlocks.forEach((name, blocks) -> copy.put(name, List.copyOf(blocks)));
			existingBlocks = Map.copyOf(copy);
		}
	}
}
