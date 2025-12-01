package net.irisshaders.iris.mixin.forge;

import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.irisshaders.iris.platform.Bypass;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com/direwolf20/justdirethings/client/renderers/OurRenderTypes")
public class MixinGooBlock {
	// TODO 1.21.5 the fuck do I do
}
