package net.coderbot.iris.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
	@Invoker("renderLayer")
	void invokeRenderLayer(RenderLayer terrainLayer, MatrixStack modelView, double cameraX, double cameraY, double cameraZ);
}
