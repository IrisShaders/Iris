package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.coderbot.iris.fantastic.VertexBufferHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
public class MixinVertexBuffer implements VertexBufferHelper {
	private static VertexBuffer current;
	private static VertexBuffer saved;

	@Shadow
	private void bind() {
		throw new IllegalStateException("not shadowed");
	}

	@Inject(method = "bind()V", at = @At("HEAD"))
	private void bindHelper(CallbackInfo ci) {
		current = (VertexBuffer) (Object) this;
	}

	@Inject(method = "unbind()V", at = @At("HEAD"))
	private static void unbindHelper(CallbackInfo ci) {
		current = null;
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
