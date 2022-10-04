package net.coderbot.iris.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.vendored.joml.Vector3i;
import net.coderbot.iris.Iris;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.EXTShaderImageLoadStore;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * This class is responsible for abstracting calls to OpenGL and asserting that calls are run on the render thread.
 */
public class IrisRenderSystem {
	private static DSAAccess dsaState;
	private static boolean hasMultibind;
	private static boolean supportsCompute;

	public static void initRenderer() {
		if (GL.getCapabilities().OpenGL45) {
			dsaState = new DSACore();
			Iris.logger.info("OpenGL 4.5 detected, enabling DSA.");
		} else if (GL.getCapabilities().GL_ARB_direct_state_access) {
			dsaState = new DSAARB();
			Iris.logger.info("ARB_direct_state_access detected, enabling DSA.");
		} else {
			dsaState = new DSAUnsupported();
			Iris.logger.info("DSA support not detected.");
		}

		if (GL.getCapabilities().OpenGL45 || GL.getCapabilities().GL_ARB_multi_bind) {
			hasMultibind = true;
		} else {
			hasMultibind = false;
		}

		supportsCompute = supportsCompute();
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
		dsaState.generateMipmaps(texture, mipmapTarget);
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
		dsaState.texParameteriv(texture, target, pname, params);
	}

