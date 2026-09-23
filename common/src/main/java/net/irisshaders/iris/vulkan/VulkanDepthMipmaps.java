package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanShaderPatcher;
import net.irisshaders.iris.samplers.SamplerBindingPlan;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class VulkanDepthMipmaps implements AutoCloseable {
	private final Map<GpuTexture, List<GpuTextureView>> depthMipViews = new IdentityHashMap<>();
	private VulkanSamplerHeap depthMipHeap;
	private VulkanGraphicsPipeline depthMipPipeline;
	private SamplerHeapLayout.Slot depthMipSlot;

	private void initialize() {
		var backend = VulkanCommandBridge.getDevice();

		var layout = new SamplerHeapLayout.Builder().add(SamplerHeapLayout.Type.SAMPLER2D, "depth", "linear").build();
		this.depthMipSlot = layout.slot(SamplerHeapLayout.Type.SAMPLER2D, "depth", "linear");

		// this is stupid???
		var shader = VulkanShaderPatcher.patch(Map.of(PatchShaderType.VERTEX, """
				#version 450
				void main(){vec2 p=vec2((gl_VertexIndex<<1)&2,gl_VertexIndex&2);gl_Position=vec4(p*2.0-1.0,0,1);}""",
			PatchShaderType.FRAGMENT, """
				#version 450
				uniform sampler2D sourceDepth;
				void main() {
				    ivec2 size = max(textureSize(sourceDepth, 0) / 2, ivec2(1));
				    vec2 uv = gl_FragCoord.xy / vec2(size);
				    gl_FragDepth = textureLod(sourceDepth, uv, 0.0).r;
				}
				"""), new SamplerBindingPlan.Builder(layout).bind("sourceDepth", this.depthMipSlot).build());
		this.depthMipHeap = new VulkanSamplerHeap(backend, layout, 256);

		try (var compiler = new VulkanShaderCompiler()) {
			var vertex = compiler.compile("depth-mips.vsh", PatchShaderType.VERTEX, shader.sources().get(PatchShaderType.VERTEX));

			try {
				var fragment = compiler.compile("depth-mips.fsh", PatchShaderType.FRAGMENT, shader.sources().get(PatchShaderType.FRAGMENT));

				try {
					this.depthMipPipeline = new VulkanGraphicsPipeline(backend.vkDevice(), this.depthMipHeap, vertex, fragment,
						VK10.VK_FORMAT_D32_SFLOAT);
				} finally {
					MemoryUtil.memFree(fragment);
				}
			} finally {
				MemoryUtil.memFree(vertex);
			}
		} catch (RuntimeException | Error failure) {
			this.depthMipHeap.close();

			throw failure;
		}
	}

	public void generate(GpuTexture texture) {
		if (texture.getFormat() != GpuFormat.D32_FLOAT || texture.isClosed()) {
			throw new IllegalArgumentException("expected live D32 shadow depth");
		}

		if (texture.getMipLevels() < 2) {
			return;
		}

		if (this.depthMipPipeline == null) {
			this.initialize();
		}

		var levels = this.depthMipViews.computeIfAbsent(texture, image -> {
			var result = new ArrayList<GpuTextureView>();

			try {
				for (int i = 0; i < image.getMipLevels(); i++) {
					result.add(RenderSystem.getDevice().createTextureView(image, i, 1));
				}

				return result;
			} catch (RuntimeException | Error failure) {
				result.forEach(GpuTextureView::close);

				throw failure;
			}
		});
		var encoder = VulkanCommandBridge.encoder(RenderSystem.getDevice().createCommandEncoder());
		var command = VulkanCommandBridge.commandBuffer(encoder);

		try (var stack = MemoryStack.stackPush()) {
			VulkanCommandEncoder.memoryBarrier(command, stack);

			for (int level = 1; level < levels.size(); level++) {
				var source = (VulkanGpuTextureView) levels.get(level - 1);
				var target = (VulkanGpuTextureView) levels.get(level);
				var snapshot = this.depthMipHeap.snapshot(Map.of(this.depthMipSlot,
					new VulkanSamplerHeap.ImageSampler(source.vkImageView(), this.depthMipHeap.sampler(GlSampler.LINEAR),
						VK10.VK_IMAGE_LAYOUT_GENERAL)));
				encoder.queueForDestroy(snapshot::close);

				int width = Math.max(1, texture.getWidth(level));
				int height = Math.max(1, texture.getHeight(level));
				((VulkanRenderBackend) Iris.getRenderBackend()).getRenderPassCache()
					.begin(command, stack, new int[0], VK10.VK_FORMAT_D32_SFLOAT, new long[]{target.vkImageView()}, width, height,
						VK10.VK_IMAGE_LAYOUT_GENERAL, VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);

				try {
					VK10.vkCmdSetViewport(command, 0, VkViewport.calloc(1, stack).width(width).height(height).minDepth(0).maxDepth(1));

					var scissor = VkRect2D.calloc(1, stack);
					scissor.extent().set(width, height);
					VK10.vkCmdSetScissor(command, 0, scissor);
					this.depthMipPipeline.bind(command, snapshot, null);
					VK10.vkCmdDraw(command, 3, 1, 0, 0);
				} finally {
					VK10.vkCmdEndRenderPass(command);
				}

				VulkanCommandEncoder.memoryBarrier(command, stack);
			}
		}
	}

	public void release(GpuTexture texture) {
		var levels = this.depthMipViews.remove(texture);
		if (levels != null) {
			levels.forEach(GpuTextureView::close);
		}
	}

	@Override
	public void close() {
		this.depthMipViews.values().forEach(levels -> levels.forEach(GpuTextureView::close));
		this.depthMipViews.clear();

		if (this.depthMipPipeline == null) {
			return;
		}

		var encoder = VulkanCommandBridge.getDevice().createCommandEncoder();
		encoder.queueForDestroy(this.depthMipPipeline::close);
		encoder.queueForDestroy(this.depthMipHeap::close);
		this.depthMipPipeline = null;
		this.depthMipHeap = null;
	}

}
