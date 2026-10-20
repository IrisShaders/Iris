package net.irisshaders.iris.vulkan;

import com.google.common.hash.HashCode;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2LongAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.PipelineState;
import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.gl.blending.BlendModeFunction;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.VulkanPushConstants;
import net.irisshaders.iris.pipeline.transform.WorldShaderTransformer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTGraphicsPipelineLibrary;
import org.lwjgl.vulkan.KHRPipelineLibrary;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineLibraryCreateInfoEXT;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLibraryCreateInfoKHR;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineTessellationDomainOriginStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineTessellationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputDivisorStateCreateInfoEXT;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDivisorDescriptionEXT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.irisshaders.iris.vulkan.VulkanChecks.check;

public class VulkanGraphicsPipeline implements AutoCloseable {
	private static final Set<Integer> UNSUPPORTED_BLEND_FORMATS = ConcurrentHashMap.newKeySet();
	private static final Set<Integer> INTEGER_COLOR_FORMATS = Arrays.stream(GpuFormat.values())
		.filter(format -> format.hasColorAspect() && PipelineState.isInteger(format)).map(VulkanConst::toVk)
		.collect(Collectors.toUnmodifiableSet());
	private final VkDevice device;
	private final long zeroVertexBuffer;
	private final int zeroVertexBinding;
	private ShaderLibrary shaderLibrary;
	private long pipelineLayout;
	private long pipeline;
	private long stateLibrary;

