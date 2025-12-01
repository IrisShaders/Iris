package net.irisshaders.iris.layer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.mixin.rendertype.RenderTypeAccessor;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class OuterWrappedRenderType extends RenderType {
	private final RenderStateShard extra;
	private final RenderType wrapped;

	public OuterWrappedRenderType(String name, RenderType wrapped, RenderStateShard extra) {
		super(name, wrapped.bufferSize(),
			wrapped.affectsCrumbling(), shouldSortOnUpload(wrapped), wrapped::setupRenderState, wrapped::clearRenderState);

		this.extra = extra;
		this.wrapped = wrapped;
	}

	public static OuterWrappedRenderType wrapExactlyOnce(String name, RenderType wrapped, RenderStateShard extra) {
		while (wrapped instanceof OuterWrappedRenderType) {
			wrapped = ((OuterWrappedRenderType) wrapped).unwrap();
		}

		return new OuterWrappedRenderType(name, wrapped, extra);
	}

	private RenderType unwrap() {
		return wrapped;
	}

	private static boolean shouldSortOnUpload(RenderType type) {
		return ((RenderTypeAccessor) type).shouldSortOnUpload();
	}

	@Override
	public void setupRenderState() {
		extra.setupRenderState();

		super.setupRenderState();
	}

	@Override
	public void clearRenderState() {
		super.clearRenderState();

		extra.clearRenderState();
	}

	@Override
	public Optional<RenderType> outline() {
		return this.wrapped.outline();
	}

	@Override
	public boolean isOutline() {
		return this.wrapped.isOutline();
	}

	@Override
	public RenderPipeline pipeline() {
		return wrapped.pipeline();
	}

	@Override
	public void draw(MeshData meshData) {
		extra.setupRenderState();
		wrapped.draw(meshData);
		extra.clearRenderState();
	}

	@Override
	public boolean sortOnUpload() {
		return wrapped.sortOnUpload();
	}

	@Override
	public RenderPipeline iris$getPipeline() {
		return wrapped.iris$getPipeline();
	}

	@Override
	public RenderTarget iris$getRenderTarget() {
		return wrapped.iris$getRenderTarget();
	}

	@Override
	public boolean canConsolidateConsecutiveGeometry() {
		return wrapped.canConsolidateConsecutiveGeometry();
	}

	@Override
	public boolean affectsCrumbling() {
		return wrapped.affectsCrumbling();
	}

	@Override
	public int bufferSize() {
		return wrapped.bufferSize();
	}

	@Override
	public VertexFormat format() {
		return wrapped.format();
	}

	@Override
	public VertexFormat.Mode mode() {
		return wrapped.mode();
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if (object == null) {
			return false;
		}

		if (object.getClass() != this.getClass()) {
			return false;
		}

		OuterWrappedRenderType other = (OuterWrappedRenderType) object;

		return Objects.equals(this.wrapped, other.wrapped) && Objects.equals(this.extra, other.extra);
	}

	@Override
	public int hashCode() {
		// Add one so that we don't have the exact same hash as the wrapped object.
		// This means that we won't have a guaranteed collision if we're inserted to a map alongside the unwrapped object.
		return this.wrapped.hashCode() + 1;
	}

	@Override
	public String toString() {
		return "iris_wrapped:" + this.wrapped.toString();
	}
}
