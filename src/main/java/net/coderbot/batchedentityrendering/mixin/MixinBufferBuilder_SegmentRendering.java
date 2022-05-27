package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.batchedentityrendering.impl.BufferBuilderExt;
import net.minecraft.util.Mth;
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
    private List<BufferBuilder.DrawState> drawStates;

    @Shadow
    private int lastPoppedStateIndex;

    @Shadow
    private int totalRenderedBytes;

    @Shadow
    private int nextElementByte;

    @Shadow
    private int totalUploadedBytes;

    @Override
    public void setupBufferSlice(ByteBuffer buffer, BufferBuilder.DrawState drawState) {
        // add the buffer slice
        this.buffer = buffer;

        // add our singular parameter
        this.drawStates.clear();
        this.drawStates.add(drawState);

        // should be zero, just making sure
        this.lastPoppedStateIndex = 0;

        // configure the build start (to avoid a warning message) and element offset (probably not important)
		this.totalRenderedBytes = Mth.roundToward(drawState.bufferSize(), 4);
		this.nextElementByte = this.totalRenderedBytes;

        // should be zero, just making sure
        this.totalUploadedBytes = 0;

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
    private int vertices;

    @Shadow
    private void ensureVertexCapacity() {
        throw new AssertionError("not shadowed");
    }

    @Override
    public void splitStrip() {
        if (vertices == 0) {
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
        this.buffer.position(this.nextElementByte);
        ByteBuffer byteBuffer = this.buffer.duplicate();
        byteBuffer.position(this.nextElementByte - i).limit(this.nextElementByte);
        this.buffer.put(byteBuffer);
        this.nextElementByte += i;
        ++this.vertices;
        this.ensureVertexCapacity();
    }

    @Inject(method = "end", at = @At("RETURN"))
    private void batchedentityrendering$onEnd(CallbackInfo ci) {
        dupeNextVertex = false;
    }

    @Inject(method = "endVertex", at = @At("RETURN"))
    private void batchedentityrendering$onNext(CallbackInfo ci) {
        if (dupeNextVertex) {
            dupeNextVertex = false;
            duplicateLastVertex();
        }
    }
}
