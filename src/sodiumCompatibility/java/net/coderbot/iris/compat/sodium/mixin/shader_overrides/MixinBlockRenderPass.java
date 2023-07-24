package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TerrainRenderPass.class)
public class MixinBlockRenderPass {
}
