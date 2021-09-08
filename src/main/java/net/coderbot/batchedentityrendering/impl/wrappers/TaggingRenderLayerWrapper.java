package net.coderbot.batchedentityrendering.impl.wrappers;

import net.coderbot.batchedentityrendering.impl.WrappableRenderLayer;
import net.coderbot.batchedentityrendering.mixin.RenderLayerAccessor;
import net.minecraft.client.render.RenderLayer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class TaggingRenderLayerWrapper extends RenderLayer implements WrappableRenderLayer {
    private final int tag;
    private final RenderLayer wrapped;

    public TaggingRenderLayerWrapper(String name, RenderLayer wrapped, int tag) {
        super(name, wrapped.getVertexFormat(), wrapped.getDrawMode(), wrapped.getExpectedBufferSize(),
                wrapped.hasCrumbling(), isTranslucent(wrapped), wrapped::startDrawing, wrapped::endDrawing);

        this.tag = tag;
        this.wrapped = wrapped;
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

        TaggingRenderLayerWrapper other = (TaggingRenderLayerWrapper) object;

        return this.tag == other.tag && Objects.equals(this.wrapped, other.wrapped);
    }

    @Override
    public int hashCode() {
        // Add one so that we don't have the exact same hash as the wrapped object.
        // This means that we won't have a guaranteed collision if we're inserted to a map alongside the unwrapped object.
        return this.wrapped.hashCode() + 1;
    }

    @Override
    public String toString() {
        return "tagged(" +tag+ "):" + this.wrapped.toString();
    }

    private static boolean isTranslucent(RenderLayer layer) {
        return ((RenderLayerAccessor) layer).isTranslucent();
    }
}
