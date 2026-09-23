package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderPass;
import net.irisshaders.iris.targets.WorldRenderTargets;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.OptionalDouble;

@Mixin(FrontendCommandEncoder.class)
public abstract class MixinWorldCommandEncoder {
	@WrapOperation(method = "createRenderPass(Lcom/mojang/renderpearl/api/commands/RenderPassDescriptor;)Lcom/mojang/renderpearl/api/commands/RenderPass;", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/api/CommandEncoderBackend;createRenderPass(Lcom/mojang/renderpearl/api/commands/RenderPassDescriptor;)Lcom/mojang/renderpearl/backend/api/RenderPassBackend;"))
	private RenderPassBackend iris$worldPass(CommandEncoderBackend backend, RenderPassDescriptor descriptor,
		Operation<RenderPassBackend> original) {
		var pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (!(pipeline instanceof IrisRenderingPipeline iris) || !iris.shouldOverrideShaders() || ImmediateState.bypass || ImmediateState.temporarilyIgnorePass) {
			return original.call(backend, descriptor);
		}

		var main = Minecraft.getInstance().gameRenderer.mainRenderTarget();

		if (descriptor.colorAttachments().isEmpty() || descriptor.colorAttachments().getFirst() == null || descriptor.colorAttachments()
			.getFirst().textureView().texture() != main.getColorTexture()) {
			return original.call(backend, descriptor);
		}

		var phase = iris.getWorldTargetPhase();
		var framebuffer = iris.getWorldTargets().getFramebuffer(phase);
		var colors = framebuffer.getColorViews();
		var depth = phase == WorldRenderTargets.Phase.SHADOW ? framebuffer.getDepthView() : descriptor.depthAttachment() == null ? null : descriptor.depthAttachment()
			.textureView();
		var size = colors.isEmpty() ? framebuffer.getDepthView() : colors.getFirst();
		var area = new RenderPass.RenderArea(0, 0, size.getWidth(0), size.getHeight(0));
		var builder = RenderPassDescriptor.builder(descriptor.label()).withRenderArea(area);

		for (var color : colors) {
			builder.withColorAttachment(color, Optional.empty());
		}

		if (depth != null) {
			builder.withDepthAttachment(depth, OptionalDouble.empty());
		}

		var commands = Iris.getRenderBackend().createRenderPass(backend, builder.build());

		return new WorldRenderPass(iris, commands, area, depth, phase);
	}
}
