package net.coderbot.iris.compat.sodium.mixin.clouds;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.client.model.vertex.VertexDrain;
import me.jellysquid.mods.sodium.client.model.vertex.formats.screen_quad.BasicScreenQuadVertexSink;
import me.jellysquid.mods.sodium.client.render.CloudRenderer;
import me.jellysquid.mods.sodium.client.util.color.ColorMixer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.ShaderKey;
import net.coderbot.iris.pipeline.newshader.fallback.WhitelistedShader;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(CloudRenderer.class)
public abstract class MixinCloudRenderer {
	@Shadow
	protected abstract void rebuildGeometry(BufferBuilder bufferBuilder, int cloudDistance, int centerCellX, int centerCellZ);

	@Shadow
	private ShaderInstance clouds;
	@Unique
	private VertexBuffer vertexBufferWithNormals;

	@Unique
	private int prevCenterCellXIris, prevCenterCellYIris, cachedRenderDistanceIris;

	@Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
	private void buildIrisVertexBuffer(ClientLevel world, PoseStack matrices, Matrix4f projectionMatrix, float ticks, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			ci.cancel();
			this.renderIris(world, matrices, projectionMatrix, ticks, tickDelta, cameraX, cameraY, cameraZ);
		}
	}

	public void renderIris(@Nullable ClientLevel world, PoseStack matrices, Matrix4f projectionMatrix, float ticks, float tickDelta, double cameraX, double cameraY, double cameraZ) {
		if (world != null) {
			Vec3 color = world.getCloudColor(tickDelta);
			float cloudHeight = world.effects().getCloudHeight();
			double cloudTime = (ticks + tickDelta) * 0.03F;
			double cloudCenterX = cameraX + cloudTime;
			double cloudCenterZ = cameraZ + 0.33;
			int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
			int cloudDistance = Math.max(32, renderDistance * 2 + 9);
			int centerCellX = (int)Math.floor(cloudCenterX / 12.0);
			int centerCellZ = (int)Math.floor(cloudCenterZ / 12.0);
			if (this.vertexBufferWithNormals == null || this.prevCenterCellXIris != centerCellX || this.prevCenterCellYIris != centerCellZ || this.cachedRenderDistanceIris != renderDistance) {
				BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
					bufferBuilder.begin(VertexFormat.Mode.QUADS, IrisVertexFormats.CLOUDS);
				this.rebuildGeometry(bufferBuilder, cloudDistance, centerCellX, centerCellZ);
				if (this.vertexBufferWithNormals == null) {
					this.vertexBufferWithNormals = new VertexBuffer();
				}

				this.vertexBufferWithNormals.bind();
				this.vertexBufferWithNormals.upload(bufferBuilder.end());
				VertexBuffer.unbind();
				this.prevCenterCellXIris = centerCellX;
				this.prevCenterCellYIris = centerCellZ;
				this.cachedRenderDistanceIris = renderDistance ;
			}

			float previousEnd = RenderSystem.getShaderFogEnd();
			float previousStart = RenderSystem.getShaderFogStart();
			RenderSystem.setShaderFogEnd((float)(cloudDistance * 8));
			RenderSystem.setShaderFogStart((float)(cloudDistance * 8 - 16));
			float translateX = (float)(cloudCenterX - (double)(centerCellX * 12));
			float translateZ = (float)(cloudCenterZ - (double)(centerCellZ * 12));
			RenderSystem.enableDepthTest();
			this.vertexBufferWithNormals.bind();
			boolean insideClouds = cameraY < (double)(cloudHeight + 4.5F) && cameraY > (double)(cloudHeight - 0.5F);
			if (insideClouds) {
				RenderSystem.disableCull();
			} else {
				RenderSystem.enableCull();
			}

			RenderSystem.disableTexture();
			RenderSystem.setShaderColor((float)color.x, (float)color.y, (float)color.z, 0.8F);
			matrices.pushPose();
			Matrix4f modelViewMatrix = matrices.last().pose();
			modelViewMatrix.translate(-translateX, cloudHeight - (float)cameraY + 0.33F, -translateZ);
			RenderSystem.disableBlend();
			RenderSystem.depthMask(true);
			RenderSystem.colorMask(false, false, false, false);
			this.vertexBufferWithNormals.drawWithShader(modelViewMatrix, projectionMatrix, getClouds());
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.depthMask(false);
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(514);
			RenderSystem.colorMask(true, true, true, true);
			this.vertexBufferWithNormals.drawWithShader(modelViewMatrix, projectionMatrix, getClouds());
			matrices.popPose();
			VertexBuffer.unbind();
			RenderSystem.disableBlend();
			RenderSystem.depthFunc(515);
			RenderSystem.enableCull();
			RenderSystem.setShaderFogEnd(previousEnd);
			RenderSystem.setShaderFogStart(previousStart);
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
