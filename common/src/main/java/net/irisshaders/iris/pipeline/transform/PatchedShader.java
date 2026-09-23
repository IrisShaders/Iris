package net.irisshaders.iris.pipeline.transform;

import net.irisshaders.iris.samplers.SamplerBindingPlan;

import java.util.BitSet;
import java.util.Map;

public record PatchedShader(Map<PatchShaderType, String> sources, ShaderResourceLayout layout, SamplerBindingPlan samplers, BitSet writtenAttachments) {
	public PatchedShader {
		sources = Map.copyOf(sources);
		writtenAttachments = writtenAttachments == null ? null : (BitSet) writtenAttachments.clone();
	}

	@Override
	public BitSet writtenAttachments() {
		return this.writtenAttachments == null ? null : (BitSet) this.writtenAttachments.clone();
	}
}
