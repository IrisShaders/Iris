package net.irisshaders.iris.pipeline;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.util.TextureViewAndSampler;
import net.irisshaders.iris.backend.ForwardingRenderPass;
import net.irisshaders.iris.backend.IrisRenderPass;
import net.irisshaders.iris.backend.MinecraftRenderPass;
import net.irisshaders.iris.backend.WorldPassCommands;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.targets.WorldRenderTargets;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.Minecraft;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public final class WorldRenderPass extends ForwardingRenderPass {
	private final IrisRenderingPipeline iris;
	private final WorldPassCommands commands;
	private final IrisRenderPass renderPass;
	private final WorldRenderTargets.Phase phase;
	private final GpuTextureView depth;
	private final RenderArea area;
	private final Map<String, Object> resources = new HashMap<>();
	private Map<String, Object> uniforms = Map.of();
	private ColorAttachmentPlan attachmentPlan;
	private IrisRenderPipeline pipeline;
	private ByteBuffer pushConstants;
	private boolean constantsPushed;

	public WorldRenderPass(IrisRenderingPipeline iris, WorldPassCommands commands, RenderArea area, GpuTextureView depth,
		WorldRenderTargets.Phase phase) {
		super(commands);
		this.iris = iris;
		this.commands = commands;
		this.renderPass = new MinecraftRenderPass(commands);
		this.area = area;
		this.depth = depth;
		this.phase = phase;
		this.attachmentPlan = iris.getWorldTargets().getAttachmentPlan(phase);
	}

	public IrisRenderPass getCommands() {
		return this.renderPass;
	}

	public void setUniforms(Map<String, Object> uniforms) {
		this.uniforms = uniforms;
	}

	@Override
	public void setPipeline(BackendRenderPipeline pipeline) {
		if (!(pipeline instanceof IrisRenderPipeline override)) {
			throw new IllegalStateException("world draw has no Iris pipeline: " + pipeline);
		}

		this.pipeline = override;
		this.constantsPushed = false;
		var next = this.iris.getWorldTargets().getAttachmentPlan(this.phase, override.shader().getName());

		if (next != this.attachmentPlan) {
			var colors = this.iris.getWorldTargets().getFramebuffer(this.phase, override.shader().getName()).getColorViews();

			for (var color : colors) {
				if (color.getWidth(0) != this.area.width() || color.getHeight(0) != this.area.height()) {
					throw new IllegalArgumentException("world attachment groups must have matching dimensions");
				}
			}

			this.commands.setAttachments(colors, this.depth, this.area);
			this.attachmentPlan = next;
		}
	}

	@Override
	public void pushConstants(ByteBuffer value) {
		if (this.pushConstants == null || this.pushConstants.capacity() < value.remaining()) {
			this.pushConstants = ByteBuffer.allocateDirect(value.remaining());
		}

		this.pushConstants.clear().order(value.order()).put(value.duplicate()).flip();
		this.constantsPushed = true;
	}

	public void end() {
		this.commands.end();
	}

	@Override
	protected void beforeDraw() {
		if (this.pipeline == null || this.pipeline.isClosed()) {
			throw new IllegalStateException("world draw has no active Iris shader");
		}

		this.resources.clear();

		for (var entry : this.uniforms.entrySet()) {
			var uniform = this.pipeline.uniforms().get(entry.getKey());
			var value = entry.getValue();

			if (value instanceof TextureViewAndSampler && (uniform == null || uniform.type() != UniformType.COMBINED_IMAGE_SAMPLER)) {
				continue;
			}

			if (uniform != null && uniform.type() == UniformType.TEXEL_BUFFER && value instanceof GpuBufferSlice slice) {
				value = this.commands.getTexelBufferView(slice, uniform.gpuFormat());
			}

			this.resources.put(entry.getKey(), value);
		}

		var level = Minecraft.getInstance().level;
		this.pipeline.shader()
			.apply(this.renderPass, this.pipeline, this.phase, this.depth == null ? null : this.depth.texture().getFormat(), this.resources,
				this.constantsPushed ? this.pushConstants : null, level == null ? 0 : level.getGameTime(),
				SystemTimeUniforms.COUNTER.getAsInt());
	}
}
