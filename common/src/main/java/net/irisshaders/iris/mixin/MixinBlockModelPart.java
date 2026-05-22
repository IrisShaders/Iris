package net.irisshaders.iris.mixin;

import net.irisshaders.iris.compat.general.IrisModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockStateModelPart.class)
public interface MixinBlockModelPart extends IrisModelPart {
}
