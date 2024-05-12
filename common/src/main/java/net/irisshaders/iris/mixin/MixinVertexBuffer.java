package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.irisshaders.iris.helpers.VertexBufferHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
public class MixinVertexBuffer implements VertexBufferHelper {
	private static VertexBuffer current;
	private static VertexBuffer saved;

	@Inject(method = "unbind()V", at = @At("HEAD"))
	private static void unbindHelper(CallbackInfo ci) {
		current = null;
	}

	@Shadow
	public void bind() {
		throw new IllegalStateException("not shadowed");
	}

	@Inject(method = "bind()V", at = @At("HEAD"))
	private void bindHelper(CallbackInfo ci) {
		current = (VertexBuffer) (Object) this;
	}

	@Override
	public void saveBinding() {
		saved = current;
	}

	@Override
	public void restoreBinding() {
		if (saved != null) {
			saved.bind();
		} else {
			VertexBuffer.unbind();
		}
	}
}
