package net.coderbot.iris.mixin.rendertype;

import net.coderbot.iris.gbuffer_overrides.matching.SpecialCondition;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderStateShard.class)
public class MixinRenderStateShard_Tagging {
	@Shadow
	@Final
	protected String name;

	@Shadow
	@Final
	@Mutable
	private Runnable setupState;

	@Shadow
	@Final
	@Mutable
	private Runnable clearState;

	@Inject(method = "<init>(Ljava/lang/String;Ljava/lang/Runnable;Ljava/lang/Runnable;)V", at = @At("RETURN"))
	private void iris$onInit(String nameArg, Runnable clearStateArg, Runnable setupStateArg, CallbackInfo ci) {
		// IntelliJ is wrong here, it doesn't understand how Mixin works
		// We only want to apply this to RenderTypes.
		if (!((Object) this instanceof RenderType)) {
			return;
		}

		Runnable previousSetupState = setupState;
		Runnable previousClearState = clearState;

		// Change the Runnable instead of injecting into setup / clear state functions so that we don't add
		// unnecessary overhead if we've determined that the render type doesn't match.

		if (name.equals("beacon_beam")) {
			setupState = () -> {
				GbufferPrograms.setupSpecialRenderCondition(SpecialCondition.BEACON_BEAM);
				previousSetupState.run();
			};

			clearState = () -> {
				GbufferPrograms.teardownSpecialRenderCondition(SpecialCondition.BEACON_BEAM);
				previousClearState.run();
			};
		} else if (name.equals("eyes")) {
			setupState = () -> {
				GbufferPrograms.setupSpecialRenderCondition(SpecialCondition.ENTITY_EYES);
				previousSetupState.run();
			};

			clearState = () -> {
				GbufferPrograms.teardownSpecialRenderCondition(SpecialCondition.ENTITY_EYES);
				previousClearState.run();
			};
		} else if (name.contains("glint")) {
			// TODO: Use blend mode & depth state instead of matching on render types.
			//       That would potentially be more more robust... but more complex.
			//       So this works for now.
			setupState = () -> {
				GbufferPrograms.setupSpecialRenderCondition(SpecialCondition.GLINT);
				previousSetupState.run();
			};

			clearState = () -> {
				GbufferPrograms.teardownSpecialRenderCondition(SpecialCondition.GLINT);
				previousClearState.run();
			};
		} else if (name.contains("crumbling")) {
			// TODO: Use blend mode & depth state instead of matching on render types.
			//       That would potentially be more robust... but more complex.
			//       So this works for now.
			setupState = () -> {
				GbufferPrograms.setOverridePhase(WorldRenderingPhase.DESTROY);
				previousSetupState.run();
			};

			clearState = () -> {
				GbufferPrograms.setOverridePhase(null);
				previousClearState.run();
			};
		}
	}
}
