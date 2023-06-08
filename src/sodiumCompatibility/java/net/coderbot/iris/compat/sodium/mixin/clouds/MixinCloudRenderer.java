package net.coderbot.iris.compat.sodium.mixin.clouds;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.render.immediate.CloudRenderer;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ColorVertex;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.CloudVertex;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.ShaderKey;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CloudRenderer.class)
public abstract class MixinCloudRenderer {
	@Shadow
	protected abstract void rebuildGeometry(BufferBuilder bufferBuilder, int cloudDistance, int centerCellX, int centerCellZ);

	@Shadow
	private ShaderInstance clouds;

	@Shadow
	protected abstract void applyFogModifiers(ClientLevel world, FogRenderer.FogData fogData, LocalPlayer player, int cloudDistance, float tickDelta);

	@Shadow
	@Final
	private FogRenderer.FogData fogData;
	@Unique
	private VertexBuffer vertexBufferWithNormals;

	@Unique
	private int prevCenterCellXIris, prevCenterCellYIris, cachedRenderDistanceIris;

	@Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
	private void buildIrisVertexBuffer(ClientLevel world, LocalPlayer player, PoseStack matrices, Matrix4f projectionMatrix, float ticks, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			ci.cancel();
			renderIris(world, player, matrices, projectionMatrix, ticks, tickDelta, cameraX, cameraY, cameraZ);
		}
	}

	public void renderIris(@Nullable ClientLevel world, LocalPlayer player, PoseStack matrices, Matrix4f projectionMatrix, float ticks, float tickDelta, double cameraX, double cameraY, double cameraZ) {
		if (world == null) {
			return;
		}

		Vec3 color = world.getCloudColor(tickDelta);

		float cloudHeight = world.effects().getCloudHeight();

		double cloudTime = (ticks + tickDelta) * 0.03F;
		double cloudCenterX = (cameraX + cloudTime);
		double cloudCenterZ = (cameraZ) + 0.33D;

		int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
		int cloudDistance = Math.max(32, (renderDistance * 2) + 9);

		int centerCellX = (int) (Math.floor(cloudCenterX / 12));
		int centerCellZ = (int) (Math.floor(cloudCenterZ / 12));

		if (this.vertexBufferWithNormals == null || this.prevCenterCellXIris != centerCellX || this.prevCenterCellYIris != centerCellZ || this.cachedRenderDistanceIris != renderDistance) {
			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, IrisVertexFormats.CLOUDS);

			// Give some space for shaders
			this.rebuildGeometry(bufferBuilder, cloudDistance + 4, centerCellX, centerCellZ);

			if (this.vertexBufferWithNormals == null) {
				this.vertexBufferWithNormals = new VertexBuffer();
			}

			this.vertexBufferWithNormals.bind();
			this.vertexBufferWithNormals.upload(bufferBuilder.end());

			VertexBuffer.unbind();

			this.prevCenterCellXIris = centerCellX;
			this.prevCenterCellYIris = centerCellZ;
			this.cachedRenderDistanceIris = renderDistance;
		}

		float previousEnd = RenderSystem.getShaderFogEnd();
		float previousStart = RenderSystem.getShaderFogStart();
		fogData.end = cloudDistance * 8;
		fogData.start = (cloudDistance * 8) - 16;

		applyFogModifiers(world, fogData, player, cloudDistance * 8, tickDelta);


		RenderSystem.setShaderFogEnd(fogData.end);
		RenderSystem.setShaderFogStart(fogData.start);

		float translateX = (float) (cloudCenterX - (centerCellX * 12));
		float translateZ = (float) (cloudCenterZ - (centerCellZ * 12));

		RenderSystem.enableDepthTest();

		this.vertexBufferWithNormals.bind();

		boolean insideClouds = cameraY < cloudHeight + 4.5f && cameraY > cloudHeight - 0.5f;

		if (insideClouds) {
			RenderSystem.disableCull();
		} else {
			RenderSystem.enableCull();
		}

		RenderSystem.setShaderColor((float) color.x, (float) color.y, (float) color.z, 0.8f);

		matrices.pushPose();

		Matrix4f modelViewMatrix = matrices.last().pose();
		modelViewMatrix.translate(-translateX, cloudHeight - (float) cameraY + 0.33F, -translateZ);

		// PASS 1: Set up depth buffer
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(false, false, false, false);

		this.vertexBufferWithNormals.drawWithShader(modelViewMatrix, projectionMatrix, getClouds());

		// PASS 2: Render geometry
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.depthMask(false);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL30C.GL_EQUAL);
		RenderSystem.colorMask(true, true, true, true);

		this.vertexBufferWithNormals.drawWithShader(modelViewMatrix, projectionMatrix, getClouds());

		matrices.popPose();

		VertexBuffer.unbind();

		RenderSystem.disableBlend();
		RenderSystem.depthFunc(GL30C.GL_LEQUAL);

		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		RenderSystem.enableCull();

		RenderSystem.setShaderFogEnd(previousEnd);
		RenderSystem.setShaderFogStart(previousStart);
	}

	@ModifyArg(method = "rebuildGeometry", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;nmalloc(I)J"))
	private int allocateNewSize(int size) {
		return IrisApi.getInstance().isShaderPackInUse() ? 480 : size;
	}

	@Inject(method = "writeVertex", at = @At("HEAD"), cancellable = true, remap = false)
	private static void writeIrisVertex(long buffer, float x, float y, float z, int color, CallbackInfoReturnable<Long> cir) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			CloudVertex.write(buffer, x, y, z, color);
			cir.setReturnValue(buffer + 20L);
		}
	}

	@ModifyArg(method = "rebuildGeometry", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;push(Lorg/lwjgl/system/MemoryStack;JILnet/caffeinemc/mods/sodium/api/vertex/format/VertexFormatDescription;)V"), index = 3)
	private VertexFormatDescription modifyArgIris(VertexFormatDescription vertexFormatDescription) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return CloudVertex.FORMAT;
		} else {
			return ColorVertex.FORMAT;
		}
	}

	private ShaderInstance getClouds() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			return ((CoreWorldRenderingPipeline) pipeline).getShaderMap().getShader(ShaderKey.CLOUDS_SODIUM);
		}

		return clouds;
	}
}
