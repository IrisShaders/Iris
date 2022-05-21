package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
	@Shadow
	private Minecraft minecraft;

	@Inject(method = "handleLogin", at = @At("TAIL"))
	private void iris$showUpdateMessage(ClientboundLoginPacket arg, CallbackInfo ci) {
		if (this.minecraft.player == null) {
			return;
		}

		Iris.getUpdateChecker().getUpdateMessage().ifPresent(msg ->
			this.minecraft.player.displayClientMessage(msg, false));
	}
}
