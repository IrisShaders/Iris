package net.irisshaders.iris.vulkan;

import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VK10;

import java.nio.ByteBuffer;

public class VulkanShaderCompiler implements AutoCloseable {
	private long compiler = Shaderc.shaderc_compiler_initialize();
	private long options;

	public VulkanShaderCompiler() {
		if (this.compiler == 0) {
			throw new IllegalStateException("could not initialize shaderc");
		}

		this.options = Shaderc.shaderc_compile_options_initialize();

		if (this.options == 0) {
			this.close();

			throw new IllegalStateException("could not initialize shaderc options");
		}

		Shaderc.shaderc_compile_options_set_target_env(this.options, Shaderc.shaderc_target_env_vulkan,
			Shaderc.shaderc_env_version_vulkan_1_2);
		// Without debug info, optimization also strips SPIR-V names. MoltenVK would otherwise
		// preserve pack identifiers that collide with Metal built-ins (e.g. length_squared).
		// TODO: This sucks!!! We need to be able to use debug info. So we should use glsl transformer to rename
		Shaderc.shaderc_compile_options_set_optimization_level(this.options, Shaderc.shaderc_optimization_level_performance);
		Shaderc.shaderc_compile_options_set_target_spirv(this.options, Shaderc.shaderc_spirv_version_1_5);
	}

	public static int stageFlag(PatchShaderType stage) {
		return switch (stage) {
			case VERTEX -> VK10.VK_SHADER_STAGE_VERTEX_BIT;
			case TESS_CONTROL -> VK10.VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT;
			case TESS_EVAL -> VK10.VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT;
			case GEOMETRY -> VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
			case FRAGMENT -> VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
			case COMPUTE -> VK10.VK_SHADER_STAGE_COMPUTE_BIT;
		};
	}

	public ByteBuffer compile(String name, PatchShaderType stage, String source) {
		if (this.compiler == 0) {
			throw new IllegalStateException("shader compiler is closed");
		}

		int kind = switch (stage) {
			case VERTEX -> Shaderc.shaderc_vertex_shader;
			case FRAGMENT -> Shaderc.shaderc_fragment_shader;
			case GEOMETRY -> Shaderc.shaderc_geometry_shader;
			case TESS_CONTROL -> Shaderc.shaderc_tess_control_shader;
			case TESS_EVAL -> Shaderc.shaderc_tess_evaluation_shader;
			case COMPUTE -> Shaderc.shaderc_compute_shader;
		};
		long result;
		ByteBuffer sourceBytes = MemoryUtil.memUTF8(source, false);
		ByteBuffer nameBytes = MemoryUtil.memUTF8(name);
		ByteBuffer entryBytes = MemoryUtil.memUTF8("main");

		try {
			result = Shaderc.shaderc_compile_into_spv(this.compiler, sourceBytes, kind, nameBytes, entryBytes, this.options);
		} finally {
			MemoryUtil.memFree(entryBytes);
			MemoryUtil.memFree(nameBytes);
			MemoryUtil.memFree(sourceBytes);
		}

		if (result == 0) {
			throw new IllegalStateException("shaderc returned no result for " + name);
		}

		try {
			if (Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
				throw new IllegalArgumentException("failed to compile " + name + ": " + Shaderc.shaderc_result_get_error_message(result));
			}

			ByteBuffer bytes = Shaderc.shaderc_result_get_bytes(result);

			return MemoryUtil.memAlloc(bytes.remaining()).put(bytes).flip();
		} finally {
			Shaderc.shaderc_result_release(result);
		}
	}

	@Override
	public void close() {
		if (this.options != 0) {
			Shaderc.shaderc_compile_options_release(this.options);
		}

		if (this.compiler != 0) {
			Shaderc.shaderc_compiler_release(this.compiler);
		}

		this.options = this.compiler = 0;
	}
}
