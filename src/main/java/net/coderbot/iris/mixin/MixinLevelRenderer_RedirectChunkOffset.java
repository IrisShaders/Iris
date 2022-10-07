package net.coderbot.iris.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LevelRenderer.class, priority = 1010)
public class MixinLevelRenderer_RedirectChunkOffset {
	@Redirect(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;set(FFF)V"), require = 0)
	private void iris$setChunkOffset(Uniform instance, float f, float g, float h) {
		if (RenderSystem.getShader() instanceof ExtendedShader) {
			((ExtendedShader) RenderSystem.getShader()).setChunkOffset(f, g, h);
		} else {
			instance.set(f, g, h);
			instance.upload();
		}
	}
}
