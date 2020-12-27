package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.VideoOptionsScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoOptionsScreen.class)
@Environment(EnvType.CLIENT)
public class MixinVideoOptionsScreen extends Screen {

	protected MixinVideoOptionsScreen(Text title) {
		super(title);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void renderActiveShaderName(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
		textRenderer.draw(matrices, "Active Shader: " + Iris.getIrisConfig().getShaderPackName(), 0, 0, 0xFF888888);
	}
}
