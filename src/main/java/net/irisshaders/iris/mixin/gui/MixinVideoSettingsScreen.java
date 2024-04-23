package net.irisshaders.iris.mixin.gui;

import net.irisshaders.iris.gui.option.IrisVideoSettings;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VideoSettingsScreen.class)
public abstract class MixinVideoSettingsScreen extends Screen {
	protected MixinVideoSettingsScreen(Component title) {
		super(title);
	}

	@ModifyArg(
		method = "init",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V"
		),
		index = 0
	)
	private OptionInstance<?>[] iris$addShaderPackScreenButton(OptionInstance<?>[] $$0) {
		OptionInstance<?>[] options = new OptionInstance[$$0.length + 2];
		System.arraycopy($$0, 0, options, 0, $$0.length);
		options[options.length - 2] = new OptionInstance<>("options.iris.shaderPackSelection", OptionInstance.cachedConstantTooltip(Component.empty()), (arg, object) -> Component.empty(), OptionInstance.BOOLEAN_VALUES, true, (parent) -> minecraft.setScreen(new ShaderPackScreen(this)));
		options[options.length - 1] = IrisVideoSettings.RENDER_DISTANCE;
		return options;
	}
}
