package net.coderbot.iris.pipeline.newshader;

import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class ShaderAttributeInputs {
	private boolean color;
	private boolean tex;
	private boolean overlay;
	private boolean light;
	private boolean normal;
	private boolean newLines;

	public ShaderAttributeInputs(VertexFormat format) {
		if (format == VertexFormats.LINES) {
			newLines = true;
		}

		format.getShaderAttributes().forEach(name -> {
			if ("Color".equals(name)) {
				color = true;
			}

			if ("UV0".equals(name)) {
				tex = true;
			}

			if ("UV1".equals(name)) {
				overlay = true;
			}

			if ("UV2".equals(name)) {
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
}
