package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.irisshaders.iris.gl.IrisRenderSystem;

import java.util.Objects;
import java.util.function.IntSupplier;

public final class PartialShader {
	private final int program;
	private final int vertexS;
	private final int fragS;
	private final int geometryS;
	private final int tessContS;
	private final int tessEvalS;

	public PartialShader(int program, int vertexS, int fragS, int geometryS, int tessContS,
						 int tessEvalS) {
		this.program = program;
		this.vertexS = vertexS;
		this.fragS = fragS;
		this.geometryS = geometryS;
		this.tessContS = tessContS;
		this.tessEvalS = tessEvalS;
	}

	private boolean hasUnbound = false;

	public int getFinally() {
		if (!hasUnbound) {
			hasUnbound = true;
			detachIfValid(program, vertexS);
			detachIfValid(program, fragS);
			detachIfValid(program, geometryS);
			detachIfValid(program, tessContS);
			detachIfValid(program, tessEvalS);
		}

		return program;
	}

	private static void detachIfValid(int i, int s) {
		if (s >= 0) {
			IrisRenderSystem.detachShader(i, s);
			GlStateManager.glDeleteShader(s);
		}
	}

	public int program() {
		return program;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (PartialShader) obj;
		return this.program == that.program &&
			this.vertexS == that.vertexS &&
			this.fragS == that.fragS &&
			this.geometryS == that.geometryS &&
			this.tessContS == that.tessContS &&
			this.tessEvalS == that.tessEvalS;
	}

	@Override
	public int hashCode() {
		return Objects.hash(program, vertexS, fragS, geometryS, tessContS, tessEvalS);
	}

	@Override
	public String toString() {
		return "PartialShader[" +
			"program=" + program + ", " +
			"vertexS=" + vertexS + ", " +
			"fragS=" + fragS + ", " +
			"geometryS=" + geometryS + ", " +
			"tessContS=" + tessContS + ", " +
			"tessEvalS=" + tessEvalS + ']';
	}

}
