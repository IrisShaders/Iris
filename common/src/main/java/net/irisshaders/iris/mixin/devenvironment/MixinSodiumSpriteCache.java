package net.irisshaders.iris.mixin.devenvironment;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ResourceManager.class)
public abstract class MixinSodiumSpriteCache implements ResourceManagerReloadListener {
}
