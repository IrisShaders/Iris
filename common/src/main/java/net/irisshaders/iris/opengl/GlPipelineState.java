package net.irisshaders.iris.opengl;

import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.irisshaders.iris.gl.blending.BlendMode;
import org.lwjgl.opengl.ARBDrawBuffersBlend;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL40C;

public final class GlPipelineState {
	private static boolean indexedBlend;

	private GlPipelineState() {
	}

	static boolean supportsIndexedBlend() {
		var capabilities = GL.getCapabilities();

		return capabilities.glBlendFuncSeparatei != 0 || capabilities.glBlendFuncSeparateiARB != 0;
	}

	static void blendFunction(int attachment, BlendMode blend) {
		var capabilities = GL.getCapabilities();

		if (capabilities.glBlendFuncSeparatei != 0) {
			GL40C.glBlendFuncSeparatei(attachment, blend.srcRgb(), blend.dstRgb(), blend.srcAlpha(), blend.dstAlpha());
			indexedBlend = true;
		} else if (capabilities.glBlendFuncSeparateiARB != 0) {
			ARBDrawBuffersBlend.glBlendFuncSeparateiARB(attachment, blend.srcRgb(), blend.dstRgb(), blend.srcAlpha(), blend.dstAlpha());
			indexedBlend = true;
		} else {
			GlStateManager._blendFuncSeparate(blend.srcRgb(), blend.dstRgb(), blend.srcAlpha(), blend.dstAlpha());
		}
	}

	public static void beforeVanillaPipeline() {
		if (indexedBlend) {
			indexedBlend = false;
			GlStateManager._blendFuncSeparate(GL11C.GL_ZERO, GL11C.GL_ZERO, GL11C.GL_ZERO, GL11C.GL_ZERO);
			GlStateManager._blendFuncSeparate(GL11C.GL_ONE, GL11C.GL_ZERO, GL11C.GL_ONE, GL11C.GL_ZERO);
			GlStateManager._blendEquationSeparate(GL14C.GL_FUNC_ADD, GL14C.GL_FUNC_ADD);
		}
	}
}
