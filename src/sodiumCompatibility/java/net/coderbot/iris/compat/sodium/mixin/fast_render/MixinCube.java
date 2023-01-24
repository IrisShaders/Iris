package net.coderbot.iris.compat.sodium.mixin.fast_render;

import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.render.ModelCuboid;
import net.minecraft.client.model.geom.ModelPart;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.Cube.class)
public class MixinCube implements ModelCuboidAccessor {
    private ModelCuboid sodium$cuboid;

	@Inject(method = "<init>", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/model/geom/ModelPart$Cube;polygons:[Lnet/minecraft/client/model/geom/ModelPart$Polygon;", ordinal = 0))
    private void onInit(int u, int v, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float extraX, float extraY, float extraZ, boolean mirror, float textureWidth, float textureHeight, CallbackInfo ci) {
        this.sodium$cuboid = new ModelCuboid(u, v, x, y, z, sizeX, sizeY, sizeZ, extraX, extraY, extraZ, mirror, textureWidth, textureHeight);
    }

    @Override
    public ModelCuboid copy() {
        return this.sodium$cuboid;
    }
}
