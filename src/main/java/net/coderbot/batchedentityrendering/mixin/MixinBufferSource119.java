package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(MultiBufferSource.BufferSource.class)
public class MixinBufferSource119 {
	// TODO: Figure out the cause of this being null in the first place.
	@Redirect(method = "getBuffer", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false), remap = false)
	private boolean iris$fixToastItems(Set<BufferBuilder> instance, Object e) {
		return e != null && instance.add((BufferBuilder) e);
	}
}
