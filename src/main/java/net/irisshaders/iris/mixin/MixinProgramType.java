package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.shaders.Program;
import net.irisshaders.iris.gl.program.IrisProgramTypes;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL42C;
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

		IrisProgramTypes.TESS_CONTROL
			= ProgramTypeAccessor.createProgramType("TESS_CONTROL", baseOrdinal + 1, "tess_control", ".tcs", GL42C.GL_TESS_CONTROL_SHADER);

		IrisProgramTypes.TESS_EVAL
			= ProgramTypeAccessor.createProgramType("TESS_EVAL", baseOrdinal + 2, "tess_eval", ".tes", GL42C.GL_TESS_EVALUATION_SHADER);

		$VALUES = ArrayUtils.addAll($VALUES, IrisProgramTypes.GEOMETRY, IrisProgramTypes.TESS_CONTROL, IrisProgramTypes.TESS_EVAL);
	}
}
