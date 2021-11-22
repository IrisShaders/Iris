package net.coderbot.iris.compat.sodium.mixin.options;

import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Disables fog occlusion when a shader pack is enabled, since shaders are not guaranteed to actually implement fog.
 */
@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
    @Redirect(method = "setup", remap = false,
            at = @At(value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/gui/SodiumGameOptions$PerformanceSettings;useFogOcclusion:Z",
                    remap = false))
    private boolean iris$disableFogOcclusion(SodiumGameOptions.PerformanceSettings settings) {
        if (Iris.getCurrentPack().isPresent()) {
            return false;
        } else {
            return settings.useFogOcclusion;
        }
    }
}
