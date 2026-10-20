package net.irisshaders.iris.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.util.spvc.SpvcReflectedBuiltinResource;
import org.lwjgl.util.spvc.SpvcReflectedResource;
import org.lwjgl.util.spvc.Spv;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

import java.nio.ByteBuffer;

public class VulkanShaderInterface {
	public static void validate(VkDevice device, String name, ByteBuffer spirv) {
		try (var stack = MemoryStack.stackPush()) {
			var pointer = stack.mallocPointer(1);
			check(Spvc.spvc_context_create(pointer));
			long context = pointer.get(0);
			try {
				var words = spirv.asIntBuffer();
				check(Spvc.spvc_context_parse_spirv(context, words, words.remaining(), pointer));
				check(Spvc.spvc_context_create_compiler(context, Spvc.SPVC_BACKEND_NONE, pointer.get(0), Spvc.SPVC_CAPTURE_MODE_TAKE_OWNERSHIP, pointer));
				long compiler = pointer.get(0);
				int stage = Spvc.spvc_compiler_get_execution_model(compiler);
				if (stage == Spv.SpvExecutionModelGLCompute) return;
				check(Spvc.spvc_compiler_create_shader_resources(compiler, pointer));
				long resources = pointer.get(0);
				var properties = VkPhysicalDeviceProperties.calloc(stack);
				VK10.vkGetPhysicalDeviceProperties(device.getPhysicalDevice(), properties);
				var limits = properties.limits();
				int input = switch (stage) {
					case Spv.SpvExecutionModelTessellationControl -> limits.maxTessellationControlPerVertexInputComponents();
					case Spv.SpvExecutionModelTessellationEvaluation -> limits.maxTessellationEvaluationInputComponents();
					case Spv.SpvExecutionModelGeometry -> limits.maxGeometryInputComponents();
					case Spv.SpvExecutionModelFragment -> limits.maxFragmentInputComponents();
					default -> 0;
				};
				int output = switch (stage) {
					case Spv.SpvExecutionModelVertex -> limits.maxVertexOutputComponents();
					case Spv.SpvExecutionModelTessellationControl -> limits.maxTessellationControlPerVertexOutputComponents();
					case Spv.SpvExecutionModelTessellationEvaluation -> limits.maxTessellationEvaluationOutputComponents();
					case Spv.SpvExecutionModelGeometry -> limits.maxGeometryOutputComponents();
					default -> 0;
				};
				if (input != 0) validate(stack, compiler, resources, name, true, input, stage);
				if (output != 0) validate(stack, compiler, resources, name, false, output, stage);
			} finally {
				Spvc.spvc_context_destroy(context);
			}
		}
	}

	private static void check(int result) {
		if (result != Spvc.SPVC_SUCCESS) throw new IllegalArgumentException("failed to inspect Vulkan shader interface: " + result);
	}

