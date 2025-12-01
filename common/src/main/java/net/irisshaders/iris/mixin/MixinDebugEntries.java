package net.irisshaders.iris.mixin;

import net.irisshaders.iris.gui.debug.IrisDebugEntry;
import net.irisshaders.iris.gui.debug.IrisTrueDebugEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenEntries.class)
public abstract class MixinDebugEntries {
	@Shadow
	public static ResourceLocation register(ResourceLocation resourceLocation, DebugScreenEntry debugScreenEntry) {
		return null;
	}

	@Inject(method = "<clinit>", at = @At(value = "RETURN"))
	private static void onInit(CallbackInfo ci) {
		register(ResourceLocation.fromNamespaceAndPath("iris", "iris"), new IrisDebugEntry());
		register(ResourceLocation.fromNamespaceAndPath("iris", "debug"), new IrisTrueDebugEntry());
	}
}
