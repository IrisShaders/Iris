package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineTessellationDomainOriginStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineTessellationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputDivisorStateCreateInfoEXT;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderingAttachmentInfoKHR;
import org.lwjgl.vulkan.VkRenderingInfoKHR;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDivisorDescriptionEXT;
import org.lwjgl.vulkan.VkViewport;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class VulkanGraphicsPipeline implements AutoCloseable {
	private static final Set<Integer> INTEGER_COLOR_FORMATS = Arrays.stream(GpuFormat.values())
		.filter(format -> format.hasColorAspect() && (format == GpuFormat.RGB10A2_UINT || switch (format.componentType()) {
			case UINT_8, SINT_8, UINT_16, SINT_16, UINT_32, SINT_32 -> true;
			default -> false;
		})).map(VulkanConst::toVk).collect(Collectors.toUnmodifiableSet());
	private final VkDevice device;
	private final long zeroVertexBuffer;
	private final int zeroVertexBinding;
	private final SamplerHeapLayout heapLayout;
	private final VulkanResourceSets uniformSets;
	private final boolean hasVertices;
	private final int colorAttachmentCount;
	private final int[] colorFormats;
	private long emptySetLayout;
	private long pipelineLayout;
	private long pipeline;

	public VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, VulkanResourceSets uniformSets, ByteBuffer vertexSpirv,
		ByteBuffer fragmentSpirv, int outputFormat, int vertexStride, List<Attribute> attributes) {
		this(device, heap, uniformSets, vertexSpirv, fragmentSpirv, new int[]{outputFormat}, vertexStride, attributes,
			Collections.singletonList(null));
	}

	public VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, VulkanResourceSets uniformSets, ByteBuffer vertexSpirv,
		ByteBuffer fragmentSpirv, int[] outputFormats, int vertexStride, List<Attribute> attributes, List<BlendMode> blends) {
		this(device, heap, uniformSets, vertexSpirv, fragmentSpirv, outputFormats, vertexStride, attributes, blends, null);
	}

	public VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, VulkanResourceSets uniformSets, ByteBuffer vertexSpirv,
		ByteBuffer fragmentSpirv, int[] outputFormats, int vertexStride, List<Attribute> attributes, List<BlendMode> blends,
		BitSet writtenAttachments) {
		this(device, heap, uniformSets, vertexSpirv, fragmentSpirv, outputFormats, vertexStride, attributes, blends, writtenAttachments,
			null);
	}

	public VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, VulkanResourceSets uniformSets, ByteBuffer vertexSpirv,
		ByteBuffer fragmentSpirv, int[] outputFormats, int vertexStride, List<Attribute> attributes, List<BlendMode> blends,
		BitSet writtenAttachments, WorldState world) {
		this(device, heap, uniformSets, vertexSpirv, fragmentSpirv, outputFormats, vertexStride, attributes, blends, writtenAttachments,
			world, VK10.VK_FORMAT_UNDEFINED);
	}

	public VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, ByteBuffer vertexSpirv, ByteBuffer fragmentSpirv,
		int depthFormat) {
		this(device, heap, null, vertexSpirv, fragmentSpirv, new int[0], 0, List.of(), List.of(), null, null, depthFormat);
	}

	private VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, VulkanResourceSets uniformSets, ByteBuffer vertexSpirv,
		ByteBuffer fragmentSpirv, int[] outputFormats, int vertexStride, List<Attribute> attributes, List<BlendMode> blends,
		BitSet writtenAttachments, WorldState world, int depthFormat) {
		this(device, heap, uniformSets, Map.of(PatchShaderType.VERTEX, vertexSpirv, PatchShaderType.FRAGMENT, fragmentSpirv),
			outputFormats,
			vertexStride, attributes, blends, writtenAttachments, world, depthFormat, null);
	}

	public VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, VulkanResourceSets uniformSets,
		Map<PatchShaderType, ByteBuffer> stages, int[] outputFormats, int vertexStride, List<Attribute> attributes, List<BlendMode> blends,
		BitSet writtenAttachments, WorldState world) {
		this(device, heap, uniformSets, stages, outputFormats, vertexStride, attributes, blends, writtenAttachments, world,
			VK10.VK_FORMAT_UNDEFINED, null);
	}

	public VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, VulkanResourceSets uniformSets,
		Map<PatchShaderType, ByteBuffer> stages, int[] outputFormats, int vertexStride, List<Attribute> attributes, List<BlendMode> blends,
		BitSet writtenAttachments, WorldState world, VulkanResourceSets frameSets) {
		this(device, heap, uniformSets, stages, outputFormats, vertexStride, attributes, blends, writtenAttachments, world,
			VK10.VK_FORMAT_UNDEFINED, frameSets);
	}

	private VulkanGraphicsPipeline(VkDevice device, VulkanSamplerHeap heap, VulkanResourceSets uniformSets,
		Map<PatchShaderType, ByteBuffer> stages, int[] outputFormats, int vertexStride, List<Attribute> attributes, List<BlendMode> blends,
		BitSet writtenAttachments, WorldState world, int depthFormat, VulkanResourceSets frameSets) {
		if (!stages.containsKey(PatchShaderType.VERTEX) || !stages.containsKey(PatchShaderType.FRAGMENT) ||
			stages.containsKey(PatchShaderType.COMPUTE)) {
			throw new IllegalArgumentException("Invalid graphics shader stages");
		}

		boolean tessellation = stages.containsKey(PatchShaderType.TESS_CONTROL);

		if (tessellation != stages.containsKey(PatchShaderType.TESS_EVAL)) {
			throw new IllegalArgumentException("Tessellation requires both control and evaluation stages");
		}

		if (writtenAttachments != null && writtenAttachments.length() > outputFormats.length) {
			throw new IllegalArgumentException("Output write mask exceeds attachment count");
		}

		if ((outputFormats.length == 0 && depthFormat == VK10.VK_FORMAT_UNDEFINED) || outputFormats.length != blends.size()) {
			throw new IllegalArgumentException("Color formats and blend states must match");
		}

		this.colorAttachmentCount = outputFormats.length;
		this.colorFormats = outputFormats.clone();
		this.device = device;
		this.zeroVertexBuffer = world == null ? 0 : world.zeroVertexBuffer();
		this.zeroVertexBinding = world == null ? 0 : fallbackVertexBinding(world.pipeline());
		this.heapLayout = heap.layout();
		this.uniformSets = uniformSets;
		this.hasVertices = !attributes.isEmpty();

		var modules = new ArrayList<Long>();

		try (var stack = MemoryStack.stackPush()) {
			var handle = stack.mallocLong(1);

			if (uniformSets == null) {
				check(VK10.vkCreateDescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default(), null,
						handle),
					"create empty resource set layout");
				emptySetLayout = handle.get(0);
			}

			check(VK10.vkCreatePipelineLayout(device, VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(
					frameSets == null ? stack.longs(uniformSets == null ? emptySetLayout : uniformSets.setLayout(), heap.setLayout()) :
						stack.longs(uniformSets == null ? emptySetLayout : uniformSets.setLayout(), heap.setLayout(),
							frameSets.setLayout())),
				null, handle), "create fullscreen pipeline layout");
			pipelineLayout = handle.get(0);

			var shaderStages = VkPipelineShaderStageCreateInfo.calloc(stages.size(), stack);
			int stageIndex = 0;

			for (var entry : stages.entrySet()) {
				long module = createModule(stack, entry.getValue());
				modules.add(module);
				shaderStages.get(stageIndex++).sType$Default().stage(stageFlag(entry.getKey())).module(module).pName(stack.UTF8("main"));
			}

			var state = world == null ? null : world.pipeline();
			var raster = VkPipelineRasterizationStateCreateInfo.calloc(stack).sType$Default()
				.polygonMode(state == null ? VK10.VK_POLYGON_MODE_FILL : VulkanConst.toVk(state.getPolygonMode()))
				.cullMode(state != null && state.isCull() && !world.shadow() ? VK10.VK_CULL_MODE_BACK_BIT : VK10.VK_CULL_MODE_NONE)
				.frontFace(state == null ? VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE : VK10.VK_FRONT_FACE_CLOCKWISE).lineWidth(1.0f);
			var depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc(stack).sType$Default()
				.depthTestEnable(depthFormat != VK10.VK_FORMAT_UNDEFINED).depthWriteEnable(depthFormat != VK10.VK_FORMAT_UNDEFINED)
				.depthCompareOp(depthFormat == VK10.VK_FORMAT_UNDEFINED ? VK10.VK_COMPARE_OP_GREATER_OR_EQUAL : VK10.VK_COMPARE_OP_ALWAYS);
			int attachmentDepthFormat = depthFormat;

			if (world != null && world.depthFormat() != VK10.VK_FORMAT_UNDEFINED) {
				attachmentDepthFormat = world.depthFormat();
				var depth = state.getDepthStencilState();

				if (depth != null) {
					depthStencil.depthTestEnable(true).depthWriteEnable(depth.writeDepth())
						.depthCompareOp(depthCompare(VulkanConst.toVk(depth.depthTest()), world.shadow()));
					raster.depthBiasEnable(depth.depthBiasConstant() != 0 || depth.depthBiasScaleFactor() != 0)
						.depthBiasConstantFactor(world.shadow() ? -depth.depthBiasConstant() : depth.depthBiasConstant())
						.depthBiasSlopeFactor(world.shadow() ? -depth.depthBiasScaleFactor() : depth.depthBiasScaleFactor());
				}
			}

			int bindingCapacity = (state == null ? 1 : state.getVertexFormatBindings().size()) + 1;
			var bindings = VkVertexInputBindingDescription.calloc(bindingCapacity, stack);
			var divisors = VkVertexInputBindingDivisorDescriptionEXT.calloc(bindingCapacity, stack);
			int bindingCount = 0;
			int divisorCount = 0;

			if (state != null) {
				for (int binding = 0; binding < state.getVertexFormatBindings().size(); binding++) {
					var format = state.getVertexFormatBinding(binding);

					if (format == null) {
						continue;
					}

					bindings.get(bindingCount++).binding(binding).stride(format.getVertexSize())
						.inputRate(format.getStepRate() == 0 ? VK10.VK_VERTEX_INPUT_RATE_VERTEX : VK10.VK_VERTEX_INPUT_RATE_INSTANCE);

					if (format.getStepRate() != 0) {
						divisors.get(divisorCount++).binding(binding).divisor(Math.max(1, format.getStepRate()));
					}
				}
			} else if (hasVertices) {
				if (vertexStride <= 0) {
					throw new IllegalArgumentException("Invalid fullscreen vertex stride");
				}

				bindings.get(bindingCount++).binding(0).stride(vertexStride).inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
			}

			if (zeroVertexBuffer != 0) {
				bindings.get(bindingCount++).binding(zeroVertexBinding).stride(0).inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
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

			if (blends.size() > 8) {
				throw new IllegalArgumentException("Too many color blend attachments");
			}

			var colorBlend = VkPipelineColorBlendStateCreateInfo.calloc(stack).sType$Default();

			if (!blends.isEmpty()) {
				var attachments = VkPipelineColorBlendAttachmentState.calloc(blends.size(), stack);

				for (int i = 0; i < blends.size(); i++) {
					boolean writes =
						outputFormats[i] != VK10.VK_FORMAT_UNDEFINED && (writtenAttachments == null || writtenAttachments.get(i));
					var mode = writes && !INTEGER_COLOR_FORMATS.contains(outputFormats[i]) ? blends.get(i) : null;
					int writeMask = writes ?
						VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT |
						VK10.VK_COLOR_COMPONENT_A_BIT : 0;

					if (writes && state != null && !state.getColorTargetStates().isEmpty() &&
						state.getColorTargetStates().getFirst() != null) {
						writeMask = VulkanConst.toVk(state.getColorTargetStates().getFirst());
					}

					if (mode != null) {
						var properties = VkFormatProperties.calloc(stack);
						VK10.vkGetPhysicalDeviceFormatProperties(device.getPhysicalDevice(), outputFormats[i], properties);

						if ((properties.optimalTilingFeatures() & VK10.VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BLEND_BIT) == 0) {
							throw new UnsupportedOperationException("Format does not support blending: " + outputFormats[i]);
						}
					}

					attachments.get(i).blendEnable(mode != null)
						.srcColorBlendFactor(mode == null ? VK10.VK_BLEND_FACTOR_ONE : mode.srcRgb())
						.dstColorBlendFactor(mode == null ? VK10.VK_BLEND_FACTOR_ZERO : mode.dstRgb()).colorBlendOp(VK10.VK_BLEND_OP_ADD)
						.srcAlphaBlendFactor(mode == null ? VK10.VK_BLEND_FACTOR_ONE : mode.srcAlpha())
						.dstAlphaBlendFactor(mode == null ? VK10.VK_BLEND_FACTOR_ZERO : mode.dstAlpha()).alphaBlendOp(VK10.VK_BLEND_OP_ADD)
						.colorWriteMask(writeMask);
				}

				colorBlend.pAttachments(attachments);
			}

			var rendering = VkPipelineRenderingCreateInfo.calloc(stack).sType$Default().depthAttachmentFormat(attachmentDepthFormat);

			if (outputFormats.length != 0) {
				rendering.pColorAttachmentFormats(stack.ints(outputFormats));
			}

			var info = VkGraphicsPipelineCreateInfo.calloc(1, stack).sType$Default().pNext(rendering.address()).pStages(shaderStages)
				.pVertexInputState(vertexInput).pInputAssemblyState(VkPipelineInputAssemblyStateCreateInfo.calloc(stack).sType$Default()
					.topology(tessellation ? VK10.VK_PRIMITIVE_TOPOLOGY_PATCH_LIST :
						state == null ? VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST : VulkanConst.toVk(state.getPrimitiveTopology())))
				.pViewportState(VkPipelineViewportStateCreateInfo.calloc(stack).sType$Default().viewportCount(1).scissorCount(1))
				.pRasterizationState(raster).pMultisampleState(
					VkPipelineMultisampleStateCreateInfo.calloc(stack).sType$Default().rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT))
				.pDepthStencilState(depthStencil).pColorBlendState(colorBlend).pDynamicState(
					VkPipelineDynamicStateCreateInfo.calloc(stack).sType$Default()
						.pDynamicStates(stack.ints(VK10.VK_DYNAMIC_STATE_VIEWPORT, VK10.VK_DYNAMIC_STATE_SCISSOR))).layout(pipelineLayout);

			if (tessellation) {
				info.pTessellationState(VkPipelineTessellationStateCreateInfo.calloc(stack).sType$Default().patchControlPoints(3).pNext(
					VkPipelineTessellationDomainOriginStateCreateInfo.calloc(stack).sType$Default()
						.domainOrigin(VK11.VK_TESSELLATION_DOMAIN_ORIGIN_LOWER_LEFT)));
			}

			handle.put(0, 0);
			int result = VK10.vkCreateGraphicsPipelines(device, VK10.VK_NULL_HANDLE, info, null, handle);
			pipeline = handle.get(0);
			check(result, "create graphics pipeline");
		} catch (RuntimeException | Error exception) {
			close();

			throw exception;
		} finally {
			for (long module : modules) {
				VK10.vkDestroyShaderModule(device, module, null);
			}
		}
	}

	private static int stageFlag(PatchShaderType stage) {
		return switch (stage) {
			case VERTEX -> VK10.VK_SHADER_STAGE_VERTEX_BIT;
			case TESS_CONTROL -> VK10.VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT;
			case TESS_EVAL -> VK10.VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT;
			case GEOMETRY -> VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
			case FRAGMENT -> VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
			default -> throw new IllegalArgumentException("Not a graphics stage: " + stage);
		};
	}

	public static int depthCompare(int compare, boolean shadow) {
		if (!shadow) {
			return compare;
		}

		return switch (compare) {
			case VK10.VK_COMPARE_OP_GREATER -> VK10.VK_COMPARE_OP_LESS;
			case VK10.VK_COMPARE_OP_GREATER_OR_EQUAL -> VK10.VK_COMPARE_OP_LESS_OR_EQUAL;
			case VK10.VK_COMPARE_OP_LESS -> VK10.VK_COMPARE_OP_GREATER;
			case VK10.VK_COMPARE_OP_LESS_OR_EQUAL -> VK10.VK_COMPARE_OP_GREATER_OR_EQUAL;
			default -> compare;
		};
	}

	private static void check(int result, String operation) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException("Failed to " + operation + ": VkResult " + result);
		}
	}

	private long createModule(MemoryStack stack, ByteBuffer spirv) {
		if (!spirv.isDirect() || spirv.remaining() == 0 || spirv.remaining() % 4 != 0) {
			throw new IllegalArgumentException("SPIR-V must be a nonempty direct buffer of words");
		}

		var handle = stack.mallocLong(1);
		check(VK10.vkCreateShaderModule(device, VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(spirv), null, handle),
			"create fullscreen shader module");

		return handle.get(0);
	}

	public void record(VkCommandBuffer commandBuffer, VulkanSamplerHeap.Snapshot heap, VulkanResourceSets.Snapshot uniforms,
		long outputView, int width, int height, long vertexBuffer) {
		record(commandBuffer, heap, uniforms, outputView, width, height, vertexBuffer, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
	}

	public void record(VkCommandBuffer commandBuffer, VulkanSamplerHeap.Snapshot heap, VulkanResourceSets.Snapshot uniforms,
		long outputView, int width, int height, long vertexBuffer, int outputLayout) {
		record(commandBuffer, heap, uniforms, new long[]{outputView}, width, height, vertexBuffer, outputLayout);
	}

	public void record(VkCommandBuffer commandBuffer, VulkanSamplerHeap.Snapshot heap, VulkanResourceSets.Snapshot uniforms,
		long[] outputViews, int width, int height, long vertexBuffer, int outputLayout) {
		if (outputViews.length != colorAttachmentCount) {
			throw new IllegalArgumentException("Wrong color attachment count");
		}

		if (outputLayout != VK10.VK_IMAGE_LAYOUT_GENERAL && outputLayout != VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {
			throw new IllegalArgumentException("Invalid color attachment layout");
		}

		validateDraw(commandBuffer, heap, uniforms, vertexBuffer);

		try (var stack = MemoryStack.stackPush()) {
			beginRendering(commandBuffer, stack, outputViews, width, height, outputLayout);
			try {
				recordDraw(commandBuffer, heap, uniforms, width, height, vertexBuffer, ViewportData.defaultValue(), 0);
			} finally {
				KHRDynamicRendering.vkCmdEndRenderingKHR(commandBuffer);
			}
		}
	}

	int[] colorFormats() {
		return colorFormats.clone();
	}

	void validateDraw(VkCommandBuffer commandBuffer, VulkanSamplerHeap.Snapshot heap, VulkanResourceSets.Snapshot uniforms,
		long vertexBuffer) {
		if (pipeline == 0) {
			throw new IllegalStateException("Fullscreen pass is closed");
		}

		if ((uniforms == null ? null : uniforms.owner()) != uniformSets) {
			throw new IllegalArgumentException("Uniform set does not match the fullscreen pipeline");
		}

		if (!heapLayout.equals(heap.layout())) {
			throw new IllegalArgumentException("Sampler heap does not match the fullscreen pipeline layout");
		}

		if (commandBuffer.getDevice() != device || hasVertices && vertexBuffer == 0) {
			throw new IllegalArgumentException("Invalid fullscreen pass resources");
		}
	}

	public void bindFrame(VkCommandBuffer command, VulkanResourceSets.Snapshot frame) {
		frame.bind(command, pipelineLayout);
	}

	public void bind(VkCommandBuffer commandBuffer, VulkanSamplerHeap.Snapshot heap, VulkanResourceSets.Snapshot uniforms) {
		if (pipeline == 0 || commandBuffer.getDevice() != device) {
			throw new IllegalStateException("Invalid graphics pipeline binding");
		}

		if ((uniforms == null ? null : uniforms.owner()) != uniformSets || !heapLayout.equals(heap.layout())) {
			throw new IllegalArgumentException("Graphics descriptors do not match the pipeline");
		}

		if (uniforms != null) {
			uniforms.bind(commandBuffer, pipelineLayout);
		}

		heap.bind(commandBuffer, pipelineLayout, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
		VK10.vkCmdBindPipeline(commandBuffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);

		if (zeroVertexBuffer != 0) {
			try (var stack = MemoryStack.stackPush()) {
				VK10.vkCmdBindVertexBuffers(commandBuffer, zeroVertexBinding, stack.longs(zeroVertexBuffer), stack.longs(0));
			}
		}
	}

	void recordDraw(VkCommandBuffer commandBuffer, VulkanSamplerHeap.Snapshot heap, VulkanResourceSets.Snapshot uniforms, int width,
		int height, long vertexBuffer, ViewportData viewport, long indexBuffer) {
		int viewportWidth = (int) (width * viewport.scale());
		int viewportHeight = (int) (height * viewport.scale());

		if (viewportWidth == 0 || viewportHeight == 0) {
			return;
		}

		try (var stack = MemoryStack.stackPush()) {
			bind(commandBuffer, heap, uniforms);
			VK10.vkCmdSetViewport(commandBuffer, 0,
				VkViewport.calloc(1, stack).x((int) (width * viewport.viewportX())).y((int) (height * viewport.viewportY()))
					.width(viewportWidth).height(viewportHeight).minDepth(0).maxDepth(1));

			var scissor = VkRect2D.calloc(1, stack);
			scissor.extent().set(width, height);
			VK10.vkCmdSetScissor(commandBuffer, 0, scissor);

			if (hasVertices) {
				VK10.vkCmdBindVertexBuffers(commandBuffer, 0, stack.longs(vertexBuffer), stack.longs(0));
			}

			if (indexBuffer == 0) {
				VK10.vkCmdDraw(commandBuffer, 3, 1, 0, 0);
			} else {
				VK10.vkCmdBindIndexBuffer(commandBuffer, indexBuffer, 0, VK10.VK_INDEX_TYPE_UINT16);
				VK10.vkCmdDrawIndexed(commandBuffer, 6, 1, 0, 0, 0);
			}
		}
	}

	@Override
	public void close() {
		if (pipeline != 0) {
			VK10.vkDestroyPipeline(device, pipeline, null);
		}

		if (pipelineLayout != 0) {
			VK10.vkDestroyPipelineLayout(device, pipelineLayout, null);
		}

		if (emptySetLayout != 0) {
			VK10.vkDestroyDescriptorSetLayout(device, emptySetLayout, null);
		}

		pipeline = pipelineLayout = emptySetLayout = 0;
	}

	public static int fallbackVertexBinding(RenderPipeline pipeline) {
		for (int binding = 0; binding < pipeline.getVertexFormatBindings().size(); binding++) {
			if (pipeline.getVertexFormatBinding(binding) == null) {
				return binding;
			}
		}

		return pipeline.getVertexFormatBindings().size();
	}

	public record WorldState(RenderPipeline pipeline, int depthFormat, boolean shadow, long zeroVertexBuffer) {
	}

	public record Attribute(int location, int binding, int format, int offset) {
		public Attribute(int location, int format, int offset) {
			this(location, 0, format, offset);
		}

		public Attribute {
			if (location < 0 || binding < 0 || offset < 0) {
				throw new IllegalArgumentException("Invalid vertex attribute");
			}
		}
	}

	public static void beginRendering(VkCommandBuffer command, MemoryStack stack, long[] views, int width, int height, int imageLayout) {
		if (width <= 0 || height <= 0 || views.length == 0 || Arrays.stream(views).anyMatch(view -> view == 0)) {
			throw new IllegalArgumentException("Invalid rendering scope attachments");
		}

		if (imageLayout != VK10.VK_IMAGE_LAYOUT_GENERAL && imageLayout != VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {
			throw new IllegalArgumentException("Invalid rendering scope layout");
		}


		var attachments = VkRenderingAttachmentInfoKHR.calloc(views.length, stack);

		for (int i = 0; i < views.length; i++) {
			attachments.get(i).sType$Default().imageView(views[i]).imageLayout(imageLayout).loadOp(VK10.VK_ATTACHMENT_LOAD_OP_LOAD)
				.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
		}

		var rendering = VkRenderingInfoKHR.calloc(stack).sType$Default().layerCount(1).pColorAttachments(attachments);
		rendering.renderArea().extent().set(width, height);
		KHRDynamicRendering.vkCmdBeginRenderingKHR(command, rendering);
	}

}
