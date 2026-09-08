package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiRenderer.class)
public class MixinGuiRenderer {
	@WrapMethod(method = "render")
	private void iris$bypassWorldRenderingState(Operation<Void> original) {
		boolean previous = ImmediateState.bypass;
		ImmediateState.bypass = true;
		try {
			original.call();
		} finally {
			ImmediateState.bypass = previous;
		}
	}
}
