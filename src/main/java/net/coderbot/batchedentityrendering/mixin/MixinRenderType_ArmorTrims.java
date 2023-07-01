package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(RenderType.class)
public abstract class MixinRenderType_ArmorTrims extends RenderStateShard {
	public MixinRenderType_ArmorTrims(String pRenderStateShard0, Runnable pRunnable1, Runnable pRunnable2) {
		super(pRenderStateShard0, pRunnable1, pRunnable2);
	}

	@Shadow
	protected static RenderType.CompositeRenderType create(String pString0, VertexFormat pVertexFormat1, VertexFormat.Mode pVertexFormat$Mode2, int pInt3, boolean pBoolean4, boolean pBoolean5, RenderType.CompositeState pRenderType$CompositeState6) {
		return null;
	}

	private static final Function<ResourceLocation, RenderType> ARMOR_TRIM = Util.memoize(
            pResourceLocation0 -> {
                RenderType.CompositeState lvRenderType$CompositeState1 = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER) // This seems wrong. It's not. We don't want overlay color.
                    .setTextureState(new TextureStateShard(pResourceLocation0, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(true);
                return create("armor_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, lvRenderType$CompositeState1);
            }
	);

	@Inject(method = "armorCutoutNoCull", at = @At("HEAD"), cancellable = true)
	private static void iris$replaceTrimRenderType(ResourceLocation pResourceLocation0, CallbackInfoReturnable<RenderType> cir) {
		if (pResourceLocation0 == Sheets.ARMOR_TRIMS_SHEET) {
			cir.setReturnValue(ARMOR_TRIM.apply(pResourceLocation0));
		}
	}
}
