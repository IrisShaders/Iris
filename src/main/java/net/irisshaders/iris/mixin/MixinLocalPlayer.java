package net.irisshaders.iris.mixin;

import net.irisshaders.iris.mixinterface.BiomeAmbienceInterface;
import net.irisshaders.iris.mixinterface.LocalPlayerInterface;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer implements LocalPlayerInterface {
	@Shadow
	@Final
	private List<AmbientSoundHandler> ambientSoundHandlers;

	@Override
	public float getCurrentConstantMood() {
		for(AmbientSoundHandler ambientSoundHandler : this.ambientSoundHandlers) {
			if (ambientSoundHandler instanceof BiomeAmbientSoundsHandler) {
				return ((BiomeAmbienceInterface)ambientSoundHandler).getConstantMood();
			}
		}

		return 0.0F;
	}
}
