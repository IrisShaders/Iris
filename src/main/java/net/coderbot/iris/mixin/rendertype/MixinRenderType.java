package net.coderbot.iris.mixin.rendertype;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.IrisRenderTypeWrapper;
import net.coderbot.iris.layer.UseProgramRenderStateShard;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderType.class)
public class MixinRenderType {
	@Unique
	private static final RenderType iris$LINES;

	@Shadow @Final @Mutable private static RenderType ARMOR_GLINT;
	@Shadow @Final @Mutable private static RenderType ARMOR_ENTITY_GLINT;
	@Shadow @Final @Mutable private static RenderType GLINT_TRANSLUCENT;
	@Shadow @Final @Mutable private static RenderType GLINT;
	@Shadow @Final @Mutable private static RenderType GLINT_DIRECT;
	@Shadow @Final @Mutable private static RenderType ENTITY_GLINT;
	@Shadow @Final @Mutable private static RenderType ENTITY_GLINT_DIRECT;

	static {
		// TODO: figure out how to assign to RenderType.LINES
		// We cannot use @Shadow easily because the type of the field is a package-private class
		iris$LINES = wrap("iris:lines", RenderType.LINES, GbufferProgram.LINES);

		ARMOR_GLINT = wrapGlint("armor", ARMOR_GLINT);
		ARMOR_ENTITY_GLINT = wrapGlint("armor_entity", ARMOR_ENTITY_GLINT);
		GLINT_TRANSLUCENT = wrapGlint("translucent", GLINT_TRANSLUCENT);
		GLINT = wrapGlint(null, GLINT);
		GLINT_DIRECT = wrapGlint("direct", GLINT_DIRECT);
		ENTITY_GLINT = wrapGlint("entity", ENTITY_GLINT);
		ENTITY_GLINT_DIRECT = wrapGlint("direct_entity_glint", ENTITY_GLINT_DIRECT);
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
		"crumbling"
	}, cancellable = true)
	private static void iris$wrapBlockBreakingRenderType(ResourceLocation texture, CallbackInfoReturnable<RenderType> cir) {
		RenderType base = cir.getReturnValue();
		cir.setReturnValue(wrap(base, GbufferProgram.DAMAGED_BLOCKS));
	}
}
