package net.coderbot.iris.shaderpack;

import net.coderbot.iris.gl.blending.BlendModeOverride;

import java.util.Optional;

public class ProgramSource {
	private final String name;
	private final String vertexSource;
	private final String geometrySource;
	private final String fragmentSource;
	private final ProgramDirectives directives;
	private final ProgramSet parent;

	private ProgramSource(String name, String vertexSource, String geometrySource, String fragmentSource,
						 ProgramDirectives directives, ProgramSet parent) {
		this.name = name;
		this.vertexSource = vertexSource;
		this.geometrySource = geometrySource;
		this.fragmentSource = fragmentSource;
		this.directives = directives;
		this.parent = parent;
	}

	public ProgramSource(String name, String vertexSource, String geometrySource, String fragmentSource,
						 ProgramSet parent, ShaderProperties properties, BlendModeOverride defaultBlendModeOverride) {
		this.name = name;
		this.vertexSource = vertexSource;
		this.geometrySource = geometrySource;
		this.fragmentSource = fragmentSource;
		this.parent = parent;
		this.directives = new ProgramDirectives(this, properties,
				PackRenderTargetDirectives.BASELINE_SUPPORTED_RENDER_TARGETS, defaultBlendModeOverride);
	}

	public ProgramSource withDirectiveOverride(ProgramDirectives overrideDirectives) {
		return new ProgramSource(name, vertexSource, geometrySource, fragmentSource, overrideDirectives, parent);
	}

	public String getName() {
		return name;
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
