package net.coderbot.iris.mixin.renderlayer;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.IrisRenderLayerWrapper;
import net.coderbot.iris.layer.UseProgramRenderPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(RenderLayer.class)
public class MixinRenderLayer {
	@Shadow
	@Final
	@Mutable
	private static RenderLayer SOLID;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer CUTOUT_MIPPED;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer CUTOUT;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer TRANSLUCENT;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer TRIPWIRE;

	@Unique
	private static RenderLayer iris$LINES;

	@Shadow @Final @Mutable private static RenderLayer LEASH;
	@Shadow @Final @Mutable private static RenderLayer ARMOR_GLINT;
	@Shadow @Final @Mutable private static RenderLayer ARMOR_ENTITY_GLINT;
	@Shadow @Final @Mutable private static RenderLayer GLINT_TRANSLUCENT;
	@Shadow @Final @Mutable private static RenderLayer GLINT;
	@Shadow @Final @Mutable private static RenderLayer DIRECT_GLINT;
	@Shadow @Final @Mutable private static RenderLayer ENTITY_GLINT;
	@Shadow @Final @Mutable private static RenderLayer DIRECT_ENTITY_GLINT;

	@Shadow @Final @Mutable private static RenderLayer TRANSLUCENT_MOVING_BLOCK;
	@Shadow @Final @Mutable private static RenderLayer LIGHTNING;

	@Shadow @Final @Mutable private static RenderLayer WATER_MASK;

	@Shadow @Final @Mutable private static RenderLayer TRANSLUCENT_NO_CRUMBLING;

	/*TODO(21w10a) figure out how to fix this
	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;alpha(Lnet/minecraft/client/render/RenderPhase$Alpha;)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;"))
	private static RenderLayer.MultiPhaseParameters.Builder iris$tweakCutoutAlpha(RenderLayer.MultiPhaseParameters.Builder builder, RenderPhase.Alpha alpha) {
		// OptiFine makes CUTOUT and CUTOUT_MIPPED use an alpha test of 0.1 instead of 0.5.
		//
		// We must replicate this behavior or else there will be issues.
		return builder.alpha(new RenderPhase.Alpha(0.1F));
	}*/

	static {
		SOLID = wrap("iris:terrain_solid", SOLID, GbufferProgram.TERRAIN);
		CUTOUT_MIPPED = wrap("iris:terrain_cutout_mipped", CUTOUT_MIPPED, GbufferProgram.TERRAIN);
		CUTOUT = wrap("iris:terrain_cutout", CUTOUT, GbufferProgram.TERRAIN);
		TRANSLUCENT = wrap("iris:translucent", TRANSLUCENT, GbufferProgram.TRANSLUCENT_TERRAIN);
		TRIPWIRE = wrap("iris:tripwire", TRIPWIRE, GbufferProgram.TRANSLUCENT_TERRAIN);
		// TODO: figure out how to assign to RenderLayer.LINES
		// We cannot use @Shadow easily because the type of the field is a package-private class
		iris$LINES = wrap("iris:lines", RenderLayer.LINES, GbufferProgram.BASIC);

		// TODO: SOLID / CUTOUT_MIPPED / CUTOUT are used for falling blocks and blocks being pushed by pistons
		// Should they still be rendered in terrain?

		TRANSLUCENT_MOVING_BLOCK = wrap("iris:translucent_moving_block", TRANSLUCENT_MOVING_BLOCK, GbufferProgram.BLOCK_ENTITIES);
		// This doesn't appear to be used, but it otherwise looks to be the same as TRANSLUCENT
		TRANSLUCENT_NO_CRUMBLING = wrap("iris:translucent_no_crumbling", TRANSLUCENT_NO_CRUMBLING, GbufferProgram.TRANSLUCENT_TERRAIN);

		LEASH = wrap("iris:leash", LEASH, GbufferProgram.BASIC);
		// TODO: Is this an appropriate program? Water masks don't have a texture...
		WATER_MASK = wrap("iris:water_mask", WATER_MASK, GbufferProgram.ENTITIES);
		ARMOR_GLINT = wrapGlint("armor", ARMOR_GLINT);
		ARMOR_ENTITY_GLINT = wrapGlint("armor_entity", ARMOR_ENTITY_GLINT);
		GLINT_TRANSLUCENT = wrapGlint("translucent", GLINT_TRANSLUCENT);
		GLINT = wrapGlint(null, GLINT);
		DIRECT_GLINT = wrapGlint("direct", DIRECT_GLINT);
		ENTITY_GLINT = wrapGlint("entity", ENTITY_GLINT);
		DIRECT_ENTITY_GLINT = wrapGlint("direct_entity_glint", DIRECT_ENTITY_GLINT);

		LIGHTNING = wrap("iris:lightning", LIGHTNING, GbufferProgram.ENTITIES);
	}

