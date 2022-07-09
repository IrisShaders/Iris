package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import net.caffeinemc.sodium.render.chunk.draw.MdiChunkRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MdiChunkRenderer.class, remap = false)
public class MixinDefaultChunkRenderer {
}
