package net.irisshaders.iris.backend;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.irisshaders.iris.gl.blending.BlendMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record PipelineState(FramebufferDescription framebuffer, List<BlendMode> blendModes, List<Attribute> vertexAttributes, int vertexStride, RenderPipeline vanillaPipeline, boolean shadowPass, GpuBuffer zeroVertexBuffer, List<VertexFormat> vertexFormats) {
	public PipelineState(FramebufferDescription framebuffer, List<BlendMode> blendModes, List<Attribute> vertexAttributes, int vertexStride,
		RenderPipeline vanillaPipeline, boolean shadowPass, GpuBuffer zeroVertexBuffer) {
		this(framebuffer, blendModes, vertexAttributes, vertexStride, vanillaPipeline, shadowPass, zeroVertexBuffer,
			vanillaPipeline == null ? List.of() : vanillaPipeline.getVertexFormatBindings());
	}

	public PipelineState {
		Objects.requireNonNull(framebuffer);
		blendModes = Collections.unmodifiableList(new ArrayList<>(blendModes));
		vertexAttributes = List.copyOf(vertexAttributes);
		vertexFormats = Collections.unmodifiableList(new ArrayList<>(vertexFormats));

		if (framebuffer.colorFormats().size() != blendModes.size()) {
			throw new IllegalArgumentException("color attachments and blend states differ");
		}
	}

	public static DepthStencilState depthState(RenderPipeline pipeline, boolean hasDepth, boolean shadow) {
		var depth = pipeline == null || !hasDepth ? null : pipeline.getDepthStencilState();

		if (depth == null || !shadow) {
			return depth;
		}

		var compare = switch (depth.depthTest()) {
			case GREATER_THAN -> CompareOp.LESS_THAN;
			case GREATER_THAN_OR_EQUAL -> CompareOp.LESS_THAN_OR_EQUAL;
			case LESS_THAN -> CompareOp.GREATER_THAN;
			case LESS_THAN_OR_EQUAL -> CompareOp.GREATER_THAN_OR_EQUAL;
			default -> depth.depthTest();
		};
		return new DepthStencilState(compare, depth.writeDepth(), -depth.depthBiasScaleFactor(), -depth.depthBiasConstant());
	}

	public static boolean cull(RenderPipeline pipeline, boolean shadow) {
		return pipeline != null && pipeline.isCull() && !shadow;
	}

	public static int colorMask(RenderPipeline pipeline, boolean written) {
		if (!written) {
			return 0;
		}

		return pipeline == null || pipeline.getColorTargetStates().isEmpty() || pipeline.getColorTargetStates()
			.getFirst() == null ? 15 : pipeline.getColorTargetStates().getFirst().writeMask() & 15;
	}

	public static boolean isInteger(GpuFormat format) {
		return format == GpuFormat.RGB10A2_UINT || switch (format.componentType()) {
			case UINT_8, SINT_8, UINT_16, SINT_16, UINT_32, SINT_32 -> true;
			default -> false;
		};
	}

	public List<GpuFormat> colorFormats() {
		return this.framebuffer.colorFormats();
	}

	public GpuFormat depthFormat() {
		return this.framebuffer.depthFormat();
	}

	public DepthStencilState depthState() {
		return depthState(this.vanillaPipeline, this.depthFormat() != null, this.shadowPass);
	}

	public record Attribute(int location, int binding, GpuFormat format, int offset) {
		public Attribute(int location, GpuFormat format, int offset) {
			this(location, 0, format, offset);
		}
	}
}
