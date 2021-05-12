package net.coderbot.iris.mixin.gui;

import net.coderbot.iris.gui.option.ShaderPackSelectionButtonOption;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.VideoOptionsScreen;
import net.minecraft.client.options.Option;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VideoOptionsScreen.class)
public abstract class MixinVideoOptionsScreen extends Screen {
	protected MixinVideoOptionsScreen(Text title) {
		super(title);
	}

	@ModifyArg(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/widget/ButtonListWidget;addAll([Lnet/minecraft/client/options/Option;)V"
			),
			index = 0
	)
	private Option[] iris$addShaderPackScreenButton(Option[] old) {
		Option[] options = new Option[old.length + 1];
		System.arraycopy(old, 0, options, 0, old.length);
		options[options.length - 1] = new ShaderPackSelectionButtonOption((VideoOptionsScreen)(Object)this, this.client);
		return options;
	}
}
