package net.irisshaders.iris.pipeline.transform;

import java.util.Set;

/** Shader storage choices, independent of value suppliers and update frequency. */
public record UniformPlan(ShaderResourceLayout frame, Set<String> draw, Profile profile) {
	public static final UniformPlan LOCAL = new UniformPlan(null, Set.of(), Profile.STANDARD);

	public UniformPlan {
		draw = Set.copyOf(draw);
	}

	public boolean isFrame(String name) {
		return this.frame != null && this.frame.uniforms().containsKey(name) && !this.draw.contains(name);
	}

	public enum Profile {
		STANDARD,
		DH
	}
}
