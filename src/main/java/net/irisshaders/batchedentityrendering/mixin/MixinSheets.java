package net.irisshaders.batchedentityrendering.mixin;

import net.irisshaders.batchedentityrendering.impl.BlendingStateHolder;
import net.irisshaders.batchedentityrendering.impl.TransparencyType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheets.class)
public class MixinSheets {
	@Shadow
	@Final
	private static RenderType ARMOR_TRIMS_SHEET_TYPE;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void setSheet(CallbackInfo ci) {
		((BlendingStateHolder) ARMOR_TRIMS_SHEET_TYPE).setTransparencyType(TransparencyType.OPAQUE_DECAL);
		((BlendingStateHolder) RenderType.textBackground()).setTransparencyType(TransparencyType.OPAQUE);
		((BlendingStateHolder) RenderType.textBackgroundSeeThrough()).setTransparencyType(TransparencyType.OPAQUE);
	}
}
