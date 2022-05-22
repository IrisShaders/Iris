package net.coderbot.iris.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.EXTShaderImageLoadStore;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL42C;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * This class is responsible for abstracting calls to OpenGL and asserting that calls are run on the render thread.
 */
public class IrisRenderSystem {
	private static Matrix4f backupProjection;

	public static void getIntegerv(int pname, int[] params) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glGetIntegerv(pname, params);
	}

	public static void getFloatv(int pname, float[] params) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glGetFloatv(pname, params);
	}

	public static void generateMipmaps(int mipmapTarget) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glGenerateMipmap(mipmapTarget);
	}

	public static void bindAttributeLocation(int program, int index, CharSequence name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glBindAttribLocation(program, index, name);
	}

	public static void texImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, @Nullable ByteBuffer pixels) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	public static void uniform1f(int location, float v0) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glUniform1f(location, v0);
	}

	public static void uniform2f(int location, float v0, float v1) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glUniform2f(location, v0, v1);
	}

	public static void uniform2i(int location, int v0, int v1) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glUniform2i(location, v0, v1);
	}

	public static void uniform3f(int location, float v0, float v1, float v2) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glUniform3f(location, v0, v1, v2);
	}

	public static void uniform4f(int location, float v0, float v1, float v2, float v3) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glUniform4f(location, v0, v1, v2, v3);
	}

	public static void uniform4i(int location, int v0, int v1, int v2, int v3) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glUniform4i(location, v0, v1, v2, v3);
	}

	public static void texParameteriv(int target, int pname, int[] params) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glTexParameteriv(target, pname, params);
	}

	public static String getProgramInfoLog(int program) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL32C.glGetProgramInfoLog(program);
	}

	public static String getShaderInfoLog(int shader) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL32C.glGetShaderInfoLog(shader);
	}

	public static void drawBuffers(int[] buffers) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glDrawBuffers(buffers);
	}

	public static void readBuffer(int buffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glReadBuffer(buffer);
	}

	public static String getActiveUniform(int program, int index, int size, IntBuffer type, IntBuffer name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL32C.glGetActiveUniform(program, index, size, type, name);
	}

	public static void readPixels(int x, int y, int width, int height, int format, int type, float[] pixels) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glReadPixels(x, y, width, height, format, type, pixels);
	}

	public static void bufferData(int target, float[] data, int usage) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glBufferData(target, data, usage);
	}

	public static void vertexAttrib4f(int index, float v0, float v1, float v2, float v3) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glVertexAttrib4f(index, v0, v1, v2, v3);
	}

	public static void detachShader(int program, int shader) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glDetachShader(program, shader);
	}

	public static int getTexParameteri(int target, int pname) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL32C.glGetTexParameteri(target, pname);
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

	public static String getStringi(int glExtensions, int index) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL32C.glGetStringi(glExtensions, index);
	}

	public static int getUniformBlockIndex(int program, String uniformBlockName) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL32C.glGetUniformBlockIndex(program, uniformBlockName);
	}

	public static void uniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL32C.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

	public static void setShadowProjection(Matrix4f shadowProjection) {
		backupProjection = RenderSystem.getProjectionMatrix();
		RenderSystem.setProjectionMatrix(shadowProjection);
	}

	public static void restorePlayerProjection() {
		RenderSystem.setProjectionMatrix(backupProjection);
		backupProjection = null;
	}
}
