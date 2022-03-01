package net.coderbot.iris.mixin.rendertype;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.IrisRenderTypeWrapper;
import net.coderbot.iris.layer.UseProgramRenderStateShard;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
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

@Mixin(RenderType.class)
public class MixinRenderType {
	@Shadow
	@Final
	@Mutable
	private static RenderType SOLID;

	@Shadow
	@Final
	@Mutable
	private static RenderType CUTOUT_MIPPED;

	@Shadow
	@Final
	@Mutable
	private static RenderType CUTOUT;

	@Shadow
	@Final
	@Mutable
	private static RenderType TRANSLUCENT;

	@Shadow
	@Final
	@Mutable
	private static RenderType TRIPWIRE;

	@Unique
	private static RenderType iris$LINES;

	@Shadow @Final @Mutable private static RenderType LEASH;
	@Shadow @Final @Mutable private static RenderType ARMOR_GLINT;
	@Shadow @Final @Mutable private static RenderType ARMOR_ENTITY_GLINT;
	@Shadow @Final @Mutable private static RenderType GLINT_TRANSLUCENT;
	@Shadow @Final @Mutable private static RenderType GLINT;
	@Shadow @Final @Mutable private static RenderType GLINT_DIRECT;
	@Shadow @Final @Mutable private static RenderType ENTITY_GLINT;
	@Shadow @Final @Mutable private static RenderType ENTITY_GLINT_DIRECT;

	@Shadow @Final @Mutable private static RenderType TRANSLUCENT_MOVING_BLOCK;
	@Shadow @Final @Mutable private static RenderType LIGHTNING;

	@Shadow @Final @Mutable private static RenderType WATER_MASK;

