package net.coderbot.iris.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
	@Invoker("renderLayer")
	void invokeRenderLayer(RenderLayer terrainLayer, MatrixStack modelView, double cameraX, double cameraY, double cameraZ);

	@Invoker("setupTerrain")
	void invokeSetupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator);

	@Accessor("world")
	ClientWorld getWorld();

	@Accessor("frame")
	int getFrame();

	@Accessor("frame")
	void setFrame(int frame);
}
