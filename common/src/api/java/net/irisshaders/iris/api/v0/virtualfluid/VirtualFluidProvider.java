package net.irisshaders.iris.api.v0.virtualfluid;

/**
 * Implemented by mods that expose virtual fluids to Iris.
 */
public interface VirtualFluidProvider {
	/**
	 * Called once per world frame before Iris renders translucent terrain.
	 *
	 * <p>The {@code level} and {@code camera} parameters are Objects to keep
	 * the public API independent from loader-specific source sets. Implementors
	 * can cast them to the Minecraft classes for the active runtime.</p>
	 */
	void collectVirtualFluids(Object level, Object camera, float tickDelta, VirtualFluidRegistry registry);
}
