package net.coderbot.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.OptionalLong;
import net.minecraft.world.level.dimension.DimensionType;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {

	@Accessor
	OptionalLong getFixedTime();

}
