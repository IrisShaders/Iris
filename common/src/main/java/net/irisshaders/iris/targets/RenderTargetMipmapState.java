package net.irisshaders.iris.targets;

import java.util.HashSet;
import java.util.Set;

/** Mipmap availability at a fixed point in the frame's pass sequence. */
public record RenderTargetMipmapState(Set<Integer> main, Set<Integer> alt) {
	public static final RenderTargetMipmapState BASE = new RenderTargetMipmapState(Set.of(), Set.of());

	public RenderTargetMipmapState {
		main = Set.copyOf(main);
		alt = Set.copyOf(alt);
	}

	public boolean mipmapped(int target, boolean readFromAlt) {
		return (readFromAlt ? this.alt : this.main).contains(target);
	}

	public RenderTargetMipmapState afterGeneration(Set<Integer> buffers, Set<Integer> flipped) {
		var main = new HashSet<>(this.main);
		var alt = new HashSet<>(this.alt);
		for (int buffer : buffers) {
			(flipped.contains(buffer) ? alt : main).add(buffer);
		}
		return new RenderTargetMipmapState(main, alt);
	}
}
