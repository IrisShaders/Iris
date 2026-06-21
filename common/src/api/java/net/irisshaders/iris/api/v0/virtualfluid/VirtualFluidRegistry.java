package net.irisshaders.iris.api.v0.virtualfluid;

/**
 * Per-frame sink for virtual fluid volumes.
 */
public interface VirtualFluidRegistry {
	void register(VirtualFluidVolume volume);
}
