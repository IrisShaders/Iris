package net.coderbot.iris.mixin.renderlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;

@Environment(EnvType.CLIENT)
@Mixin(RenderType.class)
public interface RenderLayerAccessor {
	@Accessor("sortOnUpload")
	boolean isTranslucent();
}
