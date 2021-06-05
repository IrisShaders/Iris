package net.coderbot.iris.mixin.shadows;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.coderbot.iris.shadows.CullingDataCache;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer implements CullingDataCache {
	@Shadow
	@Final
	@Mutable
	private ObjectList visibleChunks;

	@Unique
	private ObjectList savedVisibleChunks = new ObjectArrayList(69696);

	@Shadow
	private boolean needsTerrainUpdate;

	@Unique
	private boolean savedNeedsTerrainUpdate;

	@Shadow
	private double lastCameraX;

	@Shadow
	private double lastCameraY;

	@Shadow
	private double lastCameraZ;

	@Shadow
	private double lastCameraPitch;

	@Shadow
	private double lastCameraYaw;

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
		ObjectList tmpList = visibleChunks;
		visibleChunks = savedVisibleChunks;
		savedVisibleChunks = tmpList;

		// TODO: If the normal chunks need a terrain update, these chunks probably do too...
		// We probably should copy it over
		boolean tmpBool = needsTerrainUpdate;
		needsTerrainUpdate = savedNeedsTerrainUpdate;
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

		tmp = lastCameraPitch;
		lastCameraPitch = savedLastCameraPitch;
		savedLastCameraPitch = tmp;

		tmp = lastCameraYaw;
		lastCameraYaw = savedLastCameraYaw;
		savedLastCameraYaw = tmp;
	}
}
