package net.irisshaders.iris.compat.sodium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.config.builder.EnumOptionBuilderImpl")
public interface EnumOptionBuilderImplAccessor<E extends Enum<E>> {
	@Accessor("enumClass")
	Class<E> getEnumClass();
}
