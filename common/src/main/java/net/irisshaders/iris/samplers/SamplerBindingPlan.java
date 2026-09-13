package net.irisshaders.iris.samplers;

import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public record SamplerBindingPlan(SamplerHeapLayout heap, Map<String, List<SamplerHeapLayout.Slot>> bindings, boolean indirect) {
	public SamplerBindingPlan(SamplerHeapLayout heap, Map<String, List<SamplerHeapLayout.Slot>> bindings) {
		this(heap, bindings, false);
	}

	public static final String INDEX_BLOCK = "iris_SamplerIndices";

	public List<SamplerHeapLayout.Slot> indexSlots() {
		return bindings.values()
			.stream()
			.flatMap(List::stream)
			.distinct()
			.sorted(
				Comparator.comparingInt((SamplerHeapLayout.Slot s) -> s.type().ordinal()).thenComparingInt(SamplerHeapLayout.Slot::index))
			.toList();
	}

	public int indexBytes() {
		return ((indexSlots().size() + 3) / 4) * 16;
	}

	public SamplerBindingPlan {
		Objects.requireNonNull(heap);

		Map<String, List<SamplerHeapLayout.Slot>> copy = new TreeMap<>();
		bindings.forEach((name, slots) -> {
			if (name == null || name.isBlank() || slots.isEmpty()) {
				throw new IllegalArgumentException("Empty sampler binding");
			}

			for (var slot : slots) {
				heap.requireSlot(slot);
			}

			copy.put(name, List.copyOf(slots));
		});
		bindings = Collections.unmodifiableMap(copy);
	}

	public List<SamplerHeapLayout.Slot> require(String name, SamplerHeapLayout.Type type, int count) {
		var slots = bindings.get(name);

		if (slots == null || slots.size() != count || slots.stream().anyMatch(slot -> slot.type() != type)) {
			throw new IllegalArgumentException("Missing or incompatible sampler plan for " + type.glsl + " " + name + "[" + count + "]");
		}

		return slots;
	}

	public static final class Builder {
		private final SamplerHeapLayout heap;
		private final Map<String, List<SamplerHeapLayout.Slot>> bindings = new HashMap<>();

		public Builder(SamplerHeapLayout heap) {
			this.heap = heap;
		}

		public Builder bind(String name, SamplerHeapLayout.Slot slot) {
			return bindArray(name, List.of(slot));
		}

		public Builder bindArray(String name, List<SamplerHeapLayout.Slot> slots) {
			bindings.put(name, List.copyOf(slots));

			return this;
		}

		public Builder colorTarget(int index, SamplerHeapLayout.Type type, boolean linear, Set<Integer> flipped, Set<Integer> mipsOnMain,
			Set<Integer> mipsOnAlt, boolean fullscreen) {
			if (index < 0) {
				throw new IllegalArgumentException("Negative color target");
			}

			if (!fullscreen && index < 4) {
				return this;
			}

			boolean alt = flipped.contains(index);
			boolean mips = (alt ? mipsOnAlt : mipsOnMain).contains(index);
			String sampler = (linear ? "linear" : "nearest") + (mips ? "/mips" : "");
			var slot = heap.slot(type, "colortex" + index + (alt ? "/alt" : "/main"), sampler);
			bind("colortex" + index, slot);

			if (index < PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.size()) {
				bind(PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.get(index), slot);
			}

			if (index == 0 && fullscreen) {
				bind("gtexture", slot);
			}

			return this;
		}

		public Builder override(Map<String, SamplerHeapLayout.Slot> overrides, Set<Integer> flippedAtLeastOnce, String... aliases) {
			for (String alias : aliases) {
				if (isDisabled(alias, flippedAtLeastOnce)) {
					continue;
				}

				var slot = overrides.get(alias);

				if (slot != null) {
					for (String name : aliases) {
						bind(name, slot);
					}

					break;
				}
			}

			return this;
		}

		private static boolean isDisabled(String name, Set<Integer> flipped) {
			for (int index : flipped) {
				if (name.equals("colortex" + index) || index >= 0 && index < PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.size() &&
					name.equals(PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.get(index))) {
					return true;
				}
			}

			return false;
		}

		public SamplerBindingPlan build() {
			return new SamplerBindingPlan(heap, bindings);
		}
	}
}
