package net.irisshaders.iris.mixin.devenvironment;

import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ProfileKeyPairManager.class)
public interface MixinProfileKeyPairManager {
}
