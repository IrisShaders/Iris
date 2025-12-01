package net.irisshaders.iris.mixin;

import net.minecraft.client.renderer.EndFlashState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EndFlashState.class)
public interface EndFlashAccess {
	@Accessor("yAngle")
	void setYAngle(float yAngle);

	@Accessor("xAngle")
	void setXAngle(float xAngle);
}
