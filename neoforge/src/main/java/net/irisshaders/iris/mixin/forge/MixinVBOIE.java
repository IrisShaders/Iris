package net.irisshaders.iris.mixin.forge;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.programs.FallbackShader;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.minecraft.client.renderer.CompiledShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "blusunrize/immersiveengineering/client/utils/IEGLShaders", remap = false)
public class MixinVBOIE {
	@Shadow
	private static CompiledShaderProgram vboShader;

	// TODO 1.21.2: check
	@Overwrite
	public static CompiledShaderProgram getVboShader() {
		if (!Iris.isPackInUseQuick()) {
			return vboShader;
		} else {
			CompiledShaderProgram shader = ShaderAccess.getIEVBOShader();
			if (shader == null || shader instanceof FallbackShader) {
				return vboShader;
			} else {
				return shader;
			}
		}
	}
}
