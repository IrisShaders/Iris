package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.coderbot.batchedentityrendering.impl.BufferBuilderExt;
import net.minecraft.util.Mth;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.util.List;

@Mixin(value = BufferBuilder.class, priority = 1010)
public class MixinBufferBuilder_SegmentRendering implements BufferBuilderExt {
    @Shadow
    private ByteBuffer buffer;

    @Shadow
    private VertexFormat format;

    @Shadow
    private int vertices;

    @Shadow
	protected void ensureVertexCapacity() {
        throw new AssertionError("not shadowed");
    }

	@Shadow
	private int nextElementByte;

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
		MemoryIntrinsics.copyMemory(MemoryUtil.memAddress(this.buffer, this.nextElementByte - i), MemoryUtil.memAddress(this.buffer, this.nextElementByte), i);
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

	@Dynamic
    @Inject(method = "sodium$moveToNextVertex", at = @At("RETURN"), require = 0)
    private void batchedentityrendering$onNextSodium(CallbackInfo ci) {
        if (dupeNextVertex) {
            dupeNextVertex = false;
            duplicateLastVertex();
        }
    }
}
