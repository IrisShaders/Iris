package net.irisshaders.iris.vulkan;

import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;

import java.nio.ByteBuffer;

public final class VulkanShaderCompiler implements AutoCloseable {
	private long compiler = Shaderc.shaderc_compiler_initialize();
	private long options;

	public VulkanShaderCompiler() {
		if (compiler == 0) {
			throw new IllegalStateException(
				"Could not initialize shaderc");
		}

		options = Shaderc.shaderc_compile_options_initialize();

		if (options == 0) {
			close();

			throw new IllegalStateException(
				"Could not initialize shaderc options");
		}

		Shaderc.shaderc_compile_options_set_target_env(options,
			Shaderc.shaderc_target_env_vulkan,
			Shaderc.shaderc_env_version_vulkan_1_2);
		Shaderc.shaderc_compile_options_set_generate_debug_info(options);
		Shaderc.shaderc_compile_options_set_target_spirv(options,
			Shaderc.shaderc_spirv_version_1_5);
	}

	public synchronized ByteBuffer compile(String name,
		PatchShaderType stage,
		String source) {
		if (compiler == 0) {
			throw new IllegalStateException(
				"Shader compiler is closed");
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
			result = Shaderc.shaderc_compile_into_spv(compiler, sourceBytes,
				kind, nameBytes,
				entryBytes, options);
		} finally {
			MemoryUtil.memFree(entryBytes);
			MemoryUtil.memFree(nameBytes);
			MemoryUtil.memFree(sourceBytes);
		}

		if (result == 0) {
			throw new IllegalStateException(
				"shaderc returned no result for " + name);
		}

		try {
			if (Shaderc.shaderc_result_get_compilation_status(
				result) != Shaderc.shaderc_compilation_status_success) {
				throw new IllegalArgumentException(
					name + ": " + Shaderc.shaderc_result_get_error_message(
						result));
			}

			ByteBuffer bytes = Shaderc.shaderc_result_get_bytes(result);

			return MemoryUtil.memAlloc(bytes.remaining())
				.put(bytes)
				.flip();
		} finally {
			Shaderc.shaderc_result_release(result);
		}
	}

	@Override
	public synchronized void close() {
		if (options != 0) {
			Shaderc.shaderc_compile_options_release(options);
		}

		if (compiler != 0) {
			Shaderc.shaderc_compiler_release(compiler);
		}

		options = compiler = 0;
	}
}
