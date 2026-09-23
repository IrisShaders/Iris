package net.irisshaders.iris.samplers;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public record SamplerHeapLayout(Map<Type, List<Slot>> heaps, Map<Type, List<Slot>> drawHeaps) {
	public static final int DESCRIPTOR_SET = 1;
	public static final int DRAW_DESCRIPTOR_SET = 3;

	public SamplerHeapLayout(Map<Type, List<Slot>> heaps) {
		this(heaps, Map.of());
	}

	public SamplerHeapLayout {
		heaps = validate(heaps);
		drawHeaps = validate(drawHeaps);
	}

	private static Map<Type, List<Slot>> validate(Map<Type, List<Slot>> heaps) {
		Map<Type, List<Slot>> copy = new EnumMap<>(Type.class);

		for (var entry : heaps.entrySet()) {
			List<Slot> slots = List.copyOf(entry.getValue());

			if (slots.isEmpty()) {
				throw new IllegalArgumentException("empty sampler heap");
			}

			Set<Key> keys = new HashSet<>();

			for (int i = 0; i < slots.size(); i++) {
				Slot slot = slots.get(i);

				if (slot.type() != entry.getKey() || slot.index() != i || !keys.add(slot.key())) {
					throw new IllegalArgumentException("invalid sampler heap slots");
				}
			}

			copy.put(entry.getKey(), slots);
		}

		return Collections.unmodifiableMap(copy);
	}

	public Slot slot(Type type, String resource, String sampler) {
		Key key = new Key(resource, sampler);

		return Stream.concat(this.heaps.getOrDefault(type, List.of()).stream(), this.drawHeaps.getOrDefault(type, List.of()).stream())
			.filter(slot -> slot.key().equals(key)).findFirst()
			.orElseThrow(() -> new IllegalArgumentException("sampler was not allocated in the pack heap: " + type + " " + key));
	}

	public void requireSlot(Slot slot) {
		var slots = (this.isDraw(slot) ? this.drawHeaps : this.heaps).getOrDefault(slot.type(), List.of());

		if (slot.index() >= slots.size() || !slots.get(slot.index()).equals(slot)) {
			throw new IllegalArgumentException("slot belongs to a different sampler heap: " + slot);
		}
	}

	public boolean isDraw(Slot slot) {
		return this.drawHeaps.getOrDefault(slot.type(), List.of()).contains(slot);
	}

	public int descriptorCount() {
		return this.heaps.values().stream().mapToInt(List::size).reduce(0, Math::addExact);
	}

	public enum Type {
		SAMPLER2D("sampler2D"),
		ISAMPLER2D("isampler2D"),
		USAMPLER2D("usampler2D"),
		SAMPLER2DSHADOW("sampler2DShadow"),
		SAMPLER3D("sampler3D"),
		ISAMPLER3D("isampler3D"),
		USAMPLER3D("usampler3D"),
		SAMPLERCUBE("samplerCube"),
		ISAMPLERCUBE("isamplerCube"),
		USAMPLERCUBE("usamplerCube"),
		SAMPLERCUBESHADOW("samplerCubeShadow"),
		SAMPLER2DARRAY("sampler2DArray"),
		ISAMPLER2DARRAY("isampler2DArray"),
		USAMPLER2DARRAY("usampler2DArray"),
		SAMPLER2DARRAYSHADOW("sampler2DArrayShadow"),
		SAMPLER1D("sampler1D"),
		ISAMPLER1D("isampler1D"),
		USAMPLER1D("usampler1D"),
		SAMPLER1DSHADOW("sampler1DShadow"),
		SAMPLER1DARRAY("sampler1DArray"),
		ISAMPLER1DARRAY("isampler1DArray"),
		USAMPLER1DARRAY("usampler1DArray"),
		SAMPLER1DARRAYSHADOW("sampler1DArrayShadow"),
		SAMPLERCUBEARRAY("samplerCubeArray"),
		ISAMPLERCUBEARRAY("isamplerCubeArray"),
		USAMPLERCUBEARRAY("usamplerCubeArray"),
		SAMPLERCUBEARRAYSHADOW("samplerCubeArrayShadow"),
		SAMPLER2DMS("sampler2DMS"),
		ISAMPLER2DMS("isampler2DMS"),
		USAMPLER2DMS("usampler2DMS"),
		SAMPLER2DMSARRAY("sampler2DMSArray"),
		ISAMPLER2DMSARRAY("isampler2DMSArray"),
		USAMPLER2DMSARRAY("usampler2DMSArray"),
		SAMPLERBUFFER("samplerBuffer"),
		ISAMPLERBUFFER("isamplerBuffer"),
		USAMPLERBUFFER("usamplerBuffer");

		public final String glsl;

		Type(String glsl) {
			this.glsl = glsl;
		}

		public int binding() {
			return this.ordinal();
		}

		public boolean texelBuffer() {
			return this.glsl.endsWith("Buffer");
		}

		public String drawUniformName() {
			return "iris_draw_" + this.glsl;
		}

		public String uniformName() {
			return "iris_heap_" + this.glsl;
		}
	}

	public record Key(String resource, String sampler) implements Comparable<Key> {
		public Key {
			if (resource == null || resource.isBlank() || sampler == null || sampler.isBlank()) {
				throw new IllegalArgumentException("resource and sampler keys must be nonempty");
			}
		}

		@Override
		public int compareTo(Key other) {
			int order = this.resource.compareTo(other.resource);

			return order == 0 ? this.sampler.compareTo(other.sampler) : order;
		}
	}

	public record Slot(Type type, int index, Key key) {
		public Slot {
			Objects.requireNonNull(type);
			Objects.requireNonNull(key);

			if (index < 0) {
				throw new IllegalArgumentException("negative heap index");
			}
		}
	}

	public static final class Builder {
		private final Map<Type, Set<Key>> resources = new EnumMap<>(Type.class);

		public Builder add(Type type, String resource, String sampler) {
			this.resources.computeIfAbsent(type, ignored -> new TreeSet<>()).add(new Key(resource, sampler));

			return this;
		}

		public Builder colorTarget(int index, Type type, boolean linear) {
			if (index < 0 || (type != Type.SAMPLER2D && type != Type.ISAMPLER2D && type != Type.USAMPLER2D) || linear && type != Type.SAMPLER2D) {
				throw new IllegalArgumentException("invalid color target sampler");
			}

			for (String side : List.of("main", "alt")) {
				String resource = "colortex" + index + "/" + side;
				String sampler = linear ? "linear" : "nearest";
				this.add(type, resource, sampler);
				this.add(type, resource, sampler + "/mips");
			}

			return this;
		}

		public SamplerHeapLayout build() {
			Map<Type, List<Slot>> heaps = new EnumMap<>(Type.class);
			this.resources.forEach((type, keys) -> {
				List<Slot> slots = new ArrayList<>();

				for (Key key : keys) {
					slots.add(new Slot(type, slots.size(), key));
				}

				heaps.put(type, slots);
			});

			return new SamplerHeapLayout(heaps);
		}
	}
}
