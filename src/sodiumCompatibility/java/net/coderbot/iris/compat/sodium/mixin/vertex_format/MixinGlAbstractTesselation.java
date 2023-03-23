package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.gl.tessellation.GlAbstractTessellation;
import net.coderbot.iris.Iris;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlAbstractTessellation.class)
public class MixinGlAbstractTesselation {
	@Redirect(method = "bindAttributes", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL20C;glVertexAttribPointer(IIIZIJ)V"), remap = false)
	private void redirect(int index, int size, int type, boolean normalized, int stride, long pointer) {
		if (type == GL30C.GL_UNSIGNED_SHORT && size == 2 && !normalized && pointer == 16) {
			GL30C.glVertexAttribIPointer(index, size, type, stride, pointer);
		} else {
			GL30C.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
		}
	}
}
