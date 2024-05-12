package net.irisshaders.iris.mixin.devenvironment;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResourceManager.class)
public abstract class MixinSodiumSpriteCache implements ResourceManagerReloadListener {
}
