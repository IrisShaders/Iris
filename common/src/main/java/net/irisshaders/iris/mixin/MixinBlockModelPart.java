package net.irisshaders.iris.mixin;

import net.irisshaders.iris.compat.general.IrisModelPart;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockModelPart.class)
public interface MixinBlockModelPart extends IrisModelPart {
}
