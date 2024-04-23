package net.irisshaders.iris.mixin.texture;

import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnimationMetadataSection.class)
public interface AnimationMetadataSectionAccessor {
	@Accessor("frameWidth")
	int getFrameWidth();

	@Mutable
	@Accessor("frameWidth")
	void setFrameWidth(int frameWidth);

	@Accessor("frameHeight")
	int getFrameHeight();

	@Mutable
	@Accessor("frameHeight")
	void setFrameHeight(int frameHeight);
}
