package net.irisshaders.iris.api.v0.virtualfluid;

/**
 * Optional visual metadata for a virtual fluid volume.
 *
 * <p>Values are intentionally primitive so the API can stay stable across
 * loader and Minecraft mapping changes.</p>
 */
public record VirtualFluidVisualProperties(
	String fluidId,
	int materialId,
	float fogRed,
	float fogGreen,
	float fogBlue,
	float fogDensity,
	float tintRed,
	float tintGreen,
	float tintBlue,
	float alpha,
	float emissionRed,
	float emissionGreen,
	float emissionBlue,
	float emissionStrength,
	boolean useWaterCaustics,
	boolean useWaterRefraction
) {
	public static VirtualFluidVisualProperties waterLike(String fluidId, int materialId) {
		return new VirtualFluidVisualProperties(
			fluidId,
			materialId,
			0.18F, 0.42F, 0.55F,
			1.0F,
			1.0F, 1.0F, 1.0F,
			1.0F,
			0.0F, 0.0F, 0.0F,
			0.0F,
			true,
			true
		);
	}
}
