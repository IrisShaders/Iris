package net.coderbot.iris.mixin.shadows;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.coderbot.iris.shadows.CullingDataCache;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer implements CullingDataCache {
	@Shadow
	@Final
	@Mutable
	private ObjectArrayList renderChunksInFrustum;

	@Unique
	private ObjectArrayList savedRenderChunks = new ObjectArrayList(69696);

	@Shadow
	private boolean needsFullRenderChunkUpdate;

	@Unique
	private boolean savedNeedsTerrainUpdate;

	@Shadow
	private double lastCameraX;

	@Shadow
	private double lastCameraY;

	@Shadow
	private double lastCameraZ;

	@Shadow
	private double prevCamRotX;

	@Shadow
	private double prevCamRotY;

	@Unique
	private double savedLastCameraX;

	@Unique
	private double savedLastCameraY;

	@Unique
	private double savedLastCameraZ;

	@Unique
	private double savedLastCameraPitch;

	@Unique
	private double savedLastCameraYaw;

	@Override
	public void saveState() {
		swap();
	}

	@Override
	public void restoreState() {
		swap();
	}

	@Unique
	private void swap() {
		ObjectArrayList tmpList = renderChunksInFrustum;
		renderChunksInFrustum = savedRenderChunks;
		savedRenderChunks = tmpList;

		boolean tmpBool = needsFullRenderChunkUpdate;
		needsFullRenderChunkUpdate = savedNeedsTerrainUpdate;
		savedNeedsTerrainUpdate = tmpBool;

		double tmp;

		tmp = lastCameraX;
		lastCameraX = savedLastCameraX;
		savedLastCameraX = tmp;

		tmp = lastCameraY;
		lastCameraY = savedLastCameraY;
		savedLastCameraY = tmp;

		tmp = lastCameraZ;
		lastCameraZ = savedLastCameraZ;
		savedLastCameraZ = tmp;

		tmp = prevCamRotX;
		prevCamRotX = savedLastCameraPitch;
		savedLastCameraPitch = tmp;

		tmp = prevCamRotY;
		prevCamRotY = savedLastCameraYaw;
		savedLastCameraYaw = tmp;
	}
}
