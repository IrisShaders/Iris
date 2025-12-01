package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
	@Accessor
	boolean getPanoramicMode();

	@Invoker
	void invokeBobView(PoseStack poseStack, float tickDelta);

	@Invoker
	void invokeBobHurt(PoseStack poseStack, float tickDelta);

	@Invoker
	float invokeGetFov(Camera camera, float tickDelta, boolean b);

	@Invoker("shouldRenderBlockOutline")
	boolean shouldRenderBlockOutlineA();

	@Accessor("resourcePool")
	CrossFrameResourcePool getResourcePool();
}
