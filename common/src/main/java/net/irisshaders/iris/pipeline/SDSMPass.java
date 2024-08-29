package net.irisshaders.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.program.ProgramBuilder;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SDSMPass {
	private ComputeProgram sdsmInitial;
	private ComputeProgram sdsmFinal;
	private int index = 0;
	private int readIndex = 2;

	// Computes a compute shader dispatch size given a thread group size, and number of elements to process
	private static int DispatchSize(int tgSize, int numElements)
	{
		int dispatchSize = numElements / tgSize;
		dispatchSize += numElements % tgSize > 0 ? 1 : 0;

		return dispatchSize;
	}

	private float nearPlane = 0.0f, farPlane = 0.0f;


	public SDSMPass() {
		calculateInitial(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());

		ProgramBuilder initial = buildProgram("SDSM_initial", "/SDSMInitial.comp");
		initial.uniform1f("nearPlane", () -> nearPlane, (a) -> {});
		initial.uniform1f("farPlane", () -> farPlane, (a) -> {});
		initial.uniformMatrix("projection", CapturedRenderingState.INSTANCE::getGbufferProjection, (a) -> {});
		initial.addExternalSampler(0, "depthTex");
		sdsmInitial = initial.buildCompute();

		ProgramBuilder finalPass = buildProgram("SDSM_finalPass", "/SDSMFinal.comp");
		finalPass.addExternalSampler(0, "reductionTex");
		this.sdsmFinal = finalPass.buildCompute();
	}

	public static int SDSM_THREAD_GROUP_SIZE = 16;
	public static int MAX_READBACK_LATENCY = 3;

	private final List<TextureImage> images = new ArrayList<>();
	private final int[] pbos = new int[MAX_READBACK_LATENCY];
	private final long[] pboMap = new long[MAX_READBACK_LATENCY];



	public void calculateInitial(int windowWidth, int windowHeight) {
		images.forEach(TextureImage::destroy);
		images.clear();

		// setup UAVs for compute shader
		int width = windowWidth;
		int height = windowHeight;
		while (width > 1 || height > 1)
		{
			width = DispatchSize(SDSM_THREAD_GROUP_SIZE, width);
			height = DispatchSize(SDSM_THREAD_GROUP_SIZE, height);

			images.add(new TextureImage(width, height, InternalTextureFormat.RG16));
		}

		for (int i = 0; i < MAX_READBACK_LATENCY; i++) {
			pbos[i] = GL46C.glGenBuffers();
			GL46C.glNamedBufferStorage(pbos[i], 8, GL46C.GL_MAP_PERSISTENT_BIT | GL46C.GL_MAP_READ_BIT);
			pboMap[i] = GL46C.nglMapNamedBufferRange(pbos[i], 0, 8, GL46C.GL_MAP_READ_BIT | GL46C.GL_MAP_PERSISTENT_BIT);
		}
	}

	public void dispatch(int depthTex, float zNear, float zFar) {
		float minDepth = MemoryUtil.memGetFloat(pboMap[readIndex]);
		float maxDepth = MemoryUtil.memGetFloat(pboMap[readIndex] + 4);
		System.out.println("Last frame: " + minDepth + " " + maxDepth);
		this.nearPlane = zNear;
		this.farPlane = zFar;

		GlStateManager._activeTexture(GL46C.GL_TEXTURE0);

		TextureImage initialRTV = images.getFirst();
		GlStateManager._bindTexture(depthTex);
		IrisRenderSystem.bindImageTexture(0, initialRTV.getId(), 0, true, 0, GL42C.GL_WRITE_ONLY, initialRTV.getFormat());
		sdsmInitial.use();
		IrisRenderSystem.dispatchCompute(initialRTV.getWidth(), initialRTV.getHeight(), 1);

		// Subsequent passes
		sdsmFinal.use();
		for (int i = 1; i < images.size(); i++) {
			TextureImage prevRTV = images.get(i - 1);
			TextureImage rtv = images.get(i);
			// Previous RTV must be set as texture source, and current RTV as image2D
			GlStateManager._bindTexture(prevRTV.id);
			IrisRenderSystem.bindImageTexture(0, rtv.getId(), 0, true, 0, GL42C.GL_WRITE_ONLY, rtv.getFormat());

			IrisRenderSystem.dispatchCompute(rtv.getWidth(), rtv.getHeight(), 1);

		}

		GL46C.glBindBuffer(GL46C.GL_PIXEL_PACK_BUFFER, pbos[index]);
		GL46C.nglGetTextureImage(images.getLast().getId(), 0, GL46C.GL_RG, GL46C.GL_FLOAT, 8, 0);

		index = (index + 1) % MAX_READBACK_LATENCY;
		readIndex = (readIndex + 1) % MAX_READBACK_LATENCY;
		GL46C.glBindBuffer(GL46C.GL_PIXEL_PACK_BUFFER, 0);
		Minecraft.getInstance().getMainRenderTarget().unbindRead();
		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

		ComputeProgram.unbind();
	}

	private ProgramBuilder buildProgram(String name, String s) {
		String source;
		try {
			source = new String(IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream(s))), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		source = JcppProcessor.glslPreprocessSource(source, ImmutableList.of());

		return ProgramBuilder.beginCompute(name, source, ImmutableSet.of(0));
	}

	private static class TextureImage {
		private final int id;
		private final int width;
		private final int height;
		private final int format;

		public TextureImage(int width, int height, InternalTextureFormat format) {
			id = GL46C.glCreateTextures(GL46C.GL_TEXTURE_2D);
			this.width = width;
			this.height = height;
			this.format = format.getGlFormat();
			GlStateManager._bindTexture(id);
			GL46C.glTexStorage2D(GL46C.GL_TEXTURE_2D, 1, format.getGlFormat(), width, height);
		}

		public int getId() {
			return id;
		}

		public void destroy() {
			GlStateManager._deleteTexture(id);
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public int getFormat() {
			return format;
		}
	}
}
