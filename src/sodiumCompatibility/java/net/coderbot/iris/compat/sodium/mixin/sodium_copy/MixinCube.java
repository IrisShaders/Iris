package net.coderbot.iris.compat.sodium.mixin.sodium_copy;

import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// This copy of Sodium's Mixin is required in order for the configuration disabling to work.
@Mixin(ModelPart.Cube.class)
public class MixinCube implements ModelCuboidAccessor {
    @Shadow
    @Final
    private ModelPart.Polygon[] polygons;

    @Override
    public ModelPart.Polygon[] getQuads() {
        return this.polygons;
    }
}
