package net.irisshaders.iris.samplers;

import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public record SamplerBindingPlan(SamplerHeapLayout heap, Map<String, List<SamplerHeapLayout.Slot>> bindings) {
	public SamplerBindingPlan {
		Objects.requireNonNull(heap);

		Map<String, List<SamplerHeapLayout.Slot>> copy = new TreeMap<>();
		bindings.forEach((name, slots) -> {
			if (name == null || name.isBlank() || slots.isEmpty()) {
				throw new IllegalArgumentException("empty sampler binding");
			}

			for (var slot : slots) {
				heap.requireSlot(slot);
			}

			copy.put(name, List.copyOf(slots));
		});
		bindings = Collections.unmodifiableMap(copy);
	}

	public List<SamplerHeapLayout.Slot> require(String name, SamplerHeapLayout.Type type, int count) {
		var slots = this.bindings.get(name);

		if (slots == null || slots.size() != count || slots.stream().anyMatch(slot -> slot.type() != type)) {
			throw new IllegalArgumentException("missing or incompatible sampler plan for " + type.glsl + " " + name + "[" + count + "]");
		}

		return slots;
	}

	public static final class Builder {
		private final SamplerHeapLayout heap;
		private final Map<String, List<SamplerHeapLayout.Slot>> bindings = new HashMap<>();

		public Builder(SamplerHeapLayout heap) {
			this.heap = heap;
		}

		private static boolean isDisabled(String name, Set<Integer> flipped) {
			for (int index : flipped) {
				if (name.equals(
					"colortex" + index) || index >= 0 && index < PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.size() && name.equals(
					PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.get(index))) {
					return true;
				}
			}

			return false;
		}

		public Builder bind(String name, SamplerHeapLayout.Slot slot) {
			return this.bindArray(name, List.of(slot));
		}

		public Builder bindArray(String name, List<SamplerHeapLayout.Slot> slots) {
			this.bindings.put(name, List.copyOf(slots));

			return this;
		}

		public Builder colorTarget(int index, SamplerHeapLayout.Type type, boolean linear, Set<Integer> flipped, Set<Integer> mipsOnMain,
			Set<Integer> mipsOnAlt, boolean fullscreen) {
			if (index < 0) {
				throw new IllegalArgumentException("negative color target");
			}

			if (!fullscreen && index < 4) {
				return this;
			}

			boolean alt = flipped.contains(index);
			boolean mips = (alt ? mipsOnAlt : mipsOnMain).contains(index);
			String sampler = (linear ? "linear" : "nearest") + (mips ? "/mips" : "");
			var slot = this.heap.slot(type, "colortex" + index + (alt ? "/alt" : "/main"), sampler);
			this.bind("colortex" + index, slot);

			if (index < PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.size()) {
				this.bind(PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.get(index), slot);
			}

			if (index == 0 && fullscreen) {
				this.bind("gtexture", slot);
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
						this.bind(name, slot);
					}

					break;
				}
			}

			return this;
		}

		public SamplerBindingPlan build() {
			return new SamplerBindingPlan(this.heap, this.bindings);
		}
	}
}
