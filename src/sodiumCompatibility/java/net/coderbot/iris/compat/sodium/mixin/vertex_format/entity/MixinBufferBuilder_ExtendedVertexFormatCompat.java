package net.coderbot.iris.compat.sodium.mixin.vertex_format.entity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Apply after Sodium's mixins so that we can mix in to the added method. We do this so that we have the option to
 * use the non-extended vertex format in some cases even if shaders are enabled, without assumptions in the sodium
 * compatibility code getting in the way.
 */
@Mixin(value = BufferBuilder.class, priority = 1010)
public class MixinBufferBuilder_ExtendedVertexFormatCompat {
	@Shadow
	private VertexFormat format;
}