	/**
	 * @author coderbot
	 * @reason Use the wrapped render layer instead.
	 */
	@Overwrite
	public static RenderLayer getLines() {
		return iris$LINES;
	}

	private static RenderLayer wrap(String name, RenderLayer wrapped, GbufferProgram program) {
		return new IrisRenderLayerWrapper(name, wrapped, new UseProgramRenderPhase(program));
	}

	private static RenderLayer wrapGlint(String glintType, RenderLayer wrapped) {
		String wrappedName = "iris:" + glintType + "_glint";

		if (glintType == null) {
			wrappedName = "iris:glint";
		}

		return wrap(wrappedName, wrapped, GbufferProgram.ARMOR_GLINT);
	}

	private static RenderLayer wrap(RenderLayer wrapped, GbufferProgram program) {
		String name = ((RenderPhaseAccessor) wrapped).getName();

		return wrap("iris:" + name, wrapped, program);
	}

	@Inject(at = @At("RETURN"), method = {
		"getArmorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntitySolid(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityCutout(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getItemEntityTranslucentCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityTranslucentCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntitySmoothCutout(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityDecal(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityNoOutline(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityShadow(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getText(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getTextSeeThrough(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getTextIntensity(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getTextIntensitySeeThrough(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		"getTextIntensityPolygonOffset(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
	}, cancellable = true)
	private static void iris$wrapEntityRenderLayers(Identifier texture, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.ENTITIES));
	}

	@Inject(at = @At("RETURN"), method = {
		"getEntityCutoutNoCull(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityCutoutNoCullZOffset(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;",
		"getEntityTranslucent(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;",
	}, cancellable = true)
	private static void iris$wrapEntityRenderLayersZ(Identifier texture, boolean affectsOutline, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.ENTITIES));
	}

	// TODO(21w10a): Address getEntityAlpha
	/*@Inject(at = @At("RETURN"), method = {
		"getEntityAlpha(Lnet/minecraft/util/Identifier;F)Lnet/minecraft/client/render/RenderLayer;",
	}, cancellable = true)
	private static void iris$wrapEntityAlpha(Identifier texture, float alpha, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.ENTITIES));
	}*/

	@Inject(at = @At("RETURN"), method = {
		"getBeaconBeam(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;"
	}, cancellable = true)
	private static void iris$wrapBeaconBeam(Identifier texture, boolean affectsOutline, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.BEACON_BEAM));
	}

	@Inject(at = @At("RETURN"), method = {
		"getEyes(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
	}, cancellable = true)
	private static void iris$wrapEyes(Identifier texture, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.EYES));
	}

	@Inject(at = @At("RETURN"), method = {
		"getEnergySwirl(Lnet/minecraft/util/Identifier;FF)Lnet/minecraft/client/render/RenderLayer;"
	}, cancellable = true)
	private static void iris$wrapEnergySwirl(Identifier texture, float x, float y, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		// TODO: What render layer to use for this? It's used by charged creepers and withers.
		cir.setReturnValue(wrap(base, GbufferProgram.ENTITIES));
	}

	// TODO(21w10a): Figure out getOutline
	/*@Inject(at = @At("RETURN"), method = {
		"getOutline(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/RenderPhase$Cull;)Lnet/minecraft/client/render/RenderLayer;",
	}, cancellable = true)
	private static void iris$wrapGlowingOutline(Identifier texture, RenderPhase.Cull cull, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		// Note that instead of using GbufferProgram.ENTITIES_GLOWING here, we're using GbufferProgram.NONE. This is
		// intentional!
		//
		// Few shaderpacks implement the glowing effect correctly, if at all. The issue is that implementing it properly
		// requires using up a buffer / image. Shaderpacks can use at most 8 buffers (or, 16 on very recent versions of
		// OptiFine), so throwing away an entire buffer just for the glowing effect is a hard compromise to make.
		//
		// As it turns out, the implementation of the Glowing effect in Vanilla works fine for the most part. It uses a
		// framebuffer that is completely separate from that of shaderpack rendering, and the effect is only applied
		// once the world has finished rendering.
		//
		// TODO: Allow shaderpacks to override this if they do in fact implement the glowing effect properly
		cir.setReturnValue(wrap(base, GbufferProgram.NONE));
	}*/

	@Inject(at = @At("RETURN"), method = {
		"getBlockBreaking(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
	}, cancellable = true)
	private static void iris$wrapBlockBreakingRenderLayer(Identifier texture, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.DAMAGED_BLOCKS));
	}

	// TODO(21w10a): Figure out getEndPortal
	/*@Inject(at = @At("RETURN"), method = {
		"getEndPortal(I)Lnet/minecraft/client/render/RenderLayer;"
	}, cancellable = true)
	private static void iris$wrapEndPortalRenderLayer(int layer, CallbackInfoReturnable<RenderLayer> cir) {
		RenderLayer base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.BLOCK_ENTITIES));
	}*/
}
