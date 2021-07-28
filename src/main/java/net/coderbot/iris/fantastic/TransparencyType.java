package net.coderbot.iris.fantastic;

public enum TransparencyType {
	/**
	 * Opaque, non transparent content.
	 */
	OPAQUE,
	/**
	 * Generally transparent / translucent content.
	 */
	GENERAL_TRANSPARENT,
	/**
	 * Enchantment glint and crumbling blocks
	 * These *must* be rendered after their corresponding opaque / transparent parts.
	 */
	DECAL,
	/**
	 * Water mask, must be drawn after all other things.
	 * Prevents water from appearing inside of boats.
	 */
	WATER_MASK
}
