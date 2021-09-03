package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.BufferBuilderExt;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.util.List;

@Mixin(BufferBuilder.class)
public class MixinBufferBuilder_SegmentRendering implements BufferBuilderExt {
    @Shadow
    private ByteBuffer buffer;

    @Shadow
    @Final
    private List<BufferBuilder.DrawArrayParameters> parameters;

    @Shadow
    private int lastParameterIndex;

    @Shadow
    private int buildStart;

    @Shadow
    private int elementOffset;

    @Shadow
    private int nextDrawStart;

    @Override
    public void setupBufferSlice(ByteBuffer buffer, BufferBuilder.DrawArrayParameters parameters) {
        // add the buffer slice
        this.buffer = buffer;

        // add our singular parameter
        this.parameters.clear();
        this.parameters.add(parameters);

        // should be zero, just making sure
        this.lastParameterIndex = 0;

        // configure the build start (to avoid a warning message) and element offset (probably not important)
        this.buildStart = parameters.getCount() * parameters.getVertexFormat().getVertexSize();
        this.elementOffset = this.buildStart;

        // should be zero, just making sure
        this.nextDrawStart = 0;

        // target.vertexCount is never nonzero in this process.
        // target.currentElement is never non-null in this process.
        // target.currentElementId is never nonzero.
        // target.drawMode is irrelevant.
        // target.format is irrelevant.
        // The final 3 booleans are also irrelevant.
    }

    @Override
    public void teardownBufferSlice() {
        // the parameters got popped by the render call, we don't need to worry about them
        // make sure to un-set the buffer to prevent anything bad from happening with it.
        this.buffer = null;

        // target.parameters gets reset.
        // target.lastParameterIndex gets reset.
        // target.buildStart gets reset.
        // target.elementOffset gets reset.
        // target.nextDrawStart gets reset.

        // target.vertexCount is never nonzero in this process.
        // target.currentElement is never non-null in this process.
        // target.currentElementId is never nonzero.
        // target.drawMode is irrelevant.
        // target.format is irrelevant.
        // The final 3 booleans are also irrelevant.
    }

    @Shadow
    private VertexFormat format;

    @Shadow
    private int vertexCount;

    @Shadow
    private void grow() {
        throw new AssertionError("not shadowed");
    }

    @Override
    public void splitStrip() {
        if (vertexCount == 0) {
            // no strip to split, not building.
            return;
        }

        duplicateLastVertex();
        dupeNextVertex = true;
    }

    @Unique
    private boolean dupeNextVertex;

    private void duplicateLastVertex() {
        int i = this.format.getVertexSize();
        this.buffer.position(this.elementOffset);
        ByteBuffer byteBuffer = this.buffer.duplicate();
        byteBuffer.position(this.elementOffset - i).limit(this.elementOffset);
        this.buffer.put(byteBuffer);
        this.elementOffset += i;
        ++this.vertexCount;
        this.grow();
    }

    @Inject(method = "end", at = @At("RETURN"))
    private void batchedentityrendering$onEnd(CallbackInfo ci) {
        dupeNextVertex = false;
    }

    @Inject(method = "next", at = @At("RETURN"))
    private void batchedentityrendering$onNext(CallbackInfo ci) {
        if (dupeNextVertex) {
            dupeNextVertex = false;
            duplicateLastVertex();
        }
    }
}
