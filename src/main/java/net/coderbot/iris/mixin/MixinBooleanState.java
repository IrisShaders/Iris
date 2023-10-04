package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.BooleanStateExtended;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.BooleanState.class)
public class MixinBooleanState implements BooleanStateExtended {
	@Shadow
	@Final
	private int state;
	@Shadow
	public boolean enabled;
	@Unique
	private boolean stateUnknown;

	@Inject(method = "setEnabled", at = @At("HEAD"), cancellable = true)
	private void iris$setUnknownState(boolean enable, CallbackInfo ci) {
		if (stateUnknown) {
			ci.cancel();
			this.enabled = enable;
			stateUnknown = false;
			if (enable) {
				GL11.glEnable(this.state);
			} else {
				GL11.glDisable(this.state);
			}
		}
	}

	@Override
	public void setUnknownState() {
		stateUnknown = true;
	}
}
