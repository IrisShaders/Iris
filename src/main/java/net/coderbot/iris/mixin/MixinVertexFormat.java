package net.coderbot.iris.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.coderbot.iris.layer.VertexFormatInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexFormat.class)
public class MixinVertexFormat implements VertexFormatInterface {
	@Unique
	private boolean hasOverlay;

	@Inject(method = "<init>", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/vertex/VertexFormat;vertexSize:I"))
	private void getFormatInfo(ImmutableList<VertexFormatElement> immutableList, CallbackInfo ci) {
		hasOverlay = immutableList.contains(DefaultVertexFormat.ELEMENT_UV1);
	}

	@Override
	public boolean hasOverlay() {
		return hasOverlay;
	}
}
