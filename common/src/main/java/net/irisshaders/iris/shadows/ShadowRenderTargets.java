package net.irisshaders.iris.shadows;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanShaderPatcher;
import net.irisshaders.iris.samplers.SamplerBindingPlan;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import net.irisshaders.iris.shaderpack.properties.PackShadowDirectives;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;
import net.irisshaders.iris.vulkan.VulkanGraphicsPipeline;
import net.irisshaders.iris.vulkan.VulkanSamplerHeap;
import net.irisshaders.iris.vulkan.VulkanShaderCompiler;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderingAttachmentInfoKHR;
import org.lwjgl.vulkan.VkRenderingInfoKHR;
import org.lwjgl.vulkan.VkViewport;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ShadowRenderTargets {
	private final RenderTarget[] targets;
	private final GpuDevice device;
	private final GpuTextureView[] depthViews;
	private final PackShadowDirectives shadowDirectives;
	private final GpuTexture mainDepth;
	private final GpuTexture noTranslucents;
	private final GlFramebuffer depthSourceFb;
	private final GlFramebuffer noTranslucentsDestFb;
	private final boolean[] flipped;

	private final List<GlFramebuffer> ownedFramebuffers;
	private final int resolution;

	private VulkanSamplerHeap depthMipHeap;
	private VulkanGraphicsPipeline depthMipPipeline;
	private SamplerHeapLayout.Slot depthMipSlot;
	private final Map<GpuTexture, List<GpuTextureView>> depthMipViews = new IdentityHashMap<>();

	private final boolean[] hardwareFiltered;
	private final boolean[] mipped;
	private final boolean[] linearFiltered;
	private final InternalTextureFormat[] formats;
	private final IntList buffersToBeCleared;
	private final int size;
	private boolean fullClearRequired;

	public ShadowRenderTargets(WorldRenderingPipeline pipeline, int resolution, PackShadowDirectives shadowDirectives) {
		this(RenderSystem.getDevice(), pipeline.hasFeature(FeatureFlags.HIGHER_SHADOWCOLOR), resolution, shadowDirectives);
	}

	public ShadowRenderTargets(GpuDevice device, boolean higherShadowColor, int resolution, PackShadowDirectives shadowDirectives) {
		this.device = device;

		if (!device.getDeviceInfo().backendName().equalsIgnoreCase("Vulkan")) {
			throw new UnsupportedOperationException("Iris requires Vulkan");
		}

		this.shadowDirectives = shadowDirectives;
		this.size =
			higherShadowColor ? PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_IRIS : PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_OF;
		targets = new RenderTarget[size];
		formats = new InternalTextureFormat[size];
		flipped = new boolean[size];
		hardwareFiltered = new boolean[size];
		mipped = new boolean[size];
		linearFiltered = new boolean[size];
		buffersToBeCleared = new IntArrayList();


		this.ownedFramebuffers = new ArrayList<>();
		this.resolution = resolution;

		for (int i = 0; i < shadowDirectives.getDepthSamplingSettings().size(); i++) {
			this.hardwareFiltered[i] = shadowDirectives.getDepthSamplingSettings().get(i).getHardwareFiltering();
			this.mipped[i] = shadowDirectives.getDepthSamplingSettings().get(i).getMipmap();
			this.linearFiltered[i] = !shadowDirectives.getDepthSamplingSettings().get(i).getNearest();
		}

		this.mainDepth = device.createTexture("Shadow Map",
			GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
			GpuFormat.D32_FLOAT, resolution, resolution, 1, this.mipped[0] ? log2(resolution) + 1 : 1);
		this.noTranslucents = device.createTexture("Shadow Map / Opaque",
			GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, GpuFormat.D32_FLOAT,
			resolution, resolution, 1, this.mipped[1] ? log2(resolution) + 1 : 1);
		depthViews = new GpuTextureView[]{device.createTextureView(mainDepth), device.createTextureView(noTranslucents)};

		// NB: Make sure all buffers are cleared so that they don't contain undefined
		// data. Otherwise very weird things can happen.
		fullClearRequired = true;

		this.depthSourceFb = createFramebufferWritingToMain(new int[]{0});

		this.noTranslucentsDestFb = createFramebufferWritingToMain(new int[]{0});
		this.noTranslucentsDestFb.addDepthAttachment(this.noTranslucents);

	}

	private static final double LN_OF_2 = Math.log(2.0);

	public static int log2(int val) {
		return (int) Math.floor(Math.log(val) / LN_OF_2);
	}

	// TODO: Actually flip. This is required for shadow composites!
	public void flip(int target) {
		flipped[target] = !flipped[target];
	}

	public boolean isFlipped(int target) {
		return flipped[target];
	}

	public void destroy() {
		if (depthMipPipeline != null) {
			destroyDepthMipmaps();
		}

		for (GlFramebuffer owned : ownedFramebuffers) {
			owned.destroy();
		}

		for (RenderTarget target : targets) {
			if (target != null) {
				target.destroy();
			}
		}

		for (var view : depthViews) {
			view.close();
		}

		mainDepth.close();
		noTranslucents.close();
	}

	public int maxDrawBuffers() {
		return device.getDeviceInfo().limits().maxColorAttachments();
	}

	public GpuTextureView getDepthTextureView(int index) {
		return depthViews[index];
	}

	public int getRenderTargetCount() {
		return targets.length;
	}

	public RenderTarget get(int index) {
		return targets[index];
	}

	/**
	 * Gets the render target assigned to an index, and creates it if it does not exist.
	 * This is a <b>expensive</b> opetation nad may block other tasks! Use it sparingly, and use {@code get()} if possible.
	 *
	 * @param index The index of the render target to get
	 * @return The existing or a new render target, if no existing one exists
	 */
	public RenderTarget getOrCreate(int index) {
		if (targets[index] != null) {
			return targets[index];
		}

		create(index);

		return targets[index];
	}

	private void create(int index) {
		if (index > size) {
			throw new IllegalStateException("Tried to access buffer higher than allowed limit of " + size +
				"! If you're trying to use shadowcolor2-7, you need to activate it's feature flag!");
		}


		PackShadowDirectives.SamplingSettings settings = shadowDirectives.getColorSamplingSettings()
			.computeIfAbsent(index, i -> new PackShadowDirectives.SamplingSettings());
		targets[index] = RenderTarget.builder().setDevice(device).setDimensions(resolution, resolution)
			.setInternalFormat(settings.getFormat()).setName("shadowcolor" + index).setPixelFormat(settings.getFormat().getPixelFormat())
			.build();
		formats[index] = settings.getFormat();

		if (settings.getClear()) {
			buffersToBeCleared.add(index);
		}

		if (settings.getClear()) {
			buffersToBeCleared.add(index);
		}

		fullClearRequired = true;
	}

	public void createIfEmpty(int index) {
		if (targets[index] == null) {
			create(index);
		}
	}

	public int getResolution() {
		return resolution;
	}

	public GpuTexture getDepthTexture() {
		return mainDepth;
	}

	public GpuTexture getDepthTextureNoTranslucents() {
		return noTranslucents;
	}

	public GlFramebuffer getDepthSourceFb() {
		return depthSourceFb;
	}

	public void copyPreTranslucentDepth() {
		device.createCommandEncoder().copyTextureToTexture(mainDepth, noTranslucents, 0, 0, 0, 0, 0, resolution, resolution);
	}

	public boolean isFullClearRequired() {
		return fullClearRequired;
	}

	public void onFullClear() {
		fullClearRequired = false;
	}

	public GlFramebuffer createFramebufferWritingToMain(int[] drawBuffers) {
		return createFullFramebuffer(false, drawBuffers);
	}

	public GlFramebuffer createFramebufferWritingToAlt(int[] drawBuffers) {
		return createFullFramebuffer(true, drawBuffers);
	}

	private ImmutableSet<Integer> invert(ImmutableSet<Integer> base, int[] relevant) {
		ImmutableSet.Builder<Integer> inverted = ImmutableSet.builder();

		for (int i : relevant) {
			if (!base.contains(i)) {
				inverted.add(i);
			}
		}

		return inverted.build();
	}

	private GlFramebuffer createEmptyFramebuffer() {
		GlFramebuffer framebuffer = new GlFramebuffer(device);
		ownedFramebuffers.add(framebuffer);

		framebuffer.addDepthAttachment(mainDepth);

		// NB: Before OpenGL 3.0, all framebuffers are required to have a color
		// attachment no matter what.
		framebuffer.addColorAttachment(0, getOrCreate(0), false);
		framebuffer.noDrawBuffers();

		return framebuffer;
	}

	public GlFramebuffer createDHFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers);
		framebuffer.addDepthAttachment(mainDepth);

		return framebuffer;
	}

	public GlFramebuffer createShadowFramebuffer(ImmutableSet<Integer> stageWritesToAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = invert(stageWritesToAlt, drawBuffers);

		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers);
		framebuffer.addDepthAttachment(mainDepth);

		return framebuffer;
	}

	private GlFramebuffer createFullFramebuffer(boolean clearsAlt, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			return createEmptyFramebuffer();
		}

		ImmutableSet<Integer> stageWritesToMain = ImmutableSet.of();

		if (!clearsAlt) {
			stageWritesToMain = invert(ImmutableSet.of(), drawBuffers);
		}

		return createColorFramebufferWithDepth(stageWritesToMain, drawBuffers);
	}

	public GlFramebuffer createColorFramebufferWithDepth(ImmutableSet<Integer> stageWritesToMain, int[] drawBuffers) {
		GlFramebuffer framebuffer = createColorFramebuffer(stageWritesToMain, drawBuffers);
		framebuffer.addDepthAttachment(mainDepth);

		return framebuffer;
	}

	public GlFramebuffer createColorFramebuffer(ImmutableSet<Integer> stageWritesToMain, int[] drawBuffers) {
		if (drawBuffers.length == 0) {
			throw new IllegalArgumentException("Framebuffer must have at least one color buffer");
		}

		GlFramebuffer framebuffer = new GlFramebuffer(device);
		ownedFramebuffers.add(framebuffer);

		int[] actualDrawBuffers = new int[drawBuffers.length];

		for (int i = 0; i < drawBuffers.length; i++) {
			actualDrawBuffers[i] = i;

			if (drawBuffers[i] >= getRenderTargetCount()) {
				// If a shader is using an invalid drawbuffer, they're most likely relying on the Optifine behavior of ignoring
				// DRAWBUFFERS in the shadow pass.
				// We need to fix this for them, since apparantly this is a common issue.
				// Iris.logger.warn("Invalid framebuffer was attempted to be created! Forcing a framebuffer with DRAWBUFFERS 01 for shadow
				// .");
				ownedFramebuffers.remove(framebuffer);
				framebuffer.destroy();

				return createColorFramebuffer(stageWritesToMain, new int[]{0, 1});
			}

			RenderTarget target = this.getOrCreate(drawBuffers[i]);
			framebuffer.addColorAttachment(i, target, !stageWritesToMain.contains(drawBuffers[i]));
		}

		framebuffer.drawBuffers(actualDrawBuffers);
		framebuffer.readBuffer(0);

		framebuffer.validateAttachments();

		return framebuffer;
	}

	public GlFramebuffer createMergedFramebuffer(ColorAttachmentPlan plan) {
		GlFramebuffer framebuffer = new GlFramebuffer(device);

		try {
			for (int i = 0; i < plan.targets().size(); i++) {
				var target = plan.targets().get(i);
				framebuffer.addColorAttachment(i, getOrCreate(target.index()), target.alt());
			}

			framebuffer.drawBuffers(IntStream.range(0, plan.targets().size()).toArray());
			framebuffer.addDepthAttachment(mainDepth);
			framebuffer.validateAttachments();
			ownedFramebuffers.add(framebuffer);

			return framebuffer;
		} catch (RuntimeException | Error failure) {
			framebuffer.destroy();

			throw failure;
		}
	}

	public int getColorTextureId(int i) {
		return isFlipped(i) ? get(i).getAltTexture() : get(i).getMainTexture();
	}

	public boolean isHardwareFiltered(int i) {
		return hardwareFiltered[i];
	}

	public int getNumColorTextures() {
		return targets.length;
	}

	public InternalTextureFormat getColorTextureFormat(int index) {
		return formats[index];
	}

	public ImmutableSet<Integer> snapshot() {
		ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();

		for (int i = 0; i < flipped.length; i++) {
			if (flipped[i]) {
				builder.add(i);
			}
		}

		return builder.build();
	}

	public IntList getBuffersToBeCleared() {
		return buffersToBeCleared;
	}

	public void generateMipmaps() {
		for (int i = 0; i < 2; i++) {
			if (mipped[i]) {
				generateDepthMipmaps(i == 0 ? mainDepth : noTranslucents);
			}
		}

		for (int i = 0; i < targets.length; i++) {
			var target = targets[i];

			if (target == null) {
				continue;
			}

			var settings = shadowDirectives.getColorSamplingSettings().get(i);

			if (settings != null && settings.getMipmap()) {
				target.generateMipmaps(false);
			}
		}
	}

	public GlSampler getSamplerFor(int i) {
		if (hardwareFiltered[i]) {
			if (linearFiltered[i]) {
				if (mipped[i]) {
					return GlSampler.MIPPED_LINEAR_HW;
				} else {
					return GlSampler.LINEAR_HW;
				}
			} else {
				if (mipped[i]) {
					return GlSampler.MIPPED_NEAREST_HW;
				} else {
					return GlSampler.NEAREST_HW;
				}
			}
		} else {
			if (linearFiltered[i]) {
				if (mipped[i]) {
					return GlSampler.MIPPED_LINEAR;
				} else {
					return GlSampler.LINEAR;
				}
			} else {
				if (mipped[i]) {
					return GlSampler.MIPPED_NEAREST;
				} else {
					return GlSampler.NEAREST;
				}
			}
		}
	}

	private void createDepthMipmaps() {
		var backend = VulkanCommandBridge.device(device);

		var layout = new SamplerHeapLayout.Builder().add(SamplerHeapLayout.Type.SAMPLER2D, "depth", "nearest").build();
		depthMipSlot = layout.slot(SamplerHeapLayout.Type.SAMPLER2D, "depth", "nearest");

		var shader = VulkanShaderPatcher.patch(Map.of(PatchShaderType.VERTEX,
			"#version 450\nvoid main(){vec2 p=vec2((gl_VertexIndex<<1)&2,gl_VertexIndex&2);gl_Position=vec4(p*2.0-1.0,0,1);}",
			PatchShaderType.FRAGMENT, """
				#version 450
				uniform sampler2D sourceDepth;
				void main() {
				    ivec2 size=textureSize(sourceDepth,0);
				    vec2 p=gl_FragCoord.xy*vec2(size)/vec2(max(size/2,ivec2(1)))-0.5;
				    ivec2 base=ivec2(floor(p)); vec2 weight=fract(p);
				    float a=texelFetch(sourceDepth,clamp(base,ivec2(0),size-1),0).r;
				    float b=texelFetch(sourceDepth,clamp(base+ivec2(1,0),ivec2(0),size-1),0).r;
				    float c=texelFetch(sourceDepth,clamp(base+ivec2(0,1),ivec2(0),size-1),0).r;
				    float d=texelFetch(sourceDepth,clamp(base+ivec2(1),ivec2(0),size-1),0).r;
				    gl_FragDepth=mix(mix(a,b,weight.x),mix(c,d,weight.x),weight.y);
				}
				"""), new SamplerBindingPlan.Builder(layout).bind("sourceDepth", depthMipSlot).build());
		depthMipHeap = new VulkanSamplerHeap(backend.vkDevice(), layout, 256);


		try (var compiler = new VulkanShaderCompiler()) {
			var vertex = compiler.compile("depth-mips.vsh", PatchShaderType.VERTEX, shader.sources().get(PatchShaderType.VERTEX));

			try {
				var fragment = compiler.compile("depth-mips.fsh", PatchShaderType.FRAGMENT,
					shader.sources().get(PatchShaderType.FRAGMENT));

				try {
					depthMipPipeline = new VulkanGraphicsPipeline(backend.vkDevice(), depthMipHeap, vertex, fragment,
						VK10.VK_FORMAT_D32_SFLOAT);
				} finally {
					MemoryUtil.memFree(fragment);
				}
			} finally {
				MemoryUtil.memFree(vertex);
			}
		} catch (RuntimeException | Error failure) {
			depthMipHeap.close();

			throw failure;
		}
	}

	public void generateDepthMipmaps(GpuTexture texture) {
		if (texture.getFormat() != GpuFormat.D32_FLOAT || texture.isClosed()) {
			throw new IllegalArgumentException("Expected live D32 shadow depth");
		}

		if (texture.getMipLevels() < 2) {
			return;
		}

		if (depthMipPipeline == null) {
			createDepthMipmaps();
		}

		var levels = depthMipViews.computeIfAbsent(texture, image -> {
			var result = new ArrayList<GpuTextureView>();

			try {
				for (int i = 0; i < image.getMipLevels(); i++) {
					result.add(device.createTextureView(image, i, 1));
				}

				return result;
			} catch (RuntimeException | Error failure) {
				result.forEach(GpuTextureView::close);

				throw failure;
			}
		});
		var encoder = VulkanCommandBridge.encoder(device.createCommandEncoder());
		var command = encoder.allocateAndBeginTransientCommandBuffer();

		try (var stack = MemoryStack.stackPush()) {
			VulkanCommandEncoder.memoryBarrier(command, stack);

			for (int level = 1; level < levels.size(); level++) {
				var source = (VulkanGpuTextureView) levels.get(level - 1);
				var target = (VulkanGpuTextureView) levels.get(level);
				var snapshot = depthMipHeap.snapshot(Map.of(depthMipSlot,
					new VulkanSamplerHeap.ImageSampler(source.vkImageView(), depthMipHeap.sampler(GlSampler.NEAREST),
						VK10.VK_IMAGE_LAYOUT_GENERAL)));
				encoder.queueForDestroy(snapshot::close);

				int width = texture.getWidth(level);
				int height = texture.getHeight(level);
				var depth = VkRenderingAttachmentInfoKHR.calloc(stack).sType$Default().imageView(target.vkImageView())
					.imageLayout(VK10.VK_IMAGE_LAYOUT_GENERAL).loadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
					.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
				var rendering = VkRenderingInfoKHR.calloc(stack).sType$Default().layerCount(1).pDepthAttachment(depth);
				rendering.renderArea().extent().set(width, height);
				KHRDynamicRendering.vkCmdBeginRenderingKHR(command, rendering);
				VK10.vkCmdSetViewport(command, 0, VkViewport.calloc(1, stack).width(width).height(height).minDepth(0).maxDepth(1));

				var scissor = VkRect2D.calloc(1, stack);
				scissor.extent().set(width, height);
				VK10.vkCmdSetScissor(command, 0, scissor);
				depthMipPipeline.bind(command, snapshot, null);
				VK10.vkCmdDraw(command, 3, 1, 0, 0);
				KHRDynamicRendering.vkCmdEndRenderingKHR(command);
				VulkanCommandEncoder.memoryBarrier(command, stack);
			}

			int result = VK10.vkEndCommandBuffer(command);

			if (result != VK10.VK_SUCCESS) {
				throw new IllegalStateException("Could not finish depth mipmaps: " + result);
			}
		}

		encoder.execute(command);
	}

	private void destroyDepthMipmaps() {
		depthMipViews.values().forEach(levels -> levels.forEach(GpuTextureView::close));
		depthMipViews.clear();

		var encoder = VulkanCommandBridge.device(device).createCommandEncoder();
		encoder.queueForDestroy(depthMipPipeline::close);
		encoder.queueForDestroy(depthMipHeap::close);
	}

}
