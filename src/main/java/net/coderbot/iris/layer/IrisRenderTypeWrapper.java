package net.coderbot.iris.layer;

import net.coderbot.batchedentityrendering.impl.WrappableRenderType;
import net.coderbot.iris.mixin.rendertype.RenderTypeAccessor;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class IrisRenderTypeWrapper extends RenderType implements WrappableRenderType {
	private final UseProgramRenderStateShard useProgram;
	private final RenderType wrapped;

	public IrisRenderTypeWrapper(String name, RenderType wrapped, UseProgramRenderStateShard useProgram) {
		super(name, wrapped.format(), wrapped.mode(), wrapped.bufferSize(),
			wrapped.affectsCrumbling(), shouldSortOnUpload(wrapped), wrapped::setupRenderState, wrapped::clearRenderState);

		this.useProgram = useProgram;
		this.wrapped = wrapped;
	}

	@Override
	public void setupRenderState() {
		super.setupRenderState();

		useProgram.setupRenderState();
	}

	@Override
	public void clearRenderState() {
		useProgram.clearRenderState();

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

		IrisRenderTypeWrapper other = (IrisRenderTypeWrapper) object;

		return Objects.equals(this.wrapped, other.wrapped) && Objects.equals(this.useProgram, other.useProgram);
	}

	@Override
	public int hashCode() {
		// Add one so that we don't have the exact same hash as the wrapped object.
		// This means that we won't have a guaranteed collision if we're inserted to a map alongside the unwrapped object.
		return this.wrapped.hashCode() + 1;
	}

	@Override
	public String toString() {
		return "iris:" + this.wrapped.toString();
	}

	private static boolean shouldSortOnUpload(RenderType type) {
		return ((RenderTypeAccessor) type).shouldSortOnUpload();
	}
}
