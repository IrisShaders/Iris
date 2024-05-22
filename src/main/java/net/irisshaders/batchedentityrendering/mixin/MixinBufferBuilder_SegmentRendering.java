package net.irisshaders.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.irisshaders.batchedentityrendering.impl.BufferBuilderExt;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(value = BufferBuilder.class, priority = 1010)
public class MixinBufferBuilder_SegmentRendering implements BufferBuilderExt {
	@Shadow
	private ByteBufferBuilder buffer;

	@Shadow
	private VertexFormat format;

	@Shadow
	private int vertices;
	@Shadow
	@Final
	private int vertexSize;
	@Unique
	private boolean dupeNextVertex;

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
		long l = this.buffer.reserve(this.vertexSize);
		MemoryUtil.memCopy(l - (long)this.vertexSize, l, (long)this.vertexSize);
		++this.vertices;
	}

	@Inject(method = "build", at = @At("RETURN"))
	private void batchedentityrendering$onEnd(CallbackInfoReturnable<MeshData> cir) {
		dupeNextVertex = false;
	}

	@Inject(method = "endLastVertex", at = @At("RETURN"))
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
