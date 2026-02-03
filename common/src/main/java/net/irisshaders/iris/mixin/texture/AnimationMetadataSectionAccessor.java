package net.irisshaders.iris.mixin.texture;

import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(AnimationMetadataSection.class)
public interface AnimationMetadataSectionAccessor {
	@Accessor("frameWidth")
	Optional<Integer> getFrameWidth();

	@Mutable
	@Accessor("frameWidth")
	void setFrameWidth(Optional<Integer> frameWidth);

	@Accessor("frameHeight")
	Optional<Integer> getFrameHeight();

	@Mutable
	@Accessor("frameHeight")
	void setFrameHeight(Optional<Integer> frameHeight);
}
