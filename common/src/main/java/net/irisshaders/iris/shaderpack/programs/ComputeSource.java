package net.irisshaders.iris.shaderpack.programs;

import net.irisshaders.iris.shaderpack.properties.IndirectPointer;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import org.joml.Vector2f;
import org.joml.Vector3i;

import java.util.Optional;

public class ComputeSource {
	private final String name;
	private final String source;
	private final ProgramSet parent;
	private final IndirectPointer indirectPointer;
	private Vector3i workGroups;
	private Vector2f workGroupRelative;

	public ComputeSource(String name, String source, ProgramSet parent, ShaderProperties properties) {
		this.name = name;
		this.source = source;
		this.parent = parent;
		this.indirectPointer = properties.getIndirectPointers().get(name);
	}

	public String getName() {
		return name;
	}

	public Optional<String> getSource() {
		return Optional.ofNullable(source);
	}

	public ProgramSet getParent() {
		return parent;
	}

	public boolean isValid() {
		return source != null;
	}

	public Vector2f getWorkGroupRelative() {
		return workGroupRelative;
	}

	public void setWorkGroupRelative(Vector2f workGroupRelative) {
		this.workGroupRelative = workGroupRelative;
	}

	public Vector3i getWorkGroups() {
		return workGroups;
	}

	public void setWorkGroups(Vector3i workGroups) {
		this.workGroups = workGroups;
	}

	public IndirectPointer getIndirectPointer() {
		return indirectPointer;
	}

	public Optional<ComputeSource> requireValid() {
		if (this.isValid()) {
			return Optional.of(this);
		} else {
			return Optional.empty();
		}
	}
}
