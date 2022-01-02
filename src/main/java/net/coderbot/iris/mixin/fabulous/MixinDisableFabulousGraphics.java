package net.coderbot.iris.mixin.fabulous;

import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.LevelRenderer;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MixinDisableFabulousGraphics {
	/**
	 * @author IMS
	 * @reason return Fancy over Fabulous if shaders are enabled
	 */
	@Overwrite
	public static boolean useShaderTransparency() {
		return Minecraft.getInstance().options.graphicsMode.getId() >= GraphicsStatus.FABULOUS.getId() && !Iris.isPackActive();
	}

	// This field is used for the F3 screen.
	@Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GraphicsStatus;toString()Ljava/lang/String;"))
	public String changeFpsString(GraphicsStatus instance) {
		if (instance == GraphicsStatus.FABULOUS && Iris.isPackActive()) {
			return "fancy";
		} else {
			return instance.toString();
		}
	}
}
