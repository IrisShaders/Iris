package net.irisshaders.iris.mixin.forge;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.checkerframework.checker.units.qual.A;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Mixin(ShadowRenderer.class)
public class MixinShadowRenderer {
	@Unique
	private static MethodHandle IEhandle;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void init(CallbackInfo ci) {
		try {
			IEhandle = MethodHandles.lookup().findStatic(Class.forName("blusunrize.immersiveengineering.client.utils.VertexBufferHolder"), "afterTERRendering", MethodType.methodType(void.class, RenderLevelStageEvent.class));
		} catch (Throwable e) {
			if (IrisPlatformHelpers.getInstance().isModLoaded("immersiveengineering")) {
				Iris.logger.error("Failed to load IE compatibility?", e);
			}
			IEhandle = null;
		}
	}

	@Inject(method = "renderShadows", at = @At(value = "INVOKE", target = "Lnet/irisshaders/batchedentityrendering/impl/FullyBufferedMultiBufferSource;readyUp()V"))
	private void render(LevelRendererAccessor levelRenderer, Camera playerCamera, CallbackInfo ci, @Local PoseStack modelView, @Local Matrix4f shadowProjection) {
		if (IEhandle != null) {
			try {
				// TODO: This is completely wrong. There is no reason passing an identity modelview should work here. But it does.
				IEhandle.invokeExact(new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES, Minecraft.getInstance().levelRenderer, new PoseStack(), new Matrix4f(), shadowProjection, 0, Minecraft.getInstance().getTimer(), playerCamera, ShadowRenderer.FRUSTUM));
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}
}
