package net.coderbot.iris.uisupport;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.sampler.GlSampler;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.postprocess.FullScreenQuadRenderer;
import net.coderbot.iris.rendertarget.NativeImageBackedNoiseTexture;
import net.coderbot.iris.shaderpack.StringPair;
import net.coderbot.iris.shaderpack.preprocessor.JcppProcessor;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.coderbot.iris.vendored.joml.Matrix4f;
import net.coderbot.iris.vendored.joml.Vector4f;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GaussianBlurRenderer {
	private float unsmoothed;
	private float unsmoothedFrost;
	private int width;
	private int height;
	private Program program;
	private GlFramebuffer firstFB;
	private GlFramebuffer secondFB;
	private int swapTexture;
	private SmoothedFloat blurAmount;
	private SmoothedFloat frostAmount;

	private int target;
	private int directionLocation;
	private int sizeLocation;
	private static GlSampler sampler;
	private static NativeImageBackedNoiseTexture noise;

	static {
		sampler = new GlSampler(true, false, false, false, false);
		noise = new NativeImageBackedNoiseTexture(128);
	}

	public Vector4f noiseBounds = new Vector4f();

	public GaussianBlurRenderer(int width, int height, FrameUpdateNotifier notifier) {
		blurAmount = new SmoothedFloat(0.3f, 0.3f, () -> unsmoothed, notifier);
		frostAmount = new SmoothedFloat(0.3f, 0.3f, () -> unsmoothedFrost, notifier);
		rebuildProgram(width, height);
	}

	public void rebuildProgram(int width, int height) {
		if (program != null) {
			program.destroy();
			program = null;
			firstFB.destroy();
			firstFB = null;
			secondFB.destroy();
			secondFB = null;
			GlStateManager._deleteTexture(swapTexture);
			swapTexture = 0;
		}

		this.width = width;
		this.height = height;

		String vertexSource;
		String source;
		try {
			vertexSource = new String(IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("/defaultComposite.vsh"))), StandardCharsets.UTF_8);
			source = new String(IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("/gaussianBlur.fsh"))), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<StringPair> defineList = new ArrayList<>();
		source = JcppProcessor.glslPreprocessSource(source, defineList);

		ProgramBuilder builder = ProgramBuilder.begin("gaussianBlur", vertexSource, null, source, ImmutableSet.of(0));

		builder.uniformJomlMatrix(UniformUpdateFrequency.ONCE, "projection", () -> new Matrix4f(2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, -1, -1, 0, 1));
		builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "blurAmount", blurAmount);
		builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "frostAmount", frostAmount);
		builder.uniform4f(UniformUpdateFrequency.PER_FRAME, "noiseBounds", () -> noiseBounds);
		builder.addExternalSampler(0, "readImage");
		builder.addDynamicSampler(noise::getId, "noisetex");

		swapTexture = GlStateManager._genTexture();
		IrisRenderSystem.texImage2D(swapTexture, GL30C.GL_TEXTURE_2D, 0, GL30C.GL_RGBA8, width, height, 0, GL30C.GL_RGBA, GL30C.GL_UNSIGNED_BYTE, null);
		IrisRenderSystem.texParameteri(swapTexture, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
		IrisRenderSystem.texParameteri(swapTexture, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_LINEAR);

		this.firstFB = new GlFramebuffer();
		firstFB.addColorAttachment(0, swapTexture);
		this.secondFB = new GlFramebuffer();
		secondFB.addColorAttachment(0, target);
		this.program = builder.build();

		this.directionLocation = GlStateManager._glGetUniformLocation(this.program.getProgramId(), "direction");
		this.sizeLocation = GlStateManager._glGetUniformLocation(this.program.getProgramId(), "texSize");
		program.use();
		IrisRenderSystem.uniform2f(sizeLocation, width, height);
	}

	public void process(int targetImage) {
		if (this.target != targetImage) {
			secondFB.addColorAttachment(0, targetImage);
			this.target = targetImage;
		}
		IrisRenderSystem.bindSamplerToUnit(0, sampler.getId());
		IrisRenderSystem.bindTextureToUnit(GL43C.GL_TEXTURE_2D, 0, targetImage);
		program.use();
		firstFB.bind();
		IrisRenderSystem.uniform2f(directionLocation, 1.0f, 0.0f);
		FullScreenQuadRenderer.INSTANCE.render();
		IrisRenderSystem.bindTextureToUnit(GL43C.GL_TEXTURE_2D, 0, swapTexture);

		secondFB.bind();
		IrisRenderSystem.uniform2f(directionLocation, 0.0f, 1.0f);
		FullScreenQuadRenderer.INSTANCE.render();
		Program.unbind();
		IrisRenderSystem.bindSamplerToUnit(0, 0);
	}

	public void setBlurAmount(float amount) {
		unsmoothed = amount;
	}

	public void setFrostAmount(float amount) {
		unsmoothedFrost = amount;
	}
}
