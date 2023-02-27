package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;

public class ShaderAttributeInputs {
	private boolean color;
	private boolean tex;
	private boolean overlay;
	private boolean light;
	private boolean normal;
	private boolean newLines;
	private boolean glint;
	// WARNING: adding new fields requires updating hashCode and equals methods!

	public ShaderAttributeInputs(VertexFormat format, boolean isFullbright, boolean glint) {
		if (format == DefaultVertexFormat.POSITION_COLOR_NORMAL) {
			newLines = true;
		}

		this.glint = glint;

		format.getElementAttributeNames().forEach(name -> {
			if ("Color".equals(name)) {
				color = true;
			}

			if ("UV0".equals(name)) {
				tex = true;
			}

			if ("UV1".equals(name)) {
				overlay = true;
			}

			if ("UV2".equals(name) && !isFullbright) {
				light = true;
			}

			if ("Normal".equals(name)) {
				normal = true;
			}
		});
	}

	public ShaderAttributeInputs(boolean color, boolean tex, boolean overlay, boolean light, boolean normal) {
		this.color = color;
		this.tex = tex;
		this.overlay = overlay;
		this.light = light;
		this.normal = normal;
	}

	public boolean hasColor() {
		return color;
	}

	public boolean hasTex() {
		return tex;
	}

	public boolean hasOverlay() {
		return overlay;
	}

	public boolean hasLight() {
		return light;
	}

	public boolean hasNormal() {
		return normal;
	}

	public boolean isNewLines() {
		return newLines;
	}

	public boolean isGlint() {
		return glint;
	}

	public InputAvailability toAvailability() {
		return new InputAvailability(tex, light, overlay);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (color ? 1231 : 1237);
		result = prime * result + (tex ? 1231 : 1237);
		result = prime * result + (overlay ? 1231 : 1237);
		result = prime * result + (light ? 1231 : 1237);
		result = prime * result + (normal ? 1231 : 1237);
		result = prime * result + (newLines ? 1231 : 1237);
		result = prime * result + (glint ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShaderAttributeInputs other = (ShaderAttributeInputs) obj;
		if (color != other.color)
			return false;
		if (tex != other.tex)
			return false;
		if (overlay != other.overlay)
			return false;
		if (light != other.light)
			return false;
		if (normal != other.normal)
			return false;
		if (newLines != other.newLines)
			return false;
		if (glint != other.glint)
			return false;
		return true;
	}
}