	private static void validate(MemoryStack stack, long compiler, long resources, String name, boolean input, int limit, int stage) {
		var pointer = stack.mallocPointer(1);
		var count = stack.mallocPointer(1);
		check(Spvc.spvc_resources_get_resource_list_for_type(resources, input ? Spvc.SPVC_RESOURCE_TYPE_STAGE_INPUT : Spvc.SPVC_RESOURCE_TYPE_STAGE_OUTPUT, pointer, count));
		var variables = SpvcReflectedResource.create(pointer.get(0), (int) count.get(0));
		int end = 0;
		for (var variable : variables) {
			if (Spvc.spvc_compiler_has_decoration(compiler, variable.id(), Spv.SpvDecorationPatch)) continue;
			boolean perVertex = input && (stage == Spv.SpvExecutionModelGeometry || stage == Spv.SpvExecutionModelTessellationControl || stage == Spv.SpvExecutionModelTessellationEvaluation)
				|| !input && stage == Spv.SpvExecutionModelTessellationControl;
			int location = Spvc.spvc_compiler_get_decoration(compiler, variable.id(), Spv.SpvDecorationLocation);
			int component = Spvc.spvc_compiler_get_decoration(compiler, variable.id(), Spv.SpvDecorationComponent);
			end = Math.max(end, end(compiler, variable.type_id(), location * 4 + component, perVertex));
		}
		check(Spvc.spvc_resources_get_builtin_resource_list_for_type(resources, input ? Spvc.SPVC_BUILTIN_RESOURCE_TYPE_STAGE_INPUT : Spvc.SPVC_BUILTIN_RESOURCE_TYPE_STAGE_OUTPUT, pointer, count));
		int builtins = 0;
		for (var builtin : SpvcReflectedBuiltinResource.create(pointer.get(0), (int) count.get(0))) {
			if (builtin.builtin() == Spv.SpvBuiltInTessLevelInner || builtin.builtin() == Spv.SpvBuiltInTessLevelOuter) continue;
			builtins += components(compiler, builtin.value_type_id());
		}
		if ((long) end + builtins > limit) {
			throw new IllegalArgumentException("Vulkan shader " + name + " " + (input ? "input" : "output") + " interface uses " + end + " location components and " + builtins + " built-in components, exceeding device limit " + limit);
		}
	}

	private static int components(long compiler, int id) {
		long type = Spvc.spvc_compiler_get_type_handle(compiler, id);
		int size = 0;
		if (Spvc.spvc_type_get_basetype(type) == Spvc.SPVC_BASETYPE_STRUCT) {
			for (int i = 0; i < Spvc.spvc_type_get_num_member_types(type); i++) size = Math.addExact(size, components(compiler, Spvc.spvc_type_get_member_type(type, i)));
		} else {
			size = Spvc.spvc_type_get_vector_size(type) * Spvc.spvc_type_get_columns(type) * Math.max(1, Spvc.spvc_type_get_bit_width(type) / 32);
		}
		for (int i = 0; i < Spvc.spvc_type_get_num_array_dimensions(type); i++) size = Math.multiplyExact(size, Spvc.spvc_type_get_array_dimension(type, i));
		return size;
	}

	private static int end(long compiler, int id, int start, boolean perVertex) {
		long type = Spvc.spvc_compiler_get_type_handle(compiler, id);
		int end = start;
		if (Spvc.spvc_type_get_basetype(type) == Spvc.SPVC_BASETYPE_STRUCT) {
			int base = Spvc.spvc_type_get_base_type_id(type);
			for (int i = 0; i < Spvc.spvc_type_get_num_member_types(type); i++) {
				if (Spvc.spvc_compiler_has_member_decoration(compiler, base, i, Spv.SpvDecorationBuiltIn)) continue;
				int member = (end + 3) / 4 * 4;
				if (Spvc.spvc_compiler_has_member_decoration(compiler, base, i, Spv.SpvDecorationLocation)) {
					member = Spvc.spvc_compiler_get_member_decoration(compiler, base, i, Spv.SpvDecorationLocation) * 4
						+ Spvc.spvc_compiler_get_member_decoration(compiler, base, i, Spv.SpvDecorationComponent);
				}
				end = Math.max(end, end(compiler, Spvc.spvc_type_get_member_type(type, i), member, false));
			}
		} else {
			int vector = Spvc.spvc_type_get_vector_size(type) * Math.max(1, Spvc.spvc_type_get_bit_width(type) / 32);
			end = Math.addExact(start, (Spvc.spvc_type_get_columns(type) - 1) * ((vector + 3) / 4 * 4) + vector);
		}
		int elements = 1;
		for (int i = 0; i < Spvc.spvc_type_get_num_array_dimensions(type) - (perVertex ? 1 : 0); i++) elements = Math.multiplyExact(elements, Spvc.spvc_type_get_array_dimension(type, i));
		return Math.addExact(end, Math.multiplyExact(elements - 1, (end - start + 3) / 4 * 4));
	}
}
