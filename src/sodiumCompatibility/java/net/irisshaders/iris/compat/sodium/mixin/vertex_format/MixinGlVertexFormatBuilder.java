package net.irisshaders.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttribute;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.EnumMap;

@Mixin(GlVertexFormat.Builder.class)
public class MixinGlVertexFormatBuilder<T extends Enum<T>> {
	private static final GlVertexAttribute EMPTY
		= new GlVertexAttribute(GlVertexAttributeFormat.FLOAT, 0, false, 0, 0, false);
	@Shadow
	@Final
	private int stride;
	@Shadow
	@Final
	private EnumMap<T, GlVertexAttribute> attributes;

	@Redirect(method = "build",
		at = @At(value = "INVOKE",
			target = "java/util/EnumMap.get (Ljava/lang/Object;)Ljava/lang/Object;"),
		remap = false)
	private Object iris$suppressMissingAttributes(EnumMap<?, ?> map, Object key) {
		Object value = map.get(key);

		if (value == null) {
			// Missing these attributes is acceptable and will be handled properly.
			return EMPTY;
		}

		return value;
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite(remap = false)
	private GlVertexFormat.Builder<T> addElement(T type, GlVertexAttribute attribute) {
		if (this.attributes.put(type, attribute) != null) {
			throw new IllegalStateException("Generic attribute " + type.name() + " already defined in vertex format");
		} else {
			return (GlVertexFormat.Builder<T>) (Object) this;
		}
	}
}
