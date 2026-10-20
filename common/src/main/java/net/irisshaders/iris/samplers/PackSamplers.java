package net.irisshaders.iris.samplers;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.PackResources;
import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PackSamplers implements AutoCloseable {
	private final PackResources resources;
	private final ShaderStorageBufferHolder storageBuffers;
	private final Map<Key, Reference> references = new LinkedHashMap<>();
	private final Set<Reference> drawReferences = new HashSet<>();
	private final Map<Reference, SamplerHeapLayout.Slot> slots = new HashMap<>();
	private SamplerHeapLayout layout;
	private boolean closed;

	public PackSamplers() {
		this(null);
	}

	public PackSamplers(ShaderStorageBufferHolder storageBuffers) {
		this.storageBuffers = storageBuffers;
		this.resources = Iris.getRenderBackend().createPackResources(this);
	}

	public ShaderStorageBuffer getStorageBuffer(int binding) {
		if (this.storageBuffers == null) {
			throw new IllegalArgumentException("no bufferObject." + binding + " declared by the shader pack");
		}
		return this.storageBuffers.getBuffer(binding);
	}

	public Map<String, List<GpuBufferSlice>> getBufferBindings(ShaderResourceLayout layout) {
		var bindings = new LinkedHashMap<String, List<GpuBufferSlice>>();
		for (var descriptor : layout.descriptors()) {
			if (descriptor.kind() != ShaderResourceLayout.Kind.STORAGE_BUFFER) continue;
			int first = descriptor.originalBinding() == null ? 0 : descriptor.originalBinding();
			var slices = new ArrayList<GpuBufferSlice>();
			for (int i = 0; i < descriptor.count(); i++) {
				slices.add(this.getStorageBuffer(Math.addExact(first, i)).slice());
			}
			bindings.put(descriptor.name(), slices);
		}
		return bindings;
	}

	public PackResources getResources() {
		return this.resources;
	}

	public void beginFrame() {
		this.beginFrame(null);
	}

	public void beginFrame(GpuBufferSlice frameUniforms) {
		this.resources.beginFrame(frameUniforms);
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
