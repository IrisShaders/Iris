package net.coderbot.iris.shaderpack;

import java.util.Optional;

public class ProgramSource {
	private final String name;
	private final String computeSource;
	private final String vertexSource;
	private final String geometrySource;
	private final String fragmentSource;
	private final ProgramDirectives directives;
	private final ProgramSet parent;

	public ProgramSource(String name, String computeSource, String vertexSource, String geometrySource, String fragmentSource, ProgramSet parent, ShaderProperties properties) {
		this.name = name;
		this.computeSource = computeSource;
		this.vertexSource = vertexSource;
		this.geometrySource = geometrySource;
		this.fragmentSource = fragmentSource;
		this.parent = parent;
		this.directives = new ProgramDirectives(this, properties, PackRenderTargetDirectives.BASELINE_SUPPORTED_RENDER_TARGETS);
	}

	public String getName() {
		return name;
	}

	public Optional<String> getComputeSource() {
		return Optional.ofNullable(computeSource);
	}

	public Optional<String> getVertexSource() {
		return Optional.ofNullable(vertexSource);
	}

	public Optional<String> getGeometrySource() {
		return Optional.ofNullable(geometrySource);
	}

	public Optional<String> getFragmentSource() {
		return Optional.ofNullable(fragmentSource);
	}

	public ProgramDirectives getDirectives() {
		return this.directives;
	}

	public ProgramSet getParent() {
		return parent;
	}

	public boolean isValid() {
		return vertexSource != null && fragmentSource != null;
	}

	public Optional<ProgramSource> requireValid() {
		if (this.isValid()) {
			return Optional.of(this);
		} else {
			return Optional.empty();
		}
	}
}
