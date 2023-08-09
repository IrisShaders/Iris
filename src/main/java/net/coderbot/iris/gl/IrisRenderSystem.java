package net.coderbot.iris.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.coderbot.iris.gl.sampler.SamplerLimits;
import org.joml.Matrix4f;
import org.joml.Vector3i;
import org.lwjgl.opengl.ARBDrawBuffersBlend;
import org.lwjgl.opengl.EXTShaderImageLoadStore;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.opengl.NVXGPUMemoryInfo;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * This class is responsible for abstracting calls to OpenGL and asserting that calls are run on the render thread.
 */
public class IrisRenderSystem {
	private static Matrix4f backupProjection;
	private static int[] samplers;

	public static void initRenderer() {
		samplers = new int[SamplerLimits.get().getMaxTextureUnits()];
	}

	public static void getIntegerv(int pname, int[] params) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glGetIntegerv(pname, params);
	}

	public static void getFloatv(int pname, float[] params) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glGetFloatv(pname, params);
	}

	public static void generateMipmaps(int texture) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL46C.glGenerateTextureMipmap(texture);
	}

	public static void bindAttributeLocation(int program, int index, CharSequence name) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glBindAttribLocation(program, index, name);
	}
	public static void uniformMatrix4fv(int location, boolean transpose, FloatBuffer matrix) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniformMatrix4fv(location, transpose, matrix);
	}

	public static void uniform1f(int location, float v0) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform1f(location, v0);
	}

	public static void uniform2f(int location, float v0, float v1) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform2f(location, v0, v1);
	}

	public static void uniform2i(int location, int v0, int v1) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform2i(location, v0, v1);
	}

	public static void uniform3f(int location, float v0, float v1, float v2) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform3f(location, v0, v1, v2);
	}

	public static void uniform4f(int location, float v0, float v1, float v2, float v3) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform4f(location, v0, v1, v2, v3);
	}

	public static void uniform4i(int location, int v0, int v1, int v2, int v3) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform4i(location, v0, v1, v2, v3);
	}

	public static void texParameteriv(int texture, int pname, int[] params) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL46C.glTextureParameteriv(texture, pname, params);
	}

	public static void texParameteri(int texture, int pname, int param) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL46C.glTextureParameteri(texture, pname, param);
	}

	public static void texParameterf(int texture, int pname, float param) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL46C.glTextureParameterf(texture, pname, param);
	}

	public static String getProgramInfoLog(int program) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL32C.glGetProgramInfoLog(program);
	}

	public static String getShaderInfoLog(int shader) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL32C.glGetShaderInfoLog(shader);
	}

	public static void drawBuffers(int framebuffer, int[] buffers) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL46C.glNamedFramebufferDrawBuffers(framebuffer, buffers);
	}

	public static void readBuffer(int framebuffer, int buffer) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL46C.glNamedFramebufferReadBuffer(framebuffer, buffer);
	}

	public static String getActiveUniform(int program, int index, int size, IntBuffer type, IntBuffer name) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL32C.glGetActiveUniform(program, index, size, type, name);
	}

	public static void readPixels(int x, int y, int width, int height, int format, int type, float[] pixels) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glReadPixels(x, y, width, height, format, type, pixels);
	}

	public static void bufferData(int target, float[] data, int usage) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glBufferData(target, data, usage);
	}
	public static void bufferStorage(int texture, long size, int flags) {
		RenderSystem.assertOnRenderThreadOrInit();
		// The ARB version is identical to GL44 and redirects, so this should work on ARB as well.
		GL45C.glNamedBufferStorage(texture, size, flags);
	}

	public static void bindBufferBase(int target, Integer index, int buffer) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL43C.glBindBufferBase(target, index, buffer);
	}

	public static void vertexAttrib4f(int index, float v0, float v1, float v2, float v3) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glVertexAttrib4f(index, v0, v1, v2, v3);
	}

	public static void detachShader(int program, int shader) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glDetachShader(program, shader);
	}

	public static void framebufferTexture2D(int fb, int attachment, int texture, int levels) {
		GL46C.glNamedFramebufferTexture(fb, attachment, texture, levels);
	}

	public static int getTexParameteri(int texture, int pname) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL46C.glGetTextureParameteri(texture, pname);
	}

	public static void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
		RenderSystem.assertOnRenderThreadOrInit();
		if (GL.getCapabilities().OpenGL42 || GL.getCapabilities().GL_ARB_shader_image_load_store) {
			GL42C.glBindImageTexture(unit, texture, level, layered, layer, access, format);
		} else {
			EXTShaderImageLoadStore.glBindImageTextureEXT(unit, texture, level, layered, layer, access, format);
		}
	}

	public static int getMaxImageUnits() {
		if (GL.getCapabilities().OpenGL42 || GL.getCapabilities().GL_ARB_shader_image_load_store) {
			return GlStateManager._getInteger(GL42C.GL_MAX_IMAGE_UNITS);
		} else if (GL.getCapabilities().GL_EXT_shader_image_load_store) {
			return GlStateManager._getInteger(EXTShaderImageLoadStore.GL_MAX_IMAGE_UNITS_EXT);
		} else {
			return 0;
		}
	}

	public static boolean supportsSSBO() {
		return GL.getCapabilities().OpenGL44 || (GL.getCapabilities().GL_ARB_shader_storage_buffer_object && GL.getCapabilities().GL_ARB_buffer_storage);
	}

	public static boolean supportsImageLoadStore() {
		return GL.getCapabilities().glBindImageTexture != 0L || GL.getCapabilities().OpenGL42 || ((GL.getCapabilities().GL_ARB_shader_image_load_store || GL.getCapabilities().GL_EXT_shader_image_load_store) && GL.getCapabilities().GL_ARB_buffer_storage);
	}

	public static void genBuffers(int[] buffers) {
		GL43C.glGenBuffers(buffers);
	}

	public static void clearBufferSubData(int buffer, int glR8, long offset, long size, int glRed, int glByte, int[] ints) {
		GL46C.glClearNamedBufferSubData(buffer, glR8, offset, size, glRed, glByte, ints);
	}

	public static void getProgramiv(int program, int value, int[] storage) {
		GL32C.glGetProgramiv(program, value, storage);
	}

	public static void dispatchCompute(int workX, int workY, int workZ) {
		GL45C.glDispatchCompute(workX, workY, workZ);
	}

	public static void dispatchCompute(Vector3i workGroups) {
		GL45C.glDispatchCompute(workGroups.x, workGroups.y, workGroups.z);
	}

	public static void memoryBarrier(int barriers) {
		RenderSystem.assertOnRenderThreadOrInit();

		GL45C.glMemoryBarrier(barriers);
	}

	public static boolean supportsBufferBlending() {
		return GL.getCapabilities().GL_ARB_draw_buffers_blend || GL.getCapabilities().OpenGL40;
	}

	public static void disableBufferBlend(int buffer) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glDisablei(GL32C.GL_BLEND, buffer);
	}

	public static void enableBufferBlend(int buffer) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glEnablei(GL32C.GL_BLEND, buffer);
	}

	public static void blendFuncSeparatei(int buffer, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		RenderSystem.assertOnRenderThreadOrInit();
		ARBDrawBuffersBlend.glBlendFuncSeparateiARB(buffer, srcRGB, dstRGB, srcAlpha, dstAlpha);
  }

	public static void bindTextureToUnit(int unit, int texture) {
		GL46C.glBindTextureUnit(unit, texture);
	}

	// These functions are deprecated and unavailable in the core profile.

	public static int getUniformBlockIndex(int program, String uniformBlockName) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL32C.glGetUniformBlockIndex(program, uniformBlockName);
	}

	public static void uniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

	public static void setShadowProjection(Matrix4f shadowProjection) {
		backupProjection = RenderSystem.getProjectionMatrix();
		RenderSystem.setProjectionMatrix(shadowProjection, VertexSorting.ORTHOGRAPHIC_Z);
	}

	public static void restorePlayerProjection() {
		RenderSystem.setProjectionMatrix(backupProjection, VertexSorting.DISTANCE_TO_ORIGIN);
		backupProjection = null;
	}

	public static void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter) {
		GL46C.glBlitNamedFramebuffer(source, dest, offsetX, offsetY, width, height, offsetX2, offsetY2, width2, height2, bufferChoice, filter);
	}

	public static int createFramebuffer() {
		return GL46C.glCreateFramebuffers();
	}

	public static int createTexture(int target) {
		return GL46C.glCreateTextures(target);
	}

	public static void bindTextureForSetup(int glType, int glId) {
		GL30C.glBindTexture(glType, glId);
	}

	public static boolean supportsCompute() {
		return true;
	}

    public static int genSampler() {
		return GL33C.glGenSamplers();
    }

	public static void destroySampler(int glId) {
		GL33C.glDeleteSamplers(glId);
	}

	public static void bindSamplerToUnit(int unit, int sampler) {
		if (samplers[unit] == sampler) {
			return;
		}

		GL33C.glBindSampler(unit, sampler);

		samplers[unit] = sampler;
	}

	private static final int[] emptyArray = new int[SamplerLimits.get().getMaxTextureUnits()];

	public static void unbindAllSamplers() {
		boolean usedASampler = false;
		for (int i = 0; i < samplers.length; i++) {
			if (samplers[i] != 0) {
				usedASampler = true;
				samplers[i] = 0;
			}
		}
		if (usedASampler) {
			GL45C.glBindSamplers(0, emptyArray);
		}
	}


	public static void samplerParameteri(int sampler, int pname, int param) {
		GL33C.glSamplerParameteri(sampler, pname, param);
	}

	public static void samplerParameterf(int sampler, int pname, float param) {
		GL33C.glSamplerParameterf(sampler, pname, param);
	}

	public static void samplerParameteriv(int sampler, int pname, int[] params) {
		GL33C.glSamplerParameteriv(sampler, pname, params);
	}

	public static long getVRAM() {
		if (GL.getCapabilities().GL_NVX_gpu_memory_info) {
			return GL32C.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX) * 1024L;
		} else {
			return 4294967296L;
		}
	}

    public static void deleteBuffers(int glId) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL43C.glDeleteBuffers(glId);
    }
}
