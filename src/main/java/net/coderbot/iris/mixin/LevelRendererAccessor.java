package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("entityRenderDispatcher")
	EntityRenderDispatcher getEntityRenderDispatcher();

	@Accessor("renderChunks")
	ObjectList<LevelRenderer.RenderChunkInfo> getRenderChunks();

	@Invoker("renderChunkLayer")
	void invokeRenderChunkLayer(RenderType terrainLayer, PoseStack modelView, double cameraX, double cameraY, double cameraZ);

	@Invoker("setupRender")
	void invokeSetupRender(Camera camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator);

	@Invoker("renderEntity")
	void invokeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource);

	@Accessor("level")
	ClientLevel getLevel();

	@Accessor("frameId")
	int getFrameId();

	@Accessor("frameId")
	void setFrameId(int frame);

	@Accessor("renderBuffers")
	RenderBuffers getRenderBuffers();

	@Accessor("renderBuffers")
	void setRenderBuffers(RenderBuffers buffers);

	@Accessor("generateClouds")
	boolean shouldRegenerateClouds();

	@Accessor("generateClouds")
	void setShouldRegenerateClouds(boolean shouldRegenerate);
}
