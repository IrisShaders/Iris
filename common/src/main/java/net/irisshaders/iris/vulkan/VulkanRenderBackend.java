package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.ComputePipeline;
import net.irisshaders.iris.backend.GraphicsShader;
import net.irisshaders.iris.backend.IrisGpuFormats;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.backend.PackResources;
import net.irisshaders.iris.backend.RenderBackend;
import net.irisshaders.iris.backend.WorldPassCommands;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.mixinterface.VulkanWorldPassAccess;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.ProgramSamplers;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.uniforms.buffer.DrawUniforms;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFormatProperties;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class VulkanRenderBackend implements RenderBackend {
	public final VulkanTextureState textures = new VulkanTextureState();
	private final VulkanSynchronization synchronization = new VulkanSynchronization();
	private final VulkanColorMipmaps mipmaps = new VulkanColorMipmaps();
	private final VulkanDepthMipmaps depthMipmaps = new VulkanDepthMipmaps();
	private final Map<GpuFormat, GpuFormat> renderTargetFormats = new EnumMap<>(GpuFormat.class);
	private final VulkanRenderPassCache renderPassCache = new VulkanRenderPassCache(VulkanCommandBridge.getDevice().vkDevice());
	private VulkanShaderCache shaderCache;
	public VulkanSamplerHeap activeHeap;
	private volatile boolean closed;

	public VulkanRenderBackend() {
		((VulkanWorldPassAccess) VulkanCommandBridge.getDevice().createCommandEncoder()).iris$setTextureState(this.textures);
	}

	@Override
	public WorldPassCommands createRenderPass(RenderPassDescriptor descriptor) {
		return new VulkanWorldCommands(this.textures.takeClears(descriptor));
	}

	@Override
	public WorldPassCommands adaptRenderPass(RenderPassBackend pass, RenderArea area) {
		return new VulkanWorldCommands(pass, false);
	}

	@Override
	public PackResources createPackResources(PackSamplers pack) {
		return new VulkanPackResources(pack);
	}

	@Override
	public GraphicsShader createGraphicsShader(String name, PatchedShader source, ProgramSamplers resources,
		DrawUniforms uniforms) {
		return new VulkanGraphicsShader(this.getShaderCache(), name, source, resources, uniforms);
	}

	@Override
	public ComputePipeline createComputePipeline(String name, PatchedShader source, ProgramSamplers resources,
		DrawUniforms uniforms) {
		return new VulkanComputePipeline(this.getShaderCache(), name, source, resources, uniforms);
	}

	@Override
	public void finishCompilation() {
		if (this.shaderCache != null) {
			this.shaderCache.finishCompilation();
		}
	}

	private VulkanShaderCache getShaderCache() {
		if (this.shaderCache == null) {
			this.shaderCache = new VulkanShaderCache(VulkanCommandBridge.getDevice().vkDevice());
		}

		return this.shaderCache;
	}

	public VulkanRenderPassCache getRenderPassCache() {
		return this.renderPassCache;
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		if (this.shaderCache != null) {
			this.shaderCache.finishCompilation();
		}

		this.textures.flush();
		this.closed = true;
		this.mipmaps.close();
		this.depthMipmaps.close();

		if (this.shaderCache != null) {
			this.retire(this.shaderCache::close);
		}

		this.renderPassCache.close();
	}

	@Override
	public IrisImage createImage(String name, TextureType type, GpuFormat format, int width, int height, int depth, boolean storage) {
		return new VulkanImage(VulkanCommandBridge.getDevice(), name, type, format, width, height, depth, storage);
	}

	@Override
	public GpuBuffer createStorageBuffer(String name, long size, byte[] content) {
		return new VulkanStorageBuffer(VulkanCommandBridge.getDevice(), name, size, content);
	}

	@Override
	public void retire(Runnable destructor) {
		VulkanCommandBridge.deleteWhenDone(destructor);
	}

	@Override
	public void sync(long stage, long reads, long writes) {
		this.synchronization.sync(stage, reads, writes);
	}

	@Override
	public void generateMipmaps(GpuTexture texture, boolean linear) {
		if (texture.getMipLevels() <= 1) {
			return;
		}
		this.textures.beforeAccess(texture);
		var command = VulkanCommandBridge.commandBuffer();
		var debug = VulkanCommandBridge.getDevice().instance().debug();
		debug.beginDebugGroup(command, () -> "Mipmaps: " + texture.getLabel());
		try {
			if (!linear || !this.mipmaps.generate(texture, this.activeHeap)) {
				VulkanTextureOps.generateMipmaps(texture, linear);
			}
		} finally {
			debug.endDebugGroup(command);
		}
	}

	@Override
	public int getMipmapUsage(GpuFormat format) {
		return this.mipmaps.supports(format) ? RenderTarget.USAGE_STORAGE : 0;
	}

	@Override
	public GpuFormat getRenderTargetFormat(GpuFormat format) {
		return this.renderTargetFormats.computeIfAbsent(format, requested -> {
			try (var stack = MemoryStack.stackPush()) {
				var properties = VkFormatProperties.calloc(stack);
				VK10.vkGetPhysicalDeviceFormatProperties(VulkanCommandBridge.getDevice().vkDevice().getPhysicalDevice(),
					VulkanConst.toVk(requested), properties);
				if ((properties.optimalTilingFeatures() & VK10.VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BIT) != 0) {
					return requested;
				}
			}
			// Packed formats can support sampling without supporting color attachments.
			var replacement = requested == IrisGpuFormats.RGBA4_UNORM ? GpuFormat.RGBA8_UNORM
				: requested == IrisGpuFormats.RGB9E5_FLOAT ? GpuFormat.RGBA16_FLOAT : null;
			if (replacement == null) {
				throw new UnsupportedOperationException("Vulkan does not support render target format " + requested);
			}
			Iris.logger.warn("Vulkan does not support {} as a render target; using {}", requested, replacement);
			return replacement;
		});
	}

	public void onDeleteTexture(GpuTexture texture) {
		this.textures.release(texture);
		this.mipmaps.release(texture);
		this.depthMipmaps.release(texture);
	}

	@Override
	public void clearColors(List<GpuTextureView> textures, Vector4f color) {
		this.textures.clear(textures, color);
	}

	@Override
	public void generateDepthMips(GpuTexture texture) {
		if (texture.getMipLevels() <= 1) {
			return;
		}
		this.textures.beforeAccess(texture);
		var command = VulkanCommandBridge.commandBuffer();
		var debug = VulkanCommandBridge.getDevice().instance().debug();
		debug.beginDebugGroup(command, () -> "Depth mipmaps: " + texture.getLabel());
		try {
			this.depthMipmaps.generate(texture, this.activeHeap);
		} finally {
			debug.endDebugGroup(command);
		}
	}
}
