package net.irisshaders.iris.pathways.colorspace;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder.TextureBinding;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.pipeline.programs.CompositeProgram;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ColorSpaceFragmentConverter implements ColorSpaceConverter {
	private ColorSpace colorSpace;
	private CompositeProgram program;
	private PackSamplers samplers;
	private GpuTexture swap;
	private GpuTextureView swapView, target;

	public ColorSpaceFragmentConverter(int width, int height, ColorSpace colorSpace) {
		rebuildProgram(width, height, colorSpace);
	}

	@Override
	public void rebuildProgram(int width, int height, ColorSpace colorSpace) {
		close();
		this.colorSpace = colorSpace;

		if (colorSpace == ColorSpace.SRGB) {
			return;
		}

		String vertex;
		String fragment;

		try (var vs = Objects.requireNonNull(getClass().getResourceAsStream("/colorSpace.vsh"));
			 var fs = Objects.requireNonNull(getClass().getResourceAsStream("/colorSpace.csh"))) {
			vertex = new String(vs.readAllBytes(), StandardCharsets.UTF_8).replace("iris_Position", "Position").replace("iris_UV0", "UV0");
			fragment = new String(fs.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		var defines = new ArrayList<StringPair>();
		defines.add(new StringPair("CURRENT_COLOR_SPACE", Integer.toString(colorSpace.ordinal())));

		for (var space : ColorSpace.values()) {
			defines.add(new StringPair(space.name(), Integer.toString(space.ordinal())));
		}

		fragment = JcppProcessor.glslPreprocessSource(fragment, defines);
		samplers = new PackSamplers(RenderSystem.getDevice());

		var outputs = new ColorAttachmentPlan.Builder().program("colorSpace", new int[]{0}, Set.of()).build(1).program("colorSpace");
		program = new CompositeProgram(RenderSystem.getDevice(), samplers, "colorSpace",
			Map.of(PatchShaderType.VERTEX, vertex, PatchShaderType.FRAGMENT, fragment), outputs, Collections.singletonList(null));
		program.uniforms()
			.uniformMatrix(UniformUpdateFrequency.ONCE, "projection", () -> new Matrix4f(2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, -1, -1, 0,
				1));
		program.samplers()
			.addTextureSampler(new TextureBinding("colorSpace/input", () -> target, () -> GlSampler.NEAREST), false, "readImage");
		program.samplers().finish();
	}

	@Override
	public void process(GpuTextureView target) {
		if (colorSpace == ColorSpace.SRGB) {
			return;
		}

		var device = RenderSystem.getDevice();
		int width = target.getWidth(0);
		int height = target.getHeight(0);

		if (swap == null || swap.getWidth(0) != width || swap.getHeight(0) != height || swap.getFormat() != target.texture().getFormat()) {
			if (swap != null) {
				swapView.close();
				swap.close();
			}

			swap = device.createTexture("Iris color conversion", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_COPY_SRC,
				target.texture().getFormat(), width, height, 1, 1);
			swapView = device.createTextureView(swap);
		}

		this.target = target;

		try {
			program.draw(List.of(swapView), new ViewportData(1, 0, 0), 0, 0);
			device.createCommandEncoder().copyTextureToTexture(swap, target.texture(), 0, 0, 0, 0, 0, width, height);
		} finally {
			this.target = null;
		}
	}

	@Override
	public void close() {
		if (program != null) {
			program.destroy();
			program = null;
		}

		if (samplers != null) {
			samplers.close();
			samplers = null;
		}

		if (swap != null) {
			swapView.close();
			swap.close();
			swapView = null;
			swap = null;
		}
	}
}
