package net.coderbot.iris.mixin.vertices;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements BufferVertexConsumer {
	@Unique
	boolean extending;

	@Unique
	private int vertexCount;

	@Shadow
	private boolean field_21594;

	@Shadow
	private boolean field_21595;

	@Inject(method = "begin", at = @At("HEAD"))
	private void iris$onBegin(int drawMode, VertexFormat format, CallbackInfo ci) {
		extending = format == VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
		vertexCount = 0;
	}

	@Inject(method = "reset()V", at = @At("HEAD"))
	private void iris$onReset(CallbackInfo ci) {
		extending = false;
		vertexCount = 0;
	}

	@Inject(method = "method_23918(Lnet/minecraft/client/render/VertexFormat;)V", at = @At("RETURN"))
	private void iris$preventHardcodedVertexWriting(VertexFormat format, CallbackInfo ci) {
		if (!extending) {
			return;
		}

		field_21594 = false;
		field_21595 = false;
	}

	@Inject(method = "next()V", at = @At("HEAD"))
	private void iris$beforeNext(CallbackInfo ci) {
		if (!extending) {
			return;
		}

		// TODO: Hardcoded Sildur's water id
		this.putFloat(0, 10008.0F);
		this.putFloat(4, (short) -1);
		this.putFloat(8, (short) -1);
		this.putFloat(12, (short) -1);
		this.nextElement();
		this.putFloat(0, 0F);
		this.putFloat(4, 0F);
		this.nextElement();
		this.putFloat(0, 1F);
		this.putFloat(4, 0F);
		this.putFloat(8, 0F);
		this.putFloat(12, 1F);
		this.nextElement();
	}

	@Inject(method = "next()V", at = @At("RETURN"))
	private void iris$afterNext(CallbackInfo ci) {
		if (!extending) {
			return;
		}

		vertexCount += 1;

		if (vertexCount == 4) {
			vertexCount = 0;
			extendVertexData();
		}
	}

	private void extendVertexData() {
		// TODO: Use captured data to compute the tangent and miduv properties
		// TODO: Also compute correct vertex normals
	}
}
