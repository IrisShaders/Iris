package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
	@Invoker("shouldRenderBlockOutline")
	boolean shouldRenderBlockOutlineA();

	@Accessor("resourcePool")
	CrossFrameResourcePool getResourcePool();

	@Invoker
	void invokeBobView(CameraRenderState state, PoseStack target);

	@Invoker
	void invokeBobHurt(CameraRenderState state, PoseStack target);
}
