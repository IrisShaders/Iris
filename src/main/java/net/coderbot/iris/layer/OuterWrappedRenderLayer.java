package net.coderbot.iris.layer;

import net.coderbot.iris.mixin.renderlayer.RenderLayerAccessor;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class OuterWrappedRenderLayer extends RenderType implements WrappableRenderLayer {
	private final RenderStateShard extra;
	private final RenderType wrapped;

	public OuterWrappedRenderLayer(String name, RenderType wrapped, RenderStateShard extra) {
		super(name, wrapped.format(), wrapped.mode(), wrapped.bufferSize(),
			wrapped.affectsCrumbling(), isTranslucent(wrapped), wrapped::setupRenderState, wrapped::clearRenderState);

		this.extra = extra;
		this.wrapped = wrapped;
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

		OuterWrappedRenderLayer other = (OuterWrappedRenderLayer) object;

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

	private static boolean isTranslucent(RenderType layer) {
		return ((RenderLayerAccessor) layer).isTranslucent();
	}
}
