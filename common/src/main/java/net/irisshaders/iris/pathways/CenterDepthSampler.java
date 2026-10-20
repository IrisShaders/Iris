package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder.TextureBinding;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.pipeline.programs.CompositeProgram;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import org.joml.Vector4f;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class CenterDepthSampler {
	private final Supplier<GpuTexture> depthSupplier;
	private final GpuTexture texture;
	private final GpuTexture history;
	private final GpuTextureView outputView;
	private final GpuTextureView historyView;
	private final PackSamplers samplers;
	private final CompositeProgram program;
	private GpuTextureView depthView;
	private float frameTime;
	private boolean hasFirstSample;
	private boolean everRetrieved;
	private boolean destroyed;
	private int samples;

	public CenterDepthSampler(PackSamplers samplers, Supplier<GpuTexture> depthSupplier, float halfLife) {
		if (!Float.isFinite(halfLife) || halfLife < 0) {
			throw new IllegalArgumentException("invalid center-depth half-life");
		}

		this.depthSupplier = depthSupplier;
		int usage = GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_RENDER_ATTACHMENT;
		var cleanup = new ArrayList<Runnable>();

		try {
			this.texture = RenderSystem.getDevice().createTexture("Center depth", usage, GpuFormat.R32_FLOAT, 1, 1, 1, 1);
			cleanup.add(this.texture::close);
			this.history = RenderSystem.getDevice().createTexture("Center depth history", usage, GpuFormat.R32_FLOAT, 1, 1, 1, 1);
			cleanup.add(this.history::close);
			this.outputView = RenderSystem.getDevice().createTextureView(this.texture);
			cleanup.add(this.outputView::close);
			this.historyView = RenderSystem.getDevice().createTextureView(this.history);
			cleanup.add(this.historyView::close);
			this.depthView = RenderSystem.getDevice().createTextureView(this.depthSupplier.get(), 0, 1);
			cleanup.add(this.depthView::close);
			RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.history, new Vector4f());
			this.samplers = samplers;
			var outputs = new ColorAttachmentPlan.Builder().program("centerDepth", new int[]{0}, Set.of()).build(1).program("centerDepth");
			this.program = new CompositeProgram(this.samplers, "centerDepth", Map.of(PatchShaderType.VERTEX,
				readShader("/centerDepth.vsh"), PatchShaderType.FRAGMENT, readShader("/centerDepth.fsh")),
				outputs, Collections.singletonList(null));
			cleanup.add(this.program::destroy);
			this.program.getUniforms().uniform1f(UniformUpdateFrequency.PER_FRAME, "lastFrameTime", () -> this.frameTime);
			this.program.getUniforms()
				.uniform1f(UniformUpdateFrequency.ONCE, "decay", () -> Math.log(2) / (halfLife * 0.1));
			this.program.getSamplers()
				.addTextureSampler(new TextureBinding("depth", () -> this.depthView, () -> GlSampler.NEAREST), false, "depth");
			this.program.getSamplers()
				.addTextureSampler(new TextureBinding("history", () -> this.historyView, () -> GlSampler.NEAREST), false, "altDepth");
			this.program.getSamplers().finish();
		} catch (RuntimeException | Error failure) {
			for (var close : cleanup.reversed()) {
				try {
					close.run();
				} catch (RuntimeException | Error e) {
					failure.addSuppressed(e);
				}
			}

			throw failure;
		}
	}

	private static String readShader(String resource) {
		try (var stream = Objects.requireNonNull(CenterDepthSampler.class.getResourceAsStream(resource), resource)) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void sampleCenterDepth() {
		this.sampleCenterDepth(SystemTimeUniforms.TIMER.getLastFrameTime());
	}

	public void sampleCenterDepth(float lastFrameTime) {
		if (this.destroyed || this.hasFirstSample && !this.everRetrieved) {
			return;
		}

		if (!Float.isFinite(lastFrameTime) || lastFrameTime < 0) {
			throw new IllegalArgumentException("invalid frame time");
		}

		var depth = this.depthSupplier.get();

		if (this.depthView == null || this.depthView.texture() != depth) {
			var next = RenderSystem.getDevice().createTextureView(depth, 0, 1);

			if (this.depthView != null) {
				this.depthView.close();
			}

			this.depthView = next;
		}

		this.frameTime = lastFrameTime;
		this.samplers.beginFrame();
		this.program.draw(List.of(this.outputView), ViewportData.defaultValue(), this.samples, this.samples);
		this.samples++;
		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(this.texture, this.history, 0, 0, 0, 0, 0, 1, 1);
		this.hasFirstSample = true;
	}

	public GpuTextureView getCenterDepthTextureView() {
		return this.historyView;
	}

	public void setUsage(boolean usage) {
		everRetrieved |= usage;
	}

	public void destroy() {
		if (this.destroyed) {
			return;
		}

		destroyed = true;
		this.program.destroy();

		if (this.depthView != null) {
			this.depthView.close();
		}

		this.outputView.close();
		this.historyView.close();
		this.texture.close();
		this.history.close();
	}
}
