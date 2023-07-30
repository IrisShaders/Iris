package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import net.coderbot.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RegionChunkRenderer.class)
public class MixinRegionChunkRenderer {
}
