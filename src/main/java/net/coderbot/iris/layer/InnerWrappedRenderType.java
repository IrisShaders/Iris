package net.coderbot.iris.layer;

import net.coderbot.batchedentityrendering.impl.WrappableRenderType;
import net.coderbot.iris.mixin.rendertype.RenderTypeAccessor;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class InnerWrappedRenderType extends RenderType implements WrappableRenderType {
	private final RenderStateShard extra;
	private final RenderType wrapped;

	public InnerWrappedRenderType(String name, RenderType wrapped, RenderStateShard extra) {
		super(name, wrapped.format(), wrapped.mode(), wrapped.bufferSize(),
			wrapped.affectsCrumbling(), shouldSortOnUpload(wrapped), wrapped::setupRenderState, wrapped::clearRenderState);

		this.extra = extra;
		this.wrapped = wrapped;
	}

	@Override
	public void setupRenderState() {
		super.setupRenderState();

		extra.setupRenderState();
	}

	@Override
	public void clearRenderState() {
		extra.clearRenderState();

		super.clearRenderState();
	}

	@Override
	public RenderType unwrap() {
		return this.wrapped;
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
	public boolean equals(@Nullable Object object) {
		if (object == null) {
			return false;
		}

		if (object.getClass() != this.getClass()) {
			return false;
		}

		InnerWrappedRenderType other = (InnerWrappedRenderType) object;

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

	public RenderStateShard getExtra() {
		return extra;
	}

	private static boolean shouldSortOnUpload(RenderType type) {
		return ((RenderTypeAccessor) type).shouldSortOnUpload();
	}
}