	@Shadow @Final @Mutable private static RenderType TRANSLUCENT_NO_CRUMBLING;

	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setAlphaState(Lnet/minecraft/client/renderer/RenderStateShard$AlphaStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;"))
	private static RenderType.CompositeState.CompositeStateBuilder iris$tweakCutoutAlpha(RenderType.CompositeState.CompositeStateBuilder builder, RenderStateShard.AlphaStateShard alpha) {
		// OptiFine makes CUTOUT and CUTOUT_MIPPED use an alpha test of 0.1 instead of 0.5.
		//
		// We must replicate this behavior or else there will be issues.
		return builder.setAlphaState(new RenderStateShard.AlphaStateShard(0.1F));
	}

	static {
		SOLID = wrap("iris:terrain_solid", SOLID, GbufferProgram.TERRAIN);
		CUTOUT_MIPPED = wrap("iris:terrain_cutout_mipped", CUTOUT_MIPPED, GbufferProgram.TERRAIN);
		CUTOUT = wrap("iris:terrain_cutout", CUTOUT, GbufferProgram.TERRAIN);
		TRANSLUCENT = wrap("iris:translucent", TRANSLUCENT, GbufferProgram.TRANSLUCENT_TERRAIN);
		TRIPWIRE = wrap("iris:tripwire", TRIPWIRE, GbufferProgram.TRANSLUCENT_TERRAIN);
		// TODO: figure out how to assign to RenderType.LINES
		// We cannot use @Shadow easily because the type of the field is a package-private class
		iris$LINES = wrap("iris:lines", RenderType.LINES, GbufferProgram.LINES);

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
		GLINT_DIRECT = wrapGlint("direct", GLINT_DIRECT);
		ENTITY_GLINT = wrapGlint("entity", ENTITY_GLINT);
		ENTITY_GLINT_DIRECT = wrapGlint("direct_entity_glint", ENTITY_GLINT_DIRECT);

		LIGHTNING = wrap("iris:lightning", LIGHTNING, GbufferProgram.ENTITY_NO_OVERLAY);
	}

	/**
	 * @author coderbot
	 * @reason Use the wrapped render type instead.
	 */
	@Overwrite
	public static RenderType lines() {
		return iris$LINES;
	}

	private static RenderType wrap(String name, RenderType wrapped, GbufferProgram program) {
		return new IrisRenderTypeWrapper(name, wrapped, new UseProgramRenderStateShard(program));
	}

	private static RenderType wrapGlint(String glintType, RenderType wrapped) {
		String wrappedName = "iris:" + glintType + "_glint";

		if (glintType == null) {
			wrappedName = "iris:glint";
		}

		return wrap(wrappedName, wrapped, GbufferProgram.ARMOR_GLINT);
	}

	private static RenderType wrap(RenderType wrapped, GbufferProgram program) {
		String name = ((RenderStateShardAccessor) wrapped).getName();

		return wrap("iris:" + name, wrapped, program);
	}

	@Inject(at = @At("RETURN"), method = {
		"armorCutoutNoCull",
		"entitySolid",
		"entityCutout",
		"entitySmoothCutout",
		"entityDecal",
		"entityNoOutline",
		"entityShadow",
		"entityTranslucentCull",
		"itemEntityTranslucentCull",
		"text",
		"textSeeThrough",
	}, cancellable = true)
	private static void iris$wrapEntityRenderTypes(ResourceLocation texture, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, base.format().getElements().contains(DefaultVertexFormat.ELEMENT_UV1) ? GbufferProgram.ENTITIES : GbufferProgram.ENTITY_NO_OVERLAY));
	}

	@Inject(at = @At("RETURN"), method = {
		"entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
		"entityCutoutNoCullZOffset(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
		"entityTranslucent(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
	}, cancellable = true)
	private static void iris$wrapEntityRenderTypesZ(ResourceLocation texture, boolean affectsOutline, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, base.format().getElements().contains(DefaultVertexFormat.ELEMENT_UV1) ? GbufferProgram.ENTITIES : GbufferProgram.ENTITY_NO_OVERLAY));
	}

	@Inject(at = @At("RETURN"), method = {
		"dragonExplosionAlpha",
	}, cancellable = true)
	private static void iris$wrapEntityAlpha(ResourceLocation texture, float alpha, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, base.format().getElements().contains(DefaultVertexFormat.ELEMENT_UV1) ? GbufferProgram.ENTITIES : GbufferProgram.ENTITY_NO_OVERLAY));
	}

	@Inject(at = @At("RETURN"), method = {
		"beaconBeam"
	}, cancellable = true)
	private static void iris$wrapBeaconBeam(ResourceLocation texture, boolean affectsOutline, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.BEACON_BEAM));
	}

	@Inject(at = @At("RETURN"), method = {
		"eyes"
	}, cancellable = true)
	private static void iris$wrapEyes(ResourceLocation texture, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.EYES));
	}

	@Inject(at = @At("RETURN"), method = {
		"energySwirl"
	}, cancellable = true)
	private static void iris$wrapEnergySwirl(ResourceLocation texture, float x, float y, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		// TODO: What render type to use for this? It's used by charged creepers and withers.
		cir.setReturnValue(wrap(base, base.format().getElements().contains(DefaultVertexFormat.ELEMENT_UV1) ? GbufferProgram.ENTITIES : GbufferProgram.ENTITY_NO_OVERLAY));
	}

	@Inject(at = @At("RETURN"), method = {
		"outline(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/RenderStateShard$CullStateShard;)Lnet/minecraft/client/renderer/RenderType;",
	}, cancellable = true)
	private static void iris$wrapGlowingOutline(ResourceLocation texture, RenderStateShard.CullStateShard cull, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

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
	}

	@Inject(at = @At("RETURN"), method = {
		"crumbling"
	}, cancellable = true)
	private static void iris$wrapBlockBreakingRenderType(ResourceLocation texture, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.DAMAGED_BLOCKS));
	}

	@Inject(at = @At("RETURN"), method = {
		"endPortal"
	}, cancellable = true)
	private static void iris$wrapEndPortalRenderType(int type, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();

		cir.setReturnValue(wrap(base, GbufferProgram.BLOCK_ENTITIES));
	}
}
