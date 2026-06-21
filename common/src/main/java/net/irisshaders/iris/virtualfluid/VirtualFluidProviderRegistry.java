package net.irisshaders.iris.virtualfluid;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.virtualfluid.VirtualFluidProvider;
import net.irisshaders.iris.api.v0.virtualfluid.VirtualFluidRegistry;
import net.irisshaders.iris.api.v0.virtualfluid.VirtualFluidVolume;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class VirtualFluidProviderRegistry {
	private static final CopyOnWriteArrayList<VirtualFluidProvider> PROVIDERS = new CopyOnWriteArrayList<>();
	private static final ArrayList<VirtualFluidVolume> FRAME_VOLUMES = new ArrayList<>();
	private static final VirtualFluidRegistry FRAME_REGISTRY = volume -> {
		if (volume != null) {
			FRAME_VOLUMES.add(volume);
		}
	};
	private static final boolean DEBUG_VOLUME_ENABLED = Boolean.getBoolean("iris.debugVirtualFluid");
	private static final int DEBUG_VOLUME_MATERIAL_ID = Integer.getInteger("iris.debugVirtualFluid.materialId", 8);

	private VirtualFluidProviderRegistry() {
	}

	public static void registerProvider(VirtualFluidProvider provider) {
		PROVIDERS.addIfAbsent(Objects.requireNonNull(provider, "provider"));
	}

	public static void unregisterProvider(VirtualFluidProvider provider) {
		PROVIDERS.remove(provider);
	}

	public static void collect(Object level, Object camera, float tickDelta) {
		FRAME_VOLUMES.clear();
		collectDebugVolume(camera);
		for (VirtualFluidProvider provider : PROVIDERS) {
			try {
				provider.collectVirtualFluids(level, camera, tickDelta, FRAME_REGISTRY);
			} catch (RuntimeException e) {
				Iris.logger.warn("Virtual fluid provider failed during collection: " + provider.getClass().getName(), e);
			}
		}
	}

	private static void collectDebugVolume(Object camera) {
		if (!DEBUG_VOLUME_ENABLED || !(camera instanceof Camera minecraftCamera)) {
			return;
		}

		Vec3 pos = minecraftCamera.getPosition();
		FRAME_VOLUMES.add(new DebugVirtualFluidVolume(
			pos.x + minecraftCamera.getLookVector().x * 5.0D,
			pos.y - 1.0D,
			pos.z + minecraftCamera.getLookVector().z * 5.0D,
			DEBUG_VOLUME_MATERIAL_ID
		));
	}

	public static List<VirtualFluidVolume> currentFrameVolumes() {
		return List.copyOf(FRAME_VOLUMES);
	}

	public static void clearFrame() {
		FRAME_VOLUMES.clear();
	}
}
