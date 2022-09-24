package net.coderbot.iris.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.ARBMultiBind;
import org.lwjgl.opengl.EXTShaderImageLoadStore;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL42C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * This class is responsible for abstracting calls to OpenGL and asserting that calls are run on the render thread.
 */
public class IrisRenderSystem {
	enum DSAState {
		CORE,
		ARB,
		NONE
	}
	private static DSAState supportsDSA;
	private static boolean hasMultibind;

	public static void initRenderer() {
		if (GL.getCapabilities().OpenGL45) {
			supportsDSA = DSAState.CORE;
		} else if (GL.getCapabilities().GL_ARB_direct_state_access) {
			supportsDSA = DSAState.ARB;
		} else {
			supportsDSA = DSAState.NONE;
		}

		if (GL.getCapabilities().OpenGL45 || GL.getCapabilities().GL_ARB_multi_bind) {
			hasMultibind = true;
		} else {
			hasMultibind = false;
		}
	}

	public static void getIntegerv(int pname, int[] params) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glGetIntegerv(pname, params);
	}

	public static void getFloatv(int pname, float[] params) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glGetFloatv(pname, params);
	}

	public static void generateMipmaps(int texture, int mipmapTarget) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (supportsDSA) {
			case ARB:
			case CORE:
				ARBDirectStateAccess.glGenerateTextureMipmap(texture);
				break;
			case NONE:
				GlStateManager._bindTexture(texture);
				GL30C.glGenerateMipmap(mipmapTarget);
				break;
		}
	}

	public static void bindAttributeLocation(int program, int index, CharSequence name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glBindAttribLocation(program, index, name);
	}

	public static void texImage2D(int texture, int target, int level, int internalformat, int width, int height, int border, int format, int type, @Nullable ByteBuffer pixels) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GlStateManager._bindTexture(texture);
		GL30C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	public static void uniformMatrix4fv(int location, boolean transpose, FloatBuffer matrix) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glUniformMatrix4fv(location, transpose, matrix);
	}

	public static void copyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glCopyTexImage2D(target, level, internalFormat, x, y, width, height, border);
	}

	public static void uniform1f(int location, float v0) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glUniform1f(location, v0);
	}

	public static void uniform1i(int location, int v0) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glUniform1i(location, v0);
	}

	public static void uniform2f(int location, float v0, float v1) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30C.glUniform2f(location, v0, v1);
	}

	public static void uniform2i(int location, int v0, int v1) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30C.glUniform2i(location, v0, v1);
	}

	public static void uniform3f(int location, float v0, float v1, float v2) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30C.glUniform3f(location, v0, v1, v2);
	}

	public static void uniform4f(int location, float v0, float v1, float v2, float v3) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30C.glUniform4f(location, v0, v1, v2, v3);
	}

	public static void uniform4i(int location, int v0, int v1, int v2, int v3) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glUniform4i(location, v0, v1, v2, v3);
	}

	public static int getAttribLocation(int programId, String name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL30C.glGetAttribLocation(programId, name);
	}

	public static int getUniformLocation(int programId, String name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL30C.glGetUniformLocation(programId, name);
	}

	public static void texParameteriv(int texture, int target, int pname, int[] params) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (supportsDSA) {
			case ARB:
			case CORE:
				ARBDirectStateAccess.glTextureParameteriv(texture, pname, params);
				break;
			case NONE:
				GlStateManager._bindTexture(texture);
				GL30C.glTexParameteriv(target, pname, params);
				break;
		}
	}

	public static void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height) {
		switch (supportsDSA) {
			case ARB:
			case CORE:
				ARBDirectStateAccess.glCopyTextureSubImage2D(destTexture, i, i1, i2, i3, i4, width, height);
				break;
			case NONE:
				int previous = GlStateManager.getActiveTextureName();
				GlStateManager._bindTexture(destTexture);
				GL30C.glCopyTexSubImage2D(target, i, i1, i2, i3, i4, width, height);
				GlStateManager._bindTexture(previous);
				break;
		}
	}

	public static void texParameteri(int texture, int target, int pname, int param) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (supportsDSA) {
			case ARB:
			case CORE:
				ARBDirectStateAccess.glTextureParameteri(texture, pname, param);
				break;
			case NONE:
				GlStateManager._bindTexture(texture);
				GL30C.glTexParameteri(target, pname, param);
				break;
		}
	}

	public static void texParameterf(int texture, int target, int pname, float param) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (supportsDSA) {
			case ARB:
			case CORE:
				ARBDirectStateAccess.glTextureParameterf(texture, pname, param);
				break;
			case NONE:
				GlStateManager._bindTexture(texture);
				GL30C.glTexParameterf(target, pname, param);
				break;
		}
	}

	public static String getProgramInfoLog(int program) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL30C.glGetProgramInfoLog(program);
	}

	public static String getShaderInfoLog(int shader) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL30C.glGetShaderInfoLog(shader);
	}

	public static void drawBuffers(int framebuffer, int[] buffers) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (supportsDSA) {
			case ARB:
			case CORE:
				ARBDirectStateAccess.glNamedFramebufferDrawBuffers(framebuffer, buffers);
				break;
			case NONE:
				GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, framebuffer);
				GL30C.glDrawBuffers(buffers);
				break;
		}
	}

	public static void readBuffer(int framebuffer, int buffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (supportsDSA) {
			case ARB:
			case CORE:
				ARBDirectStateAccess.glNamedFramebufferReadBuffer(framebuffer, buffer);
				break;
			case NONE:
				GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, framebuffer);
				GL30C.glReadBuffer(buffer);
				break;
		}
	}

	public static String getActiveUniform(int program, int index, int size, IntBuffer type, IntBuffer name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL30C.glGetActiveUniform(program, index, size, type, name);
	}

	public static void readPixels(int x, int y, int width, int height, int format, int type, float[] pixels) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glReadPixels(x, y, width, height, format, type, pixels);
	}

	public static void bufferData(int target, float[] data, int usage) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glBufferData(target, data, usage);
	}

	public static void vertexAttrib4f(int index, float v0, float v1, float v2, float v3) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glVertexAttrib4f(index, v0, v1, v2, v3);
	}

	public static void detachShader(int program, int shader) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glDetachShader(program, shader);
	}

	public static void framebufferTexture2D(int fb, int fbtarget, int format, int target, int texture, int i) {
		// TODO: A NVIDIA bug prevents this from properly working... pain.
		/*switch (supportsDSA) {
			case ARB:
			case CORE:
				ARBDirectStateAccess.glNamedFramebufferTexture(fb, target, texture, i);
				break;
			case NONE:
				GlStateManager._glBindFramebuffer(fbtarget, fb);
				GL30C.glFramebufferTexture2D(fbtarget, format, target, texture, i);
				break;
		}*/

		GlStateManager._glBindFramebuffer(fbtarget, fb);
		GL30C.glFramebufferTexture2D(fbtarget, format, target, texture, i);
	}

	public static int getTexParameteri(int texture, int target, int pname) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (supportsDSA) {
			case ARB:
			case CORE:
				return ARBDirectStateAccess.glGetTextureParameteri(texture, pname);
			case NONE:
				GlStateManager._bindTexture(texture);
				return GL30C.glGetTexParameteri(target, pname);
		}

		throw new IllegalStateException("Unreachable");
	}

	public static void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		if (GL.getCapabilities().OpenGL42) {
			GL42C.glBindImageTexture(unit, texture, level, layered, layer, access, format);
		} else {
			EXTShaderImageLoadStore.glBindImageTextureEXT(unit, texture, level, layered, layer, access, format);
		}
	}

	public static int getMaxImageUnits() {
		if (GL.getCapabilities().OpenGL42) {
			return GlStateManager._getInteger(GL42C.GL_MAX_IMAGE_UNITS);
		} else if (GL.getCapabilities().GL_EXT_shader_image_load_store) {
			return GlStateManager._getInteger(EXTShaderImageLoadStore.GL_MAX_IMAGE_UNITS_EXT);
		} else {
			return 0;
		}
	}

	public static void bindTextures(int startingTexture, int[] bindings) {
		if (hasMultibind) {
			ARBMultiBind.glBindTextures(startingTexture, bindings);
		} else if (supportsDSA != DSAState.NONE) {
			for (int binding : bindings) {
				ARBDirectStateAccess.glBindTextureUnit(startingTexture, binding);
				startingTexture++;
			}
		} else {
			for (int binding : bindings) {
				GlStateManager._activeTexture(startingTexture);
				GlStateManager._bindTexture(binding);
				startingTexture++;
			}
		}
	}

	public static void bindTextureToUnit(int unit, int texture) {
		if (supportsDSA != DSAState.NONE) {
			ARBDirectStateAccess.glBindTextureUnit(unit, texture);
		} else {
			GlStateManager._activeTexture(unit);
			GlStateManager._bindTexture(texture);
		}
	}

	// These functions are deprecated and unavailable in the core profile.

	@Deprecated
	public static void setupProjectionMatrix(float[] matrix) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		GL20.glLoadMatrixf(matrix);
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
	}

	@Deprecated
	public static void restoreProjectionMatrix() {
		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
	}
}
