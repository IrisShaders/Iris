package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import net.irisshaders.iris.backend.ComputePipeline;
import net.irisshaders.iris.backend.GraphicsShader;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.backend.NonRenderCommands;
import net.irisshaders.iris.backend.PackResources;
import net.irisshaders.iris.backend.RenderBackend;
import net.irisshaders.iris.backend.SyncCommand;
import net.irisshaders.iris.backend.WorldPassCommands;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.List;

// not sure if i'm happy with this yet... AI designed a lot of it, but it seems mostly logical and I don't feel like rewriting it right now?
public class VulkanRenderBackend implements RenderBackend, NonRenderCommands {
	public final VulkanSynchronization synchronization = new VulkanSynchronization();
	private final VulkanColorMipmaps mipmaps = new VulkanColorMipmaps();
	private final VulkanDepthMipmaps depthMipmaps = new VulkanDepthMipmaps();
	private final VulkanRenderPassCache renderPassCache = new VulkanRenderPassCache(VulkanCommandBridge.getDevice().vkDevice());
	private VulkanShaderCache shaderCache;
	private volatile boolean closed;

	@Override
	public WorldPassCommands createRenderPass(CommandEncoderBackend encoder, RenderPassDescriptor descriptor) {
		return new VulkanWorldCommands((VulkanCommandEncoder) encoder, descriptor);
	}

	@Override
	public WorldPassCommands adaptRenderPass(RenderPassBackend pass, RenderArea area) {
		return new VulkanWorldCommands(
			(VulkanCommandEncoder) ((FrontendCommandEncoder) RenderSystem.getDevice().createCommandEncoder()).backend(), pass, false);
	}

	@Override
	public NonRenderCommands commands() {
		return this;
	}

	@Override
	public SyncCommand createSync() {
		return new VulkanSyncCommand();
	}

	@Override
	public void dispatch(ComputePipeline pipeline, Vector3i groups, FilledIndirectPointer indirect) {
		((VulkanComputePipeline) pipeline).dispatch(groups, indirect);
	}

	@Override
	public PackResources createPackResources(PackSamplers pack) {
		return new VulkanPackResources(pack);
	}

	@Override
	public GraphicsShader createGraphicsShader(String name, PatchedShader source, ProgramResources resources,
		BufferUniformHolder uniforms) {
		return new VulkanGraphicsShader(this.getShaderCache(), name, source, resources, uniforms);
	}

	@Override
	public ComputePipeline createComputePipeline(String name, PatchedShader source, ProgramResources resources,
		BufferUniformHolder uniforms) {
		return new VulkanComputePipeline(this.getShaderCache(), name, source, resources, uniforms);
	}

	@Override
	public void finishCompilation() {
		if (this.shaderCache != null) {
			this.shaderCache.finishCompilation();
		}
	}

	private VulkanShaderCache getShaderCache() {
		if (this.closed) {
			throw new IllegalStateException("Vulkan render backend is closed");
		}

		if (this.shaderCache == null) {
			this.shaderCache = new VulkanShaderCache(VulkanCommandBridge.getDevice().vkDevice());
		}

		return this.shaderCache;
	}

	public VulkanRenderPassCache getRenderPassCache() {
		if (this.closed) {
			throw new IllegalStateException("Vulkan render backend is closed");
		}

		return this.renderPassCache;
	}

	public void onDeleteTextureView(long view) {
		this.renderPassCache.release(view, VulkanCommandBridge.getDevice().createCommandEncoder());
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		if (this.shaderCache != null) {
			this.shaderCache.finishCompilation();
		}

		this.closed = true;
		this.mipmaps.close();
		this.depthMipmaps.close();

		if (this.shaderCache != null) {
			this.retire(this.shaderCache::close);
		}

		this.renderPassCache.close(VulkanCommandBridge.getDevice().createCommandEncoder());
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
		VulkanCommandBridge.retire(RenderSystem.getDevice().createCommandEncoder(), destructor);
	}

	@Override
	public void generateMipmaps(GpuTexture texture, boolean linear) {
		if (!linear || !this.mipmaps.generate(texture)) {
			VulkanTextureOps.generateMipmaps(texture, linear);
		}
	}

	@Override
	public void copyTexture(GpuTexture source, GpuTexture destination, int width, int height) {
		VulkanTextureOps.copyTexture(source, destination, width, height);
	}

	@Override
	public int getMipmapUsage(GpuFormat format) {
		return this.mipmaps.supports(format) ? RenderTarget.USAGE_STORAGE : 0;
	}

	public void onDeleteTexture(GpuTexture texture) {
		this.mipmaps.release(texture);
		this.depthMipmaps.release(texture);
	}

	@Override
	public void clearColors(List<GpuTextureView> textures, Vector4f color) {
		VulkanTextureOps.clearColors(textures, color);
	}

	@Override
	public void generateDepthMips(GpuTexture texture) {
		this.depthMipmaps.generate(texture);
	}
}
