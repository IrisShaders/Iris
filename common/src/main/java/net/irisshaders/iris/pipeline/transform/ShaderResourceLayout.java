package net.irisshaders.iris.pipeline.transform;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ShaderResourceLayout(int uniformBufferSize, Map<String, Uniform> uniforms, List<Descriptor> descriptors,
	Map<String, Uniform> pushUniforms) {
	public static final String UNIFORM_BLOCK = "iris_Uniforms";
	public static final String FRAME_BLOCK = "iris_FrameUniforms";

	public ShaderResourceLayout(int uniformBufferSize, Map<String, Uniform> uniforms, List<Descriptor> descriptors) {
		this(uniformBufferSize, uniforms, descriptors, Map.of());
	}

	public ShaderResourceLayout {
		uniforms = Map.copyOf(uniforms);
		descriptors = List.copyOf(descriptors);
		pushUniforms = Map.copyOf(pushUniforms);
	}

	public enum Kind {
		UNIFORM_BUFFER,
		SAMPLER,
		IMAGE,
		STORAGE_BUFFER
	}

	public record Uniform(int offset, Std140Type type) {
	}

	public enum Access {
		READ_ONLY,
		WRITE_ONLY,
		READ_WRITE;

		public boolean writes() {
			return this != READ_ONLY;
		}

		public Access merge(Access other) {
			return this == other ? this : READ_WRITE;
		}
	}

	public record Descriptor(String name, Kind kind, String type, int count, int set, int binding, Integer originalBinding,
		Set<PatchShaderType> stages, Access access) {
		public Descriptor(String name, Kind kind, String type, int count, int set, int binding, Integer originalBinding,
			Set<PatchShaderType> stages) {
			this(name, kind, type, count, set, binding, originalBinding, stages,
				kind == Kind.IMAGE || kind == Kind.STORAGE_BUFFER ? Access.READ_WRITE : Access.READ_ONLY);
		}

		public Descriptor {
			stages = Set.copyOf(stages);
		}
	}
}
