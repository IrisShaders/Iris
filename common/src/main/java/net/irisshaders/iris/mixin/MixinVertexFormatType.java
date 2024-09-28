package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.irisshaders.iris.gl.program.IrisProgramTypes;
import net.irisshaders.iris.vertices.IrisFormatTypes;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL42C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VertexFormatElement.Type.class)
public class MixinVertexFormatType {
	@SuppressWarnings("target")
	@Shadow
	@Final
	@Mutable
	private static VertexFormatElement.Type[] $VALUES;

	static {
		int baseOrdinal = $VALUES.length;

		IrisFormatTypes.HALF_FLOAT
			= VertexTypeAccessor.createFormatType("HALF_FLOAT", baseOrdinal, 2, "Half Float", GL32C.GL_HALF_FLOAT);

		$VALUES = ArrayUtils.addAll($VALUES, IrisFormatTypes.HALF_FLOAT);
	}
}
