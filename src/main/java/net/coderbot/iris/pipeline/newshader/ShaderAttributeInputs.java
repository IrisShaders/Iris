package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

public class ShaderAttributeInputs {
	private boolean color;
	private boolean tex;
	private boolean overlay;
	private boolean light;
	private boolean normal;
	private boolean newLines;

	public ShaderAttributeInputs(VertexFormat format, boolean isFullbright) {
		if (format == DefaultVertexFormat.POSITION_COLOR_NORMAL) {
			newLines = true;
		}

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
}
