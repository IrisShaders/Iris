package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.batchedentityrendering.impl.BufferBuilderExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public class MixinBufferBuilder_SegmentRendering implements BufferBuilderExt {
	@Shadow
	private ByteBuffer buffer;

	@Shadow
	private VertexFormat format;

	@Shadow
	private int vertices;
	@Shadow
	private int nextElementByte;
	@Unique
	private boolean dupeNextVertex;

	@Shadow
	protected void ensureVertexCapacity() {
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

	private void duplicateLastVertex() {
		int i = this.format.getVertexSize();
		this.buffer.put(this.nextElementByte, this.buffer, this.nextElementByte - i, i);
		this.nextElementByte += i;
		++this.vertices;
		this.ensureVertexCapacity();
	}

	@Inject(method = "end", at = @At("RETURN"))
	private void batchedentityrendering$onEnd(CallbackInfoReturnable<BufferBuilder.RenderedBuffer> cir) {
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