	public static void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height) {
		dsaState.copyTexSubImage2D(destTexture, target, i, i1, i2, i3, i4, width, height);
	}

	public static void texParameteri(int texture, int target, int pname, int param) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		dsaState.texParameteri(texture, target, pname, param);
	}

	public static void texParameterf(int texture, int target, int pname, float param) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		dsaState.texParameterf(texture, target, pname, param);
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
		dsaState.drawBuffers(framebuffer, buffers);
	}

	public static void readBuffer(int framebuffer, int buffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		dsaState.readBuffer(framebuffer, buffer);
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

	public static int bufferStorage(int target, float[] data, int usage) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return dsaState.bufferStorage(target, data, usage);
	}

	public static void vertexAttrib4f(int index, float v0, float v1, float v2, float v3) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glVertexAttrib4f(index, v0, v1, v2, v3);
	}

	public static void detachShader(int program, int shader) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glDetachShader(program, shader);
	}

	public static void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels) {
		dsaState.framebufferTexture2D(fb, fbtarget, attachment, target, texture, levels);
	}

	public static int getTexParameteri(int texture, int target, int pname) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		return dsaState.getTexParameteri(texture, target, pname);
	}

	public static void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		if (GL.getCapabilities().OpenGL42) {
			GL45C.glBindImageTexture(unit, texture, level, layered, layer, access, format);
		} else {
			EXTShaderImageLoadStore.glBindImageTextureEXT(unit, texture, level, layered, layer, access, format);
		}
	}

	public static int getMaxImageUnits() {
		if (GL.getCapabilities().OpenGL42) {
			return GlStateManager._getInteger(GL45C.GL_MAX_IMAGE_UNITS);
		} else if (GL.getCapabilities().GL_EXT_shader_image_load_store) {
			return GlStateManager._getInteger(EXTShaderImageLoadStore.GL_MAX_IMAGE_UNITS_EXT);
		} else {
			return 0;
		}
	}

	public static void getProgramiv(int program, int value, int[] storage) {
		GL30C.glGetProgramiv(program, value, storage);
	}

	public static void dispatchCompute(int workX, int workY, int workZ) {
		GL45C.glDispatchCompute(workX, workY, workZ);
	}

	public static void dispatchCompute(Vector3i workGroups) {
		GL45C.glDispatchCompute(workGroups.x, workGroups.y, workGroups.z);
	}

	public static void memoryBarrier(int barriers) {
		if (supportsCompute) {
			GL45C.glMemoryBarrier(barriers);
		}
	}

	public static boolean supportsBufferBlending() {
		return GL.getCapabilities().GL_ARB_draw_buffers_blend || GL.getCapabilities().OpenGL40;
	}

	public static void disableBufferBlend(int buffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glDisablei(GL30C.GL_BLEND, buffer);
	}

	public static void enableBufferBlend(int buffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glEnablei(GL30C.GL_BLEND, buffer);
	}

	public static void blendFuncSeparatei(int buffer, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL40C.glBlendFuncSeparatei(buffer, srcRGB, dstRGB, srcAlpha, dstAlpha);
  }
  
	public static void bindTextureToUnit(int unit, int texture) {
		dsaState.bindTextureToUnit(unit, texture);
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

	public static void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter) {
		dsaState.blitFramebuffer(source, dest, offsetX, offsetY, width, height, offsetX2, offsetY2, width2, height2, bufferChoice, filter);
	}

	public static int createFramebuffer() {
		return dsaState.createFramebuffer();
	}

	public static int createTexture(int target) {
		return dsaState.createTexture(target);
	}

	public interface DSAAccess {
		void generateMipmaps(int texture, int target);

		void texParameteri(int texture, int target, int pname, int param);
		void texParameterf(int texture, int target, int pname, float param);
		void texParameteriv(int texture, int target, int pname, int[] params);

		void readBuffer(int framebuffer, int buffer);

		void drawBuffers(int framebuffer, int[] buffers);

		int getTexParameteri(int texture, int target, int pname);

		void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height);

		void bindTextureToUnit(int unit, int texture);

		int bufferStorage(int target, float[] data, int usage);

		void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter);

		void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels);

		int createFramebuffer();
		int createTexture(int target);
	}

	public static class DSACore extends DSAARB {

	}

	public static class DSAARB extends DSAUnsupported {

		@Override
		public void generateMipmaps(int texture, int target) {
			ARBDirectStateAccess.glGenerateTextureMipmap(texture);
		}

		@Override
		public void texParameteri(int texture, int target, int pname, int param) {
			ARBDirectStateAccess.glTextureParameteri(texture, pname, param);
		}

		@Override
		public void texParameterf(int texture, int target, int pname, float param) {
			ARBDirectStateAccess.glTextureParameterf(texture, pname, param);
		}

		@Override
		public void texParameteriv(int texture, int target, int pname, int[] params) {
			ARBDirectStateAccess.glTextureParameteriv(texture, pname, params);
		}

		@Override
		public void readBuffer(int framebuffer, int buffer) {
			ARBDirectStateAccess.glNamedFramebufferReadBuffer(framebuffer, buffer);
		}

		@Override
		public void drawBuffers(int framebuffer, int[] buffers) {
			ARBDirectStateAccess.glNamedFramebufferDrawBuffers(framebuffer, buffers);
		}

		@Override
		public int getTexParameteri(int texture, int target, int pname) {
			return ARBDirectStateAccess.glGetTextureParameteri(texture, pname);
		}

		@Override
		public void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height) {
			ARBDirectStateAccess.glCopyTextureSubImage2D(destTexture, i, i1, i2, i3, i4, width, height);
		}

		@Override
		public void bindTextureToUnit(int unit, int texture) {
			if (texture == 0) {
				super.bindTextureToUnit(unit, texture);
			} else {
				ARBDirectStateAccess.glBindTextureUnit(unit, texture);
			}
		}

		@Override
		public int bufferStorage(int target, float[] data, int usage) {
			int buffer = GL45C.glCreateBuffers();
			GL45C.glNamedBufferData(buffer, data, usage);
			return buffer;
		}

		@Override
		public void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter) {
			ARBDirectStateAccess.glBlitNamedFramebuffer(source, dest, offsetX, offsetY, width, height, offsetX2, offsetY2, width2, height2, bufferChoice, filter);
		}

		@Override
		public void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels) {
			ARBDirectStateAccess.glNamedFramebufferTexture(fb, attachment, texture, levels);
		}

		@Override
		public int createFramebuffer() {
			return ARBDirectStateAccess.glCreateFramebuffers();
		}

		@Override
		public int createTexture(int target) {
			return ARBDirectStateAccess.glCreateTextures(target);
		}
	}

	public static class DSAUnsupported implements DSAAccess {
		@Override
		public void generateMipmaps(int texture, int target) {
			GlStateManager._bindTexture(texture);
			GL30C.glGenerateMipmap(target);
		}

		@Override
		public void texParameteri(int texture, int target, int pname, int param) {
			GlStateManager._bindTexture(texture);
			GL30C.glTexParameteri(target, pname, param);
		}

		@Override
		public void texParameterf(int texture, int target, int pname, float param) {
			GlStateManager._bindTexture(texture);
			GL30C.glTexParameterf(target, pname, param);
		}

		@Override
		public void texParameteriv(int texture, int target, int pname, int[] params) {
			GlStateManager._bindTexture(texture);
			GL30C.glTexParameteriv(target, pname, params);
		}

		@Override
		public void readBuffer(int framebuffer, int buffer) {
			GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, framebuffer);
			GL30C.glReadBuffer(buffer);
		}

		@Override
		public void drawBuffers(int framebuffer, int[] buffers) {
			GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, framebuffer);
			GL30C.glDrawBuffers(buffers);
		}

		@Override
		public int getTexParameteri(int texture, int target, int pname) {
			GlStateManager._bindTexture(texture);
			return GL30C.glGetTexParameteri(target, pname);
		}

		@Override
		public void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height) {
			int previous = GlStateManager.getActiveTextureName();
			GlStateManager._bindTexture(destTexture);
			GL30C.glCopyTexSubImage2D(target, i, i1, i2, i3, i4, width, height);
			GlStateManager._bindTexture(previous);
		}

		@Override
		public void bindTextureToUnit(int unit, int texture) {
			GlStateManager._activeTexture(GL30C.GL_TEXTURE0 + unit);
			GlStateManager._bindTexture(texture);
		}

		@Override
		public int bufferStorage(int target, float[] data, int usage) {
			int buffer = GlStateManager._glGenBuffers();
			GlStateManager._glBindBuffer(target, buffer);
			bufferData(target, data, usage);
			GlStateManager._glBindBuffer(target, 0);

			return buffer;
		}

		@Override
		public void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter) {
			GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, source);
			GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, dest);
			GL30C.glBlitFramebuffer(offsetX, offsetY, width, height, offsetX2, offsetY2, width2, height2, bufferChoice, filter);
		}

		@Override
		public void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels) {
			GlStateManager._glBindFramebuffer(fbtarget, fb);
			GL30C.glFramebufferTexture2D(fbtarget, attachment, target, texture, levels);
		}

		@Override
		public int createFramebuffer() {
			int framebuffer = GlStateManager.glGenFramebuffers();
			GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, framebuffer);
			return framebuffer;
		}

		@Override
		public int createTexture(int target) {
			int texture = GlStateManager._genTexture();
			GlStateManager._bindTexture(texture);
			return texture;
		}
	}

	/*
	public static void bindTextures(int startingTexture, int[] bindings) {
		if (hasMultibind) {
			ARBMultiBind.glBindTextures(startingTexture, bindings);
		} else if (dsaState != DSAState.NONE) {
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
	 */

	// TODO: Proper notification of compute support
	public static boolean supportsCompute() {
		return GL.getCapabilities().glDispatchCompute != MemoryUtil.NULL;
	}
}
