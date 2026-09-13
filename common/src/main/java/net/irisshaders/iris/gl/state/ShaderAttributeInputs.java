package net.irisshaders.iris.gl.state;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;

import java.util.List;

import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;

public class ShaderAttributeInputs {
	private boolean ie;
	private boolean color;
	private boolean tex;
	private boolean overlay;
	private boolean light;
	private boolean normal;
	private boolean newLines;
	private boolean glint;
	private boolean text;
	private int entityComponents;
	// WARNING: adding new fields requires updating hashCode and equals methods!

	public ShaderAttributeInputs(VertexFormat format, boolean isFullbright, boolean isLines, boolean glint, boolean text, boolean ie) {
		this.ie = ie;
		this.text = text;
		this.glint = glint;

		this.newLines = isLines;

        if (format == null) {
            // Sodium. Get current.
            format = WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
        }

		format.getElements().forEach(n -> {
			var name = n.name();

			if ("Color".equals(name) || "a_Color".equals(name)) {
				color = true;
			}

			if ("LineWidth".equals(name)) {
				newLines = true;
			}

			if ("UV0".equals(name) || "a_TexCoord".equals(name)) {
				tex = true;
			}

			if ("UV1".equals(name)) {
				overlay = true;
			}

			if (("UV2".equals(name) || "a_LightAndData".equals(name)) && !isFullbright) {
				light = true;
			}

			if ("Normal".equals(name) || "iris_Normal".equals(name)) {
				normal = true;
			}
		});

		// Packs declare mc_Entity as a float, so it reads back zero and the transformer has to
		// redeclare it. Zero here means there is nothing to fix.
		List<VertexFormatElement> elements = format.getElements();
		for (int index = 0; index < elements.size(); index++) {
			VertexFormatElement element = elements.get(index);
			String name = element.name();

			if ("mc_Entity".equals(name) && (element.format().componentType() == GpuFormat.ComponentType.SINT_16 || element.format().componentType() == GpuFormat.ComponentType.SINT_32)) {
				entityComponents = element.format().componentCount();
			}
		}
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

	public int getEntityComponents() {
		return entityComponents;
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
		result = prime * result + (text ? 1231 : 1237);
		result = prime * result + entityComponents;
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
		if (text != other.text)
			return false;
		return entityComponents == other.entityComponents;
	}

	public boolean isText() {
		return text;
	}

	public boolean isIE() {
		return ie;
	}
}
