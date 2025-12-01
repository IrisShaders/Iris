package net.irisshaders.iris.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.SortedSet;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("entityRenderDispatcher")
	EntityRenderDispatcher getEntityRenderDispatcher();

	@Invoker("cullTerrain")
	void invokeCullTerrain(Camera camera, Frustum frustum, boolean spectator);

	@Accessor("level")
	ClientLevel getLevel();

	@Accessor("renderBuffers")
	RenderBuffers getRenderBuffers();

	@Accessor("renderBuffers")
	void setRenderBuffers(RenderBuffers buffers);

	@Invoker
	boolean invokeDoesMobEffectBlockSky(Camera mainCamera);

	@Accessor
	Long2ObjectMap<SortedSet<BlockDestructionProgress>> getDestructionProgress();

	@Invoker("extractVisibleBlockEntities")
	void invokeExtractBlockEntities(Camera camera, float f, LevelRenderState levelRenderState);
}
