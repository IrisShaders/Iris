package net.irisshaders.iris.samplers;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.PackResources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class PackSamplers implements AutoCloseable {
	private final PackResources resources;
	private final Map<Key, Reference> references = new LinkedHashMap<>();
	private final Set<Reference> drawReferences = new HashSet<>();
	private final Map<Reference, SamplerHeapLayout.Slot> slots = new HashMap<>();
	private SamplerHeapLayout layout;
	private boolean closed;

	public PackSamplers() {
		this.resources = Iris.getRenderBackend().createPackResources(this);
	}

	public PackResources getResources() {
		return this.resources;
	}

	public void beginFrame() {
		this.resources.beginFrame();
	}

	public Reference register(SamplerHeapLayout.Type type, Object resource) {
		return this.register(type, resource, false);
	}

	public Reference register(SamplerHeapLayout.Type type, Object resource, boolean draw) {
		if (this.closed) {
			throw new IllegalStateException("pack samplers are closed");
		}

		var key = new Key(type, resource, draw);
		var previous = this.references.get(key);

		if (previous != null) {
			return previous;
		}

		if (this.layout != null) {
			throw new IllegalStateException("sampler discovery already finished: " + resource);
		}

		var ref = new Reference(type, "resource/" + this.references.size());
		this.references.put(key, ref);

		if (draw) {
			this.drawReferences.add(ref);
		}

		return ref;
	}

	public SamplerHeapLayout getLayout() {
		if (this.closed) {
			throw new IllegalStateException("pack samplers are closed");
		}

		if (this.layout == null) {
			var builder = new SamplerHeapLayout.Builder();
			var draws = new SamplerHeapLayout.Builder();

			for (var ref : this.references.values()) {
				(this.drawReferences.contains(ref) ? draws : builder).add(ref.type(), ref.resource(), "iris");
			}

			this.layout = new SamplerHeapLayout(builder.build().heaps(), draws.build().heaps());
		}

		return this.layout;
	}

	public SamplerHeapLayout.Slot getSlot(Reference ref) {
		var layout = this.getLayout();
		return this.slots.computeIfAbsent(ref, reference -> layout.slot(reference.type(), reference.resource(), "iris"));
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;
		this.resources.close();
	}

	private record Key(SamplerHeapLayout.Type type, Object resource, boolean draw) {
	}

	public record Reference(SamplerHeapLayout.Type type, String resource) {
	}
}
