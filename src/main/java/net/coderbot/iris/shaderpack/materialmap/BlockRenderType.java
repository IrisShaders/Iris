package net.coderbot.iris.shaderpack.materialmap;

import java.util.Optional;

/**
 * Defines the possible render types for blocks.
 *
 * @see <a href="https://github.com/sp614x/optifine/blob/9ea6d9b7f57f20babae64f3807b00a6ca6e1d44a/OptiFineDoc/doc/shaders.txt#L662-L681">
 *     the corresponding OptiFine documentation.</a>
 */
public enum BlockRenderType {
	/**
	 * Alpha test disabled, blending disabled, mipmaps enabled.
	 */
	SOLID,
	/**
	 * Alpha test enabled, blending disabled, mipmaps disabled.
	 * Used for tall grass, normal glass, etc.
	 */
	CUTOUT,
	/**
	 * Alpha test enabled, blending disabled, mipmaps enabled.
	 * Used for leaves.
	 */
	CUTOUT_MIPPED,
	/**
	 * Alpha test enabled (w/ low cutoff), blending enabled, mipmaps enabled.
	 * Used for stained glass, nether portals, and water.
	 */
	TRANSLUCENT;

	public static Optional<BlockRenderType> fromString(String name) {
		switch (name) {
			case "solid":
				return Optional.of(SOLID);
			case "cutout":
				return Optional.of(CUTOUT);
			case "cutout_mipped":
				return Optional.of(CUTOUT_MIPPED);
			case "translucent":
				return Optional.of(TRANSLUCENT);
			default:
				return Optional.empty();
		}
	}
}
