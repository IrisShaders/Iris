package net.coderbot.iris.layer;

import net.coderbot.batchedentityrendering.impl.WrappableRenderLayer;
import net.coderbot.iris.mixin.renderlayer.RenderLayerAccessor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class InnerWrappedRenderLayer extends RenderLayer implements WrappableRenderLayer {
	private final RenderPhase extra;
	private final RenderLayer wrapped;

	public InnerWrappedRenderLayer(String name, RenderLayer wrapped, RenderPhase extra) {
		super(name, wrapped.getVertexFormat(), wrapped.getDrawMode(), wrapped.getExpectedBufferSize(),
			wrapped.hasCrumbling(), isTranslucent(wrapped), wrapped::startDrawing, wrapped::endDrawing);

		this.extra = extra;
		this.wrapped = wrapped;
	}

	@Override
	public void startDrawing() {
		super.startDrawing();

		extra.startDrawing();
	}

	@Override
	public void endDrawing() {
		extra.endDrawing();

		super.endDrawing();
	}

	@Override
	public RenderLayer unwrap() {
		return this.wrapped;
	}

	@Override
	public Optional<RenderLayer> getAffectedOutline() {
		return this.wrapped.getAffectedOutline();
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

		InnerWrappedRenderLayer other = (InnerWrappedRenderLayer) object;

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

	private static boolean isTranslucent(RenderLayer layer) {
		return ((RenderLayerAccessor) layer).isTranslucent();
	}
}
