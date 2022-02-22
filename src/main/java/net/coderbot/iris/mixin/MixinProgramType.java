package net.coderbot.iris.mixin;

import com.mojang.blaze3d.shaders.Program;
import net.coderbot.iris.pipeline.newshader.IrisProgramTypes;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Program.Type.class)
public class MixinProgramType {
	@SuppressWarnings("target")
    @Shadow
    @Final
    @Mutable
    private static Program.Type[] $VALUES;

    static {
        int baseOrdinal = $VALUES.length;

        IrisProgramTypes.GEOMETRY
                = ProgramTypeAccessor.createProgramType("GEOMETRY", baseOrdinal, "geometry", ".gsh", GL32C.GL_GEOMETRY_SHADER);

        $VALUES = ArrayUtils.addAll($VALUES, IrisProgramTypes.GEOMETRY);
    }
}
