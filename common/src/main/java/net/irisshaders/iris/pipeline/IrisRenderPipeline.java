package net.irisshaders.iris.pipeline;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout.UniformDescription;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record IrisRenderPipeline(ExtendedShader shader, RenderPipeline source, List<VertexFormat> vertexFormats, Map<String, UniformDescription> uniforms) implements BackendRenderPipeline {
	public IrisRenderPipeline(ExtendedShader shader, RenderPipeline source, List<VertexFormat> vertexFormats) {
		this(shader, source, vertexFormats, uniforms(source));
	}

	public IrisRenderPipeline {
		vertexFormats = Collections.unmodifiableList(new ArrayList<>(vertexFormats));
		uniforms = Map.copyOf(uniforms);
	}

	private static Map<String, UniformDescription> uniforms(RenderPipeline source) {
		var uniforms = new LinkedHashMap<String, UniformDescription>();

		for (var group : source.getBindGroupLayouts()) {
			for (var uniform : group.uniforms()) {
				uniforms.put(uniform.name(), uniform);
			}
		}

		return uniforms;
	}

	@Override
	public boolean isClosed() {
		return this.shader.isClosed();
	}

	@Override
	public void close() {
	}
}
