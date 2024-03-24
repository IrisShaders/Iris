package net.irisshaders.batchedentityrendering.impl;

public enum TransparencyType {
	/**
	 * Opaque, non transparent content.
	 */
	OPAQUE,
	/**
	 * Opaque, non transparent content that must be rendered after other opaque content, but before translucents.
	 */
	OPAQUE_DECAL,
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
	 * Water mask, should be drawn after pretty much everything except for translucent terrain and lines.
	 * Prevents water from appearing inside of boats.
	 */
	WATER_MASK,
	/**
	 * Block outlines and other debug things that are overlaid on to the world.
	 * Should be drawn last to avoid weirdness with entity shadows / banners.
	 */
	LINES
}
