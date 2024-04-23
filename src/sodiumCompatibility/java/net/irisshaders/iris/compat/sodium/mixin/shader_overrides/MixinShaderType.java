package net.irisshaders.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.IrisShaderTypes;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL42C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShaderType.class)
public class MixinShaderType {
	@SuppressWarnings("target")
	@Shadow(remap = false)
	@Final
	@Mutable
	private static ShaderType[] $VALUES;

	static {
		int baseOrdinal = $VALUES.length;

		IrisShaderTypes.GEOMETRY
			= ShaderTypeAccessor.createShaderType("GEOMETRY", baseOrdinal, GL32C.GL_GEOMETRY_SHADER);
		IrisShaderTypes.TESS_CONTROL
			= ShaderTypeAccessor.createShaderType("TESS_CONTROL", baseOrdinal + 1, GL42C.GL_TESS_CONTROL_SHADER);
		IrisShaderTypes.TESS_EVAL
			= ShaderTypeAccessor.createShaderType("TESS_EVAL", baseOrdinal + 2, GL42C.GL_TESS_EVALUATION_SHADER);

		$VALUES = ArrayUtils.addAll($VALUES, IrisShaderTypes.GEOMETRY, IrisShaderTypes.TESS_CONTROL, IrisShaderTypes.TESS_EVAL);
	}
}
