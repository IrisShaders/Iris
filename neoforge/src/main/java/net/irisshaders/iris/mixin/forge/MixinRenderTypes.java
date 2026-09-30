package net.irisshaders.iris.mixin.forge;

import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;

// WeatherEffectRenderer.render was reshaped to render(WeatherRenderState, RenderPass) and no
// longer calls GameRenderState.useShaderTransparency(), which no longer exists. The redirect that used to
// force rain/snow into the depth buffer has nothing left to redirect, so this mixin is now empty - the
// same thing upstream did to the Fabric copy of this class for 26.3.
@Mixin(WeatherEffectRenderer.class)
public class MixinRenderTypes {

}
