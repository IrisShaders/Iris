package net.coderbot.iris.mixin.texture;

import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnimationMetadataSection.class)
public interface AnimationMetadataSectionAccessor {
	@Accessor("frameWidth")
	int getFrameWidth();

	@Accessor("frameWidth")
	void setFrameWidth(int frameWidth);

	@Accessor("frameHeight")
	int getFrameHeight();

	@Accessor("frameHeight")
	void setFrameHeight(int frameHeight);
}
