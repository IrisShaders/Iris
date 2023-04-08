package net.irisshaders.iris.shaderpack;

import org.joml.Vector2f;
import org.joml.Vector3i;

import java.util.Optional;

public class ComputeSource {
	private final String name;
	private final String source;
	private final ProgramSet parent;
	private Vector3i workGroups;
	private Vector2f workGroupRelative;

	public ComputeSource(String name, String source, ProgramSet parent) {
		this.name = name;
		this.source = source;
		this.parent = parent;
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

	public Optional<ComputeSource> requireValid() {
		if (this.isValid()) {
			return Optional.of(this);
		} else {
			return Optional.empty();
		}
	}
}
