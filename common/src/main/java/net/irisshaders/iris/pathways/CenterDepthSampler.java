package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanShaderPatcher;
import net.irisshaders.iris.samplers.SamplerBindingPlan;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;
import net.irisshaders.iris.vulkan.VulkanGraphicsPipeline;
import net.irisshaders.iris.vulkan.VulkanResourceSets;
import net.irisshaders.iris.vulkan.VulkanSamplerHeap;
import net.irisshaders.iris.vulkan.VulkanShaderCompiler;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class CenterDepthSampler {
	private final GpuDevice device;
	private final Supplier<GpuTexture> depthSupplier;
	private final GpuTexture texture, history;
	private final GpuTextureView outputView, historyView;
	private final GpuBuffer vertices;
	private final VulkanSamplerHeap heap;
	private final VulkanResourceSets uniformSets;
	private final BufferUniformHolder uniforms;
	private final VulkanGraphicsPipeline program;
	private final SamplerHeapLayout.Slot depthSlot, historySlot;
	private GpuTextureView depthView;
	private float frameTime;
	private boolean hasFirstSample;
	private boolean everRetrieved;
	private boolean destroyed;
	private int samples;

	public CenterDepthSampler(Supplier<GpuTexture> depthSupplier, float halfLife) {
		this(RenderSystem.getDevice(), VulkanCommandBridge.device(RenderSystem.getDevice()), depthSupplier, halfLife);
	}

	public CenterDepthSampler(GpuDevice device, VulkanDevice backend, Supplier<GpuTexture> depthSupplier, float halfLife) {
		if (!Float.isFinite(halfLife) || halfLife < 0) {
			throw new IllegalArgumentException("Invalid center-depth half-life");
		}

		this.device = device;
		this.depthSupplier = depthSupplier;

		var layout = new SamplerHeapLayout.Builder().add(SamplerHeapLayout.Type.SAMPLER2D, "depth", "nearest")
			.add(SamplerHeapLayout.Type.SAMPLER2D, "history", "nearest").build();
		depthSlot = layout.slot(SamplerHeapLayout.Type.SAMPLER2D, "depth", "nearest");
		historySlot = layout.slot(SamplerHeapLayout.Type.SAMPLER2D, "history", "nearest");

		var plan = new SamplerBindingPlan.Builder(layout).bind("depth", depthSlot).bind("altDepth", historySlot).build();
		var shader = VulkanShaderPatcher.patch(Map.of(PatchShaderType.VERTEX,
			"#version 450\nlayout(location=0)in vec3 iris_Position;void main(){gl_Position=vec4(iris_Position.xy*2.0-1.0,0,1);}",
			PatchShaderType.FRAGMENT,
			"#version 450\nuniform sampler2D depth;uniform sampler2D altDepth;uniform float lastFrameTime;uniform float decay;" +
				"uniform" + " " +
				"bool firstSample;layout(location=0)out float iris_fragColor;void main(){float currentDepth=1.0-texture(depth,vec2(0" +
				".5)).r;float oldDepth=texture(altDepth,vec2(0.5)).r;float weight=decay<0.0?1.0:1.0-exp(-decay*lastFrameTime);" +
				"iris_fragColor=firstSample||isnan(oldDepth)?currentDepth:mix(oldDepth,currentDepth,weight);}"), plan);
		uniforms = new BufferUniformHolder(shader.layout());
		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "lastFrameTime", () -> frameTime);
		uniforms.uniform1f(UniformUpdateFrequency.ONCE, "decay", () -> halfLife == 0 ? -1 : Math.log(2) / (halfLife * 0.1));
		uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "firstSample", () -> !hasFirstSample);

		var cleanup = new ArrayList<Runnable>();

		try {
			heap = new VulkanSamplerHeap(backend.vkDevice(), layout, 16);
			cleanup.add(heap::close);

			uniformSets = new VulkanResourceSets(backend.vkDevice(), shader.layout(), 16);
			cleanup.add(uniformSets::close);

			try (var compiler = new VulkanShaderCompiler()) {
				var vertex = compiler.compile("centerDepth.vsh", PatchShaderType.VERTEX, shader.sources().get(PatchShaderType.VERTEX));

				try {
					var fragment = compiler.compile("centerDepth.fsh", PatchShaderType.FRAGMENT,
						shader.sources().get(PatchShaderType.FRAGMENT));

					try {
						program = new VulkanGraphicsPipeline(backend.vkDevice(), heap, uniformSets, vertex, fragment,
							VK10.VK_FORMAT_R32_SFLOAT, 12,
							List.of(new VulkanGraphicsPipeline.Attribute(0, VK10.VK_FORMAT_R32G32B32_SFLOAT, 0)));
					} finally {
						MemoryUtil.memFree(fragment);
					}
				} finally {
					MemoryUtil.memFree(vertex);
				}
			}

			cleanup.add(program::close);

			int usage = GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST |
				GpuTexture.USAGE_RENDER_ATTACHMENT;
			texture = device.createTexture("Center depth", usage, GpuFormat.R32_FLOAT, 1, 1, 1, 1);
			cleanup.add(texture::close);
			history = device.createTexture("Center depth history", usage, GpuFormat.R32_FLOAT, 1, 1, 1, 1);
			cleanup.add(history::close);
			outputView = device.createTextureView(texture);
			cleanup.add(outputView::close);
			historyView = device.createTextureView(history);
			cleanup.add(historyView::close);
			device.createCommandEncoder().clearColorTexture(history, new Vector4f());

			try (var stack = MemoryStack.stackPush()) {
				var data = stack.malloc(36).order(ByteOrder.nativeOrder());
				data.asFloatBuffer().put(new float[]{0, 0, 0, 2, 0, 0, 0, 2, 0});
				vertices = device.createBuffer(() -> "Center depth vertices", GpuBuffer.USAGE_VERTEX, data);
				cleanup.add(vertices::close);
			}
		} catch (RuntimeException | Error failure) {
			for (var close : cleanup.reversed()) {
				try {
					close.run();
				} catch (RuntimeException | Error suppressed) {
					failure.addSuppressed(suppressed);
				}
			}

			throw failure;
		}
	}

	public void sampleCenterDepth() {
		sampleCenterDepth(SystemTimeUniforms.TIMER.getLastFrameTime());
	}

	public void sampleCenterDepth(float lastFrameTime) {
		if (destroyed || hasFirstSample && !everRetrieved) {
			return;
		}

		if (!Float.isFinite(lastFrameTime) || lastFrameTime < 0) {
			throw new IllegalArgumentException("Invalid frame time");
		}

		var depth = depthSupplier.get();

		if (depthView == null || depthView.texture() != depth) {
			var replacement = device.createTextureView(depth, 0, 1);

			if (depthView != null) {
				depthView.close();
			}

			depthView = replacement;
		}

		frameTime = lastFrameTime;
		uniforms.update(samples, samples);
		samples++;

		long sampler = heap.sampler(GlSampler.NEAREST);
		var snapshot = heap.snapshot(Map.of(depthSlot,
			new VulkanSamplerHeap.ImageSampler(((VulkanGpuTextureView) depthView).vkImageView(), sampler, VK10.VK_IMAGE_LAYOUT_GENERAL),
			historySlot,
			new VulkanSamplerHeap.ImageSampler(((VulkanGpuTextureView) historyView).vkImageView(), sampler,
				VK10.VK_IMAGE_LAYOUT_GENERAL)));
		var encoder = device.createCommandEncoder();

		try {
			VulkanCommandBridge.draw(encoder, device.getDeviceInfo().limits().minUniformOffsetAlignment(), program, uniforms, uniformSets,
				snapshot, outputView, vertices);
		} finally {
			VulkanCommandBridge.retire(encoder, snapshot::close);
		}

		encoder.copyTextureToTexture(texture, history, 0, 0, 0, 0, 0, 1, 1);
		hasFirstSample = true;
	}

	public GpuTextureView getCenterDepthTextureView() {
		return historyView;
	}

	public int getCenterDepthTexture() {
		throw new UnsupportedOperationException("Center depth is a Vulkan texture view; use getCenterDepthTextureView()");
	}

	public void setUsage(boolean usage) {
		everRetrieved |= usage;
	}

	public void destroy() {
		if (destroyed) {
			return;
		}

		destroyed = true;

		var encoder = device.createCommandEncoder();
		VulkanCommandBridge.retire(encoder, program::close);
		VulkanCommandBridge.retire(encoder, uniformSets::close);
		VulkanCommandBridge.retire(encoder, heap::close);

		if (depthView != null) {
			depthView.close();
		}

		outputView.close();
		historyView.close();
		texture.close();
		history.close();
		vertices.close();
	}
}
