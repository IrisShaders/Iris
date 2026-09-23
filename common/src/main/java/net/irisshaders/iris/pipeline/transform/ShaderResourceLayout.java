package net.irisshaders.iris.pipeline.transform;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ShaderResourceLayout(int uniformBufferSize, Map<String, Uniform> uniforms, List<Descriptor> descriptors) {
	public static final String UNIFORM_BLOCK = "iris_Uniforms";
	public static final String FRAME_BLOCK = "iris_FrameUniforms";

	public ShaderResourceLayout {
		uniforms = Map.copyOf(uniforms);
		descriptors = List.copyOf(descriptors);
	}

	public enum Kind {
		UNIFORM_BUFFER,
		SAMPLER,
		IMAGE,
		STORAGE_BUFFER
	}

	public record Uniform(int offset, Std140Type type) {
	}

	public record Descriptor(String name, Kind kind, String type, int count, int set, int binding, Integer originalBinding, Set<PatchShaderType> stages) {
		public Descriptor {
			stages = Set.copyOf(stages);
		}
	}
}
