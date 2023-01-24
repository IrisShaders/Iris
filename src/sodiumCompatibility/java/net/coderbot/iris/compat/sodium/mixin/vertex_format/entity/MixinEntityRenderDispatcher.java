package net.coderbot.iris.compat.sodium.mixin.vertex_format.entity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = EntityRenderDispatcher.class, priority = 1010)
public class MixinEntityRenderDispatcher {
}
