package net.irisshaders.iris.uniforms.buffer;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.irisshaders.iris.gl.uniform.UniformType;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.pipeline.GraphicsBackend;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.pipeline.transform.Std140Type;
import net.irisshaders.iris.pipeline.transform.UniformPlan;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** Per-program draw values: Vulkan push constants or OpenGL loose uniforms. */
public class DrawUniforms extends UniformValues {
	private final Set<String> draw = new HashSet<>();
	private final UniformPlan.Profile profile;
	private FrameUniforms frame;
	private Supplier<Map<String, List<GpuBufferSlice>>> uniformBuffers = Map::of;

	public DrawUniforms(ShaderResourceLayout layout) {
		this(layout, UniformPlan.Profile.STANDARD);
	}

	public DrawUniforms(ShaderResourceLayout layout, UniformPlan.Profile profile) {
		super(layout);
		this.profile = profile;
	}

	public void useFrameUniforms(FrameUniforms frame) {
		this.frame = frame;
	}

	public FrameUniforms frameUniforms() {
		return this.frame;
	}

	public void setUniformBuffers(Supplier<Map<String, List<GpuBufferSlice>>> buffers) {
		this.uniformBuffers = buffers;
	}

	public Map<String, List<GpuBufferSlice>> uniformBuffers() {
		return this.uniformBuffers.get();
	}

	public UniformPlan plan() {
		return new UniformPlan(this.frame == null ? null : this.frame.getLayout(), this.draw, this.profile);
	}

	private boolean isFrame(String name) {
		return this.frame != null && this.frame.getLayout().uniforms().containsKey(name) && !this.draw.contains(name);
	}

	public void drawUniform(String name, UniformType type) {
		this.draw.add(name);
		this.externallyManagedUniform(name, type);
	}

	@Override
	public boolean isRegistered(String name) {
		return super.isRegistered(name) || this.isFrame(name);
	}

	@Override
	public Set<String> registeredUniforms() {
		var names = new HashSet<>(super.registeredUniforms());
		if (this.frame != null) {
			names.addAll(this.frame.getLayout().uniforms().keySet());
		}
		return Set.copyOf(names);
	}

	@Override
	protected UniformValues register(UniformUpdateFrequency frequency, String name, String expected, IntConsumer writer) {
		return this.isFrame(name) ? this : super.register(frequency, name, expected, writer);
	}

	@Override
	public void resolveLayout(ShaderResourceLayout layout) {
		if (GraphicsBackend.isVulkan()) {
			int size = layout.pushUniforms().values().stream().mapToInt(value -> value.offset() + value.type().std430Size()).max().orElse(0);
			super.resolveLayout(new ShaderResourceLayout(size, layout.pushUniforms(), List.of()));
		} else {
			super.resolveLayout(new ShaderResourceLayout(layout.uniformBufferSize(), layout.uniforms(), List.of()));
		}
	}

	@Override
	protected int uniformSize(Std140Type type) {
		return GraphicsBackend.isVulkan() ? type.std430Size() : type.size();
	}

	public ByteBuffer pushConstants() {
		return this.data();
	}

	public void evaluateLocalValues(long tick, long frame) {
		this.update(tick, frame);
	}
}
