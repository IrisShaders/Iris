package net.irisshaders.iris.api.v0.virtualfluid;

/**
 * Describes the broad shader semantics a virtual fluid volume wants.
 *
 * <p>This intentionally does not describe a Minecraft {@code FluidState}. A
 * virtual fluid can be backed by a block entity, tank renderer, multiblock, or
 * another client-side structure that has no real chunk fluid state.</p>
 */
public enum VirtualFluidType {
	WATER_LIKE,
	LAVA_LIKE,
	CUSTOM
}
