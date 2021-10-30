package net.coderbot.iris.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * This class is responsible for abstracting calls to OpenGL and asserting that calls are run on the render thread,
 */
public class IrisRenderSystem {
	public static void generateMipmaps(int mipmapTarget) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glGenerateMipmap(mipmapTarget);
	}

	public static void bindAttributeLocation(int program, int index, ByteBuffer name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glBindAttribLocation(program, index, name);
	}

	public static void bindAttributeLocation(int program, int index, CharSequence name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glBindAttribLocation(program, index, name);
	}

	public static void bindFragDataLocation(int program, int index, ByteBuffer name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glBindFragDataLocation(program, index, name);
	}

	public static void texImage2D(int i, int j, int k, int l, int m, int n, int o, int p, @Nullable ByteBuffer byteBuffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glTexImage2D(i, j, k, l, m, n, o, p, byteBuffer);
	}

	public static void uniformMatrix4fv(int location, boolean transpose, FloatBuffer matrix) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glUniformMatrix4fv(location, transpose, matrix);
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

	public static int getAttribLocation(int programId, String name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL30C.glGetAttribLocation(programId, name);
	}

	public static int getUniformLocation(int programId, String name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL30C.glGetUniformLocation(programId, name);
	}

	public static void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
	}

	public static void copyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glCopyTexImage2D(target, level, internalFormat, x, y, width, height, border);
	}

	public static String getProgramInfoLog(int program) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL30C.glGetProgramInfoLog(program);
	}

	public static String getShaderInfoLog(int shader) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return GL30C.glGetShaderInfoLog(shader);
	}

	public static void drawBuffers(int[] buffers) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glDrawBuffers(buffers);
	}

	public static void readBuffer(int buffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glReadBuffer(buffer);
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

	// These functions are deprecated and unavailable in OpenGL 3 core

	@Deprecated
	public static void loadMatrixf(float[] matrix) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL21.glLoadMatrixf(matrix);
	}
}