	public VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, ByteBuffer vertexSpirv, ByteBuffer fragmentSpirv,
		int depthFormat, long drawLayout) {
		this(device, heap, Map.of(PatchShaderType.VERTEX, vertexSpirv, PatchShaderType.FRAGMENT, fragmentSpirv), new int[0], 0,
			List.of(), List.of(), null, null, depthFormat, null, drawLayout, false);
	}

	public VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap,
		Map<PatchShaderType, ByteBuffer> stages, int[] outputFormats, int vertexStride, List<Attribute> attributes, List<BlendMode> blends,
		BitSet writtenAttachments, WorldState world, long drawLayout, boolean minecraftPass) {
		this(device, heap, stages, outputFormats, vertexStride, attributes, blends, writtenAttachments, world,
			VK10.VK_FORMAT_UNDEFINED, null, drawLayout, minecraftPass);
	}

	public VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, ShaderLibrary library,
		int[] outputFormats, int vertexStride, List<Attribute> attributes, List<BlendMode> blends, BitSet writtenAttachments,
		WorldState world, long drawLayout, boolean minecraftPass) {
		this(device, heap, Map.of(), outputFormats, vertexStride, attributes, blends, writtenAttachments, world,
			VK10.VK_FORMAT_UNDEFINED, library, drawLayout, minecraftPass);
	}

	private VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap,
		Map<PatchShaderType, ByteBuffer> stages, int[] outputFormats, int vertexStride, List<Attribute> attributes, List<BlendMode> blends,
		BitSet writtenAttachments, WorldState world, int depthFormat, ShaderLibrary library,
		long drawLayout, boolean minecraftPass) {
		boolean tessellation = library == null ? stages.containsKey(PatchShaderType.TESS_CONTROL) : library.tessellation;

		this.device = device;
		this.zeroVertexBuffer = world == null ? 0 : world.zeroVertexBuffer();
		this.zeroVertexBinding = world == null ? 0 : WorldShaderTransformer.fallbackVertexBinding(world.vertexFormats());

		var modules = new LongArrayList();

		try (var stack = MemoryStack.stackPush()) {
			var handle = stack.mallocLong(1);

			if (library != null) {
				this.shaderLibrary = library.retain();
				this.pipelineLayout = library.pipelineLayout;
			} else {
				check(VK10.vkCreatePipelineLayout(device, VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pPushConstantRanges(
					VkPushConstantRange.calloc(1, stack).stageFlags(VK10.VK_SHADER_STAGE_ALL_GRAPHICS).offset(0).size(VulkanPushConstants.MAX_SIZE)).pSetLayouts(
					heap.pipelineSetLayouts(stack, drawLayout)), null, handle), "create fullscreen pipeline layout");
				this.pipelineLayout = handle.get(0);
			}

			var shaderStages = stages.isEmpty() ? null : VkPipelineShaderStageCreateInfo.calloc(stages.size(), stack);
			int stageIndex = 0;

			for (var entry : stages.entrySet()) {
				long module = createModule(device, stack, entry.getValue());
				modules.add(module);
				shaderStages.get(stageIndex++).sType$Default().stage(VulkanShaderCompiler.stageFlag(entry.getKey())).module(module)
					.pName(stack.UTF8("main"));
			}

			var state = world == null ? null : world.pipeline();
			var raster = VkPipelineRasterizationStateCreateInfo.calloc(stack).sType$Default()
				.polygonMode(state == null ? VK10.VK_POLYGON_MODE_FILL : VulkanConst.toVk(state.getPolygonMode()))
				.cullMode(PipelineState.cull(state, world != null && world.shadow()) ? VK10.VK_CULL_MODE_BACK_BIT : VK10.VK_CULL_MODE_NONE)
				.frontFace(state == null ? VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE : VK10.VK_FRONT_FACE_CLOCKWISE).lineWidth(1.0f);
			var depthStencil = depthStencil(stack, world, depthFormat);
			int attachmentDepthFormat = depthFormat;

			if (world != null && world.depthFormat() != VK10.VK_FORMAT_UNDEFINED) {
				attachmentDepthFormat = world.depthFormat();
				var depth = PipelineState.depthState(state, true, world.shadow());

				if (depth != null) {
					raster.depthBiasEnable(depth.depthBiasConstant() != 0 || depth.depthBiasScaleFactor() != 0)
						.depthBiasConstantFactor(depth.depthBiasConstant()).depthBiasSlopeFactor(depth.depthBiasScaleFactor());
				}
			}

			int bindingCapacity = (world == null ? 1 : world.vertexFormats().size()) + 1;
			var bindings = VkVertexInputBindingDescription.calloc(bindingCapacity, stack);
			var divisors = VkVertexInputBindingDivisorDescriptionEXT.calloc(bindingCapacity, stack);
			int bindingCount = 0;
			int divisorCount = 0;

			if (state != null) {
				for (int binding = 0; binding < world.vertexFormats().size(); binding++) {
					var format = world.vertexFormats().get(binding);

					if (format == null) {
						continue;
					}

					bindings.get(bindingCount++).binding(binding).stride(format.getVertexSize())
						.inputRate(format.getStepRate() == 0 ? VK10.VK_VERTEX_INPUT_RATE_VERTEX : VK10.VK_VERTEX_INPUT_RATE_INSTANCE);

					if (format.getStepRate() != 0) {
						divisors.get(divisorCount++).binding(binding).divisor(Math.max(1, format.getStepRate()));
					}
				}
			} else if (!attributes.isEmpty()) {
				bindings.get(bindingCount++).binding(0).stride(vertexStride).inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
			}

			if (this.zeroVertexBuffer != 0) {
				bindings.get(bindingCount++).binding(this.zeroVertexBinding).stride(0).inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
			}

			var vertexInput = VkPipelineVertexInputStateCreateInfo.calloc(stack).sType$Default();

			if (bindingCount != 0) {
				vertexInput.pVertexBindingDescriptions(bindings.limit(bindingCount));
			}

			if (divisorCount != 0) {
				vertexInput.pNext(VkPipelineVertexInputDivisorStateCreateInfoEXT.calloc(stack).sType$Default()
					.pVertexBindingDivisors(divisors.limit(divisorCount)));
			}

			if (!attributes.isEmpty()) {
				var descriptions = VkVertexInputAttributeDescription.calloc(attributes.size(), stack);

				for (int i = 0; i < attributes.size(); i++) {
					var attribute = attributes.get(i);
					descriptions.get(i).location(attribute.location()).binding(attribute.binding()).format(attribute.format())
						.offset(attribute.offset());
				}

				vertexInput.pVertexAttributeDescriptions(descriptions);
			}

			var colorBlend = VkPipelineColorBlendStateCreateInfo.calloc(stack).sType$Default();

			if (!blends.isEmpty()) {
				var attachments = VkPipelineColorBlendAttachmentState.calloc(blends.size(), stack);

				for (int i = 0; i < blends.size(); i++) {
					boolean writes = outputFormats[i] != VK10.VK_FORMAT_UNDEFINED && (writtenAttachments == null || writtenAttachments.get(
						i));
					int writeMask = PipelineState.colorMask(state, writes);
					var mode = writeMask != 0 && !INTEGER_COLOR_FORMATS.contains(outputFormats[i]) ? blends.get(i) : null;

					if (mode != null) {
						var properties = VkFormatProperties.calloc(stack);
						VK10.vkGetPhysicalDeviceFormatProperties(device.getPhysicalDevice(), outputFormats[i], properties);

						if ((properties.optimalTilingFeatures() & VK10.VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BLEND_BIT) == 0) {
							mode = null;

							if (UNSUPPORTED_BLEND_FORMATS.add(outputFormats[i])) {
								Iris.logger.warn(
									"Blending is not supported for Vulkan format {}. Disabling blending for attachments using this format.",
									outputFormats[i]);
							}
						}
					}

					attachments.get(i).blendEnable(mode != null)
						.srcColorBlendFactor(mode == null ? VK10.VK_BLEND_FACTOR_ONE : BlendModeFunction.toVk(mode.srcRgb()))
						.dstColorBlendFactor(mode == null ? VK10.VK_BLEND_FACTOR_ZERO : BlendModeFunction.toVk(mode.dstRgb()))
						.colorBlendOp(VK10.VK_BLEND_OP_ADD)
						.srcAlphaBlendFactor(mode == null ? VK10.VK_BLEND_FACTOR_ONE : BlendModeFunction.toVk(mode.srcAlpha()))
						.dstAlphaBlendFactor(mode == null ? VK10.VK_BLEND_FACTOR_ZERO : BlendModeFunction.toVk(mode.dstAlpha()))
						.alphaBlendOp(VK10.VK_BLEND_OP_ADD).colorWriteMask(writeMask);
				}

				colorBlend.pAttachments(attachments);
			}

			long renderPass = minecraftPass ? 0 : ((VulkanRenderBackend) Iris.getRenderBackend()).getRenderPassCache()
				.get(outputFormats, attachmentDepthFormat);

			var info = VkGraphicsPipelineCreateInfo.calloc(1, stack).sType$Default().renderPass(renderPass).pStages(shaderStages)
				.pVertexInputState(vertexInput).pInputAssemblyState(VkPipelineInputAssemblyStateCreateInfo.calloc(stack).sType$Default()
					.topology(
						tessellation ? VK10.VK_PRIMITIVE_TOPOLOGY_PATCH_LIST : state == null ? VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST : VulkanConst.toVk(
							state.getPrimitiveTopology())))
				.pViewportState(VkPipelineViewportStateCreateInfo.calloc(stack).sType$Default().viewportCount(1).scissorCount(1))
				.pRasterizationState(raster).pMultisampleState(
					VkPipelineMultisampleStateCreateInfo.calloc(stack).sType$Default().rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT))
				.pDepthStencilState(depthStencil).pColorBlendState(colorBlend).pDynamicState(
					VkPipelineDynamicStateCreateInfo.calloc(stack).sType$Default()
						.pDynamicStates(stack.ints(VK10.VK_DYNAMIC_STATE_VIEWPORT, VK10.VK_DYNAMIC_STATE_SCISSOR)))
				.layout(this.pipelineLayout);

			if (minecraftPass) {
				var rendering = VkPipelineRenderingCreateInfo.calloc(stack).sType$Default().depthAttachmentFormat(attachmentDepthFormat);
				if (outputFormats.length != 0) {
					rendering.pColorAttachmentFormats(stack.ints(outputFormats));
				}
				info.pNext(rendering);
			}

			if (tessellation) {
				info.pTessellationState(VkPipelineTessellationStateCreateInfo.calloc(stack).sType$Default().patchControlPoints(3).pNext(
					VkPipelineTessellationDomainOriginStateCreateInfo.calloc(stack).sType$Default()
						.domainOrigin(VK11.VK_TESSELLATION_DOMAIN_ORIGIN_LOWER_LEFT)));
			}

			if (library != null) {
				info.flags(
						KHRPipelineLibrary.VK_PIPELINE_CREATE_LIBRARY_BIT_KHR | EXTGraphicsPipelineLibrary.VK_PIPELINE_CREATE_RETAIN_LINK_TIME_OPTIMIZATION_INFO_BIT_EXT)
					.pNext(VkGraphicsPipelineLibraryCreateInfoEXT.calloc(stack).sType$Default().flags(
						EXTGraphicsPipelineLibrary.VK_GRAPHICS_PIPELINE_LIBRARY_VERTEX_INPUT_INTERFACE_BIT_EXT | EXTGraphicsPipelineLibrary.VK_GRAPHICS_PIPELINE_LIBRARY_FRAGMENT_OUTPUT_INTERFACE_BIT_EXT))
					.pStages(null).pViewportState(null).pRasterizationState(null).pDepthStencilState(null).pTessellationState(null)
					.pDynamicState(null);
			}

			handle.put(0, 0);
			int result = VK10.vkCreateGraphicsPipelines(device, VulkanCommandBridge.pipelineCache(), info, null, handle);
			this.pipeline = handle.get(0);
			check(result, "create graphics pipeline" + (library != null ? " library" : ""));

			if (library != null) {
				this.stateLibrary = this.pipeline;
				this.pipeline = 0;
				var link = VkGraphicsPipelineCreateInfo.calloc(1, stack).sType$Default().renderPass(renderPass).layout(this.pipelineLayout)
					.flags(EXTGraphicsPipelineLibrary.VK_PIPELINE_CREATE_LINK_TIME_OPTIMIZATION_BIT_EXT).pNext(
						VkPipelineLibraryCreateInfoKHR.calloc(stack).sType$Default().pLibraries(
							stack.longs(library.preRaster(raster, renderPass), library.fragment(depthStencil, renderPass),
								this.stateLibrary)));
				handle.put(0, 0);
				result = VK10.vkCreateGraphicsPipelines(device, VulkanCommandBridge.pipelineCache(), link, null, handle);
				this.pipeline = handle.get(0);
				check(result, "link graphics pipeline libraries");
			}
		} catch (RuntimeException | Error exception) {
			this.close();

			throw exception;
		} finally {
			for (long module : modules) {
				VK10.vkDestroyShaderModule(device, module, null);
			}
		}
	}

	private static VkPipelineDepthStencilStateCreateInfo depthStencil(MemoryStack stack, WorldState world, int depthFormat) {
		var state = VkPipelineDepthStencilStateCreateInfo.calloc(stack).sType$Default()
			.depthTestEnable(depthFormat != VK10.VK_FORMAT_UNDEFINED).depthWriteEnable(depthFormat != VK10.VK_FORMAT_UNDEFINED)
			.depthCompareOp(depthFormat == VK10.VK_FORMAT_UNDEFINED ? VK10.VK_COMPARE_OP_GREATER_OR_EQUAL : VK10.VK_COMPARE_OP_ALWAYS);
		if (world != null && world.depthFormat() != VK10.VK_FORMAT_UNDEFINED) {
			var depth = PipelineState.depthState(world.pipeline(), true, world.shadow());
			if (depth != null) {
				state.depthTestEnable(true).depthWriteEnable(depth.writeDepth()).depthCompareOp(VulkanConst.toVk(depth.depthTest()));
			}
		}
		return state;
	}

	private static long createModule(VkDevice device, MemoryStack stack, ByteBuffer spirv) {
		var handle = stack.mallocLong(1);
		check(VK10.vkCreateShaderModule(device, VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(spirv), null, handle),
			"create fullscreen shader module");

		return handle.get(0);
	}

	public void pushConstants(VkCommandBuffer command, ByteBuffer data) {
		VK10.vkCmdPushConstants(command, this.pipelineLayout, VK10.VK_SHADER_STAGE_ALL_GRAPHICS, VulkanPushConstants.NATIVE_OFFSET, data);
	}

	public void pushUniforms(VkCommandBuffer command, ByteBuffer data) {
		if (data.hasRemaining()) {
			VK10.vkCmdPushConstants(command, this.pipelineLayout, VK10.VK_SHADER_STAGE_ALL_GRAPHICS, 0, data);
		}
	}

	public long layout() {
		return this.pipelineLayout;
	}

	public void bind(VkCommandBuffer commandBuffer, VulkanBindingState state) {
		if (state == null || state.pipeline(this.pipeline)) {
			VK10.vkCmdBindPipeline(commandBuffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, this.pipeline);
		}

		if (this.zeroVertexBuffer != 0) {
			try (var stack = MemoryStack.stackPush()) {
				VK10.vkCmdBindVertexBuffers(commandBuffer, this.zeroVertexBinding, stack.longs(this.zeroVertexBuffer), stack.longs(0));
			}
		}
	}

	@Override
	public void close() {
		if (this.pipeline != 0) {
			VK10.vkDestroyPipeline(this.device, this.pipeline, null);
		}

		if (this.stateLibrary != 0) {
			VK10.vkDestroyPipeline(this.device, this.stateLibrary, null);
		}

		if (this.shaderLibrary == null && this.pipelineLayout != 0) {
			VK10.vkDestroyPipelineLayout(this.device, this.pipelineLayout, null);
		}

		this.pipeline = this.stateLibrary = this.pipelineLayout = 0;

		if (this.shaderLibrary != null) {
			this.shaderLibrary.close();
			this.shaderLibrary = null;
		}
	}

	public static class ShaderLibrary implements AutoCloseable {
		private final VulkanShaderCache cache;
		private final Map<PatchShaderType, HashCode> vertexSources;
		private final Map<PatchShaderType, HashCode> fragmentSources;
		private final VkDevice device;
		private final List<VulkanShaderBindings.LayoutBinding> drawLayout;
		private final boolean tessellation;
		private final Object2LongMap<PatchShaderType> modules = new Object2LongAVLTreeMap<>();
		private final Map<RasterState, PipelineLibrary> preRaster = new ConcurrentHashMap<>();
		private final Map<DepthState, PipelineLibrary> fragments = new ConcurrentHashMap<>();
		private final AtomicInteger references = new AtomicInteger(1);
		private final long pipelineLayout;

		public ShaderLibrary(VulkanShaderCache cache, Map<PatchShaderType, HashCode> sources, VkDevice device, VulkanSamplerHeap heap,
			Map<PatchShaderType, ByteBuffer> stages, long drawLayout) {
			this.cache = cache;
			var vertex = new EnumMap<PatchShaderType, HashCode>(PatchShaderType.class);
			vertex.putAll(sources);
			vertex.remove(PatchShaderType.FRAGMENT);
			this.vertexSources = Map.copyOf(vertex);
			this.fragmentSources = sources.containsKey(PatchShaderType.FRAGMENT) ? Map.of(PatchShaderType.FRAGMENT,
				sources.get(PatchShaderType.FRAGMENT)) : Map.of();
			this.device = device;
			this.drawLayout = heap.drawLayoutKey(drawLayout);
			this.tessellation = stages.containsKey(PatchShaderType.TESS_CONTROL);

			try (var stack = MemoryStack.stackPush()) {
				var handle = stack.mallocLong(1);

				check(VK10.vkCreatePipelineLayout(device, VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pPushConstantRanges(
					VkPushConstantRange.calloc(1, stack).stageFlags(VK10.VK_SHADER_STAGE_ALL_GRAPHICS).offset(0).size(VulkanPushConstants.MAX_SIZE)).pSetLayouts(
					heap.pipelineSetLayouts(stack, drawLayout)), null, handle), "create shader library layout");
				this.pipelineLayout = handle.get(0);
				stages.forEach((stage, spirv) -> this.modules.put(stage, createModule(device, stack, spirv)));
			} catch (RuntimeException | Error e) {
				this.close();
				throw e;
			}
		}

		private long preRaster(VkPipelineRasterizationStateCreateInfo state, long renderPass) {
			var key = new RasterState(state.polygonMode(), state.cullMode(), state.frontFace(), state.depthBiasConstantFactor(),
				state.depthBiasSlopeFactor(), renderPass);
			return this.preRaster.computeIfAbsent(key, unused -> {
				try (var stack = MemoryStack.stackPush()) {
					var info = this.libraryInfo(stack, false, renderPass).pRasterizationState(state)
						.pViewportState(VkPipelineViewportStateCreateInfo.calloc(stack).sType$Default().viewportCount(1).scissorCount(1))
						.pDynamicState(VkPipelineDynamicStateCreateInfo.calloc(stack).sType$Default()
							.pDynamicStates(stack.ints(VK10.VK_DYNAMIC_STATE_VIEWPORT, VK10.VK_DYNAMIC_STATE_SCISSOR)));

					if (this.tessellation) {
						info.pTessellationState(VkPipelineTessellationStateCreateInfo.calloc(stack).sType$Default().patchControlPoints(3)
							.pNext(VkPipelineTessellationDomainOriginStateCreateInfo.calloc(stack).sType$Default()
								.domainOrigin(VK11.VK_TESSELLATION_DOMAIN_ORIGIN_LOWER_LEFT)));
					}

					return this.compile(stack, info, this.vertexSources, key);
				}
			}).pipeline;
		}

		public void prepareFragment(int[] outputFormats, WorldState world, boolean minecraftPass) {
			int depthFormat = world == null ? VK10.VK_FORMAT_UNDEFINED : world.depthFormat();
			long renderPass = minecraftPass ? 0 : ((VulkanRenderBackend) Iris.getRenderBackend()).getRenderPassCache()
				.get(outputFormats, depthFormat);
			try (var stack = MemoryStack.stackPush()) {
				var state = depthStencil(stack, world, VK10.VK_FORMAT_UNDEFINED);
				var key = new DepthState(state.depthTestEnable(), state.depthWriteEnable(), state.depthCompareOp(), renderPass);
				this.fragments.computeIfAbsent(key, unused -> this.compile(stack,
					this.libraryInfo(stack, true, renderPass).pDepthStencilState(state).pMultisampleState(
						VkPipelineMultisampleStateCreateInfo.calloc(stack).sType$Default()
							.rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT)), this.fragmentSources, key));
			}
		}

		private long fragment(VkPipelineDepthStencilStateCreateInfo state, long renderPass) {
			var key = new DepthState(state.depthTestEnable(), state.depthWriteEnable(), state.depthCompareOp(), renderPass);
			var library = this.fragments.get(key);
			if (library == null) {
				throw new IllegalStateException("fragment library was not prepared: " + key);
			}
			return library.pipeline;
		}

		private VkGraphicsPipelineCreateInfo.Buffer libraryInfo(MemoryStack stack, boolean fragment, long renderPass) {
			var stages = VkPipelineShaderStageCreateInfo.calloc(fragment ? 1 : this.modules.size() - 1, stack);
			int index = 0;

			for (var entry : this.modules.object2LongEntrySet()) {
				if ((entry.getKey() == PatchShaderType.FRAGMENT) == fragment) {
					stages.get(index++).sType$Default().stage(VulkanShaderCompiler.stageFlag(entry.getKey())).module(entry.getLongValue())
						.pName(stack.UTF8("main"));
				}
			}

			return VkGraphicsPipelineCreateInfo.calloc(1, stack).sType$Default().renderPass(renderPass).layout(this.pipelineLayout)
				.pStages(stages).flags(
					KHRPipelineLibrary.VK_PIPELINE_CREATE_LIBRARY_BIT_KHR | EXTGraphicsPipelineLibrary.VK_PIPELINE_CREATE_RETAIN_LINK_TIME_OPTIMIZATION_INFO_BIT_EXT)
				.pNext(VkGraphicsPipelineLibraryCreateInfoEXT.calloc(stack).sType$Default().flags(
					fragment ? EXTGraphicsPipelineLibrary.VK_GRAPHICS_PIPELINE_LIBRARY_FRAGMENT_SHADER_BIT_EXT : EXTGraphicsPipelineLibrary.VK_GRAPHICS_PIPELINE_LIBRARY_PRE_RASTERIZATION_SHADERS_BIT_EXT));
		}

		private PipelineLibrary compile(MemoryStack stack, VkGraphicsPipelineCreateInfo.Buffer info, Map<PatchShaderType, HashCode> sources,
			Object state) {
			return this.cache.acquirePipelineLibrary(sources, this.drawLayout, state, () -> this.compile(stack, info));
		}

		private long compile(MemoryStack stack, VkGraphicsPipelineCreateInfo.Buffer info) {
			var handle = stack.callocLong(1);
			int result = VK10.vkCreateGraphicsPipelines(this.device, VulkanCommandBridge.pipelineCache(), info, null, handle);

			if (result != VK10.VK_SUCCESS) {
				if (handle.get(0) != 0) {
					VK10.vkDestroyPipeline(this.device, handle.get(0), null);
				}
				check(result, "create shader library");
			}

			return handle.get(0);
		}

		public ShaderLibrary retain() {
			this.references.incrementAndGet();

			return this;
		}

		public boolean isUnused() {
			return this.references.get() == 1;
		}

		@Override
		public void close() {
			if (this.references.getAndDecrement() == 1) {
				this.preRaster.values().forEach(PipelineLibrary::close);
				this.fragments.values().forEach(PipelineLibrary::close);
				this.modules.values().forEach(module -> VK10.vkDestroyShaderModule(this.device, module, null));
				if (this.pipelineLayout != 0) {
					VK10.vkDestroyPipelineLayout(this.device, this.pipelineLayout, null);
				}
			}
		}

		public record RasterState(int polygonMode, int cullMode, int frontFace, float biasConstant, float biasSlope, long renderPass) {
		}

		public record DepthState(boolean test, boolean write, int compare, long renderPass) {
		}
	}

	public static class PipelineLibrary implements AutoCloseable {
		private final VkDevice device;
		private final long pipeline;
		private final AtomicInteger references = new AtomicInteger(1);

		public PipelineLibrary(VkDevice device, long pipeline) {
			this.device = device;
			this.pipeline = pipeline;
		}

		public PipelineLibrary retain() {
			this.references.incrementAndGet();

			return this;
		}

		public boolean isUnused() {
			return this.references.get() == 1;
		}

		@Override
		public void close() {
			if (this.references.getAndDecrement() == 1) {
				VK10.vkDestroyPipeline(this.device, this.pipeline, null);
			}
		}
	}

	public record WorldState(RenderPipeline pipeline, int depthFormat, boolean shadow, long zeroVertexBuffer, List<VertexFormat> vertexFormats) {
		public WorldState {
			vertexFormats = Collections.unmodifiableList(new ArrayList<>(vertexFormats));
		}
	}

	public record Attribute(int location, int binding, int format, int offset) {
	}
}
