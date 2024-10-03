package com.seibel.distanthorizons.api.interfaces.util;

/**
 * Used for objects that need deep clones. <br>
 * Replacement for {@link Cloneable}.
 * 
 * @see Cloneable
 * 
 * @author James Seibel
 * @version 2024-7-12
 * @since API 3.0.0
 */
public interface IDhApiCopyable
{
	/** Returns a deep clone of all parameters whenever possible. */
	IDhApiCopyable copy();
	
}
