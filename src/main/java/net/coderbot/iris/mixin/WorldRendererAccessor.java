package net.coderbot.iris.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
	@Accessor("entityRenderDispatcher")
	EntityRenderDispatcher getEntityRenderDispatcher();

	@Invoker("renderLayer")
	void invokeRenderLayer(RenderLayer terrainLayer, MatrixStack modelView, double cameraX, double cameraY, double cameraZ, Matrix4f projection);

	@Accessor("visibleChunks")
	ObjectArrayList<WorldRenderer.ChunkInfo> getVisibleChunks();

	@Invoker("setupTerrain")
	void invokeSetupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator);

	@Invoker("renderEntity")
	void invokeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers);

	@Accessor("world")
	ClientWorld getWorld();

	@Accessor("frame")
	int getFrame();

	@Accessor("frame")
	void setFrame(int frame);
}
