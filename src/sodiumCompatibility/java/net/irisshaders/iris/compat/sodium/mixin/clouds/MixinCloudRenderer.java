package net.irisshaders.iris.compat.sodium.mixin.clouds;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.render.immediate.CloudRenderer;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ColorVertex;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.entity_xhfp.CloudVertex;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
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
	private ShaderInstance shader;
	@Shadow
	@Final
	private FogRenderer.FogData fogData;
	@Unique
	private VertexBuffer vertexBufferWithNormals;
	@Unique
	private int prevCenterCellXIris, prevCenterCellYIris, cachedRenderDistanceIris;

	@Inject(method = "writeVertex", at = @At("HEAD"), cancellable = true, remap = false)
	private static void writeIrisVertex(long buffer, float x, float y, float z, int color, CallbackInfoReturnable<Long> cir) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			CloudVertex.write(buffer, x, y, z, color);
			cir.setReturnValue(buffer + 20L);
		}
	}

	@Shadow
	protected abstract void rebuildGeometry(BufferBuilder bufferBuilder, int cloudDistance, int centerCellX, int centerCellZ);

	@Shadow
	protected abstract void applyFogModifiers(ClientLevel world, FogRenderer.FogData fogData, LocalPlayer player, int cloudDistance, float tickDelta);

	@Shadow
	private CloudStatus cloudRenderMode;

	@Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
	private void buildIrisVertexBuffer(ClientLevel level, LocalPlayer player, PoseStack matrices, Matrix4f projectionMatrix, float ticks, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			ci.cancel();
			renderIris(level, player, matrices, projectionMatrix, ticks, tickDelta, cameraX, cameraY, cameraZ);
		}
	}

	public void renderIris(@Nullable ClientLevel level, LocalPlayer player, PoseStack matrices, Matrix4f projectionMatrix, float ticks, float tickDelta, double cameraX, double cameraY, double cameraZ) {
		if (level == null) {
			return;
		}

		float cloudHeight = level.effects().getCloudHeight();

		// Vanilla uses NaN height as a way to disable cloud rendering
		if (Float.isNaN(cloudHeight)) {
			return;
		}

		Vec3 color = level.getCloudColor(tickDelta);

		double cloudTime = (ticks + tickDelta) * 0.03F;
		double cloudCenterX = (cameraX + cloudTime);
		double cloudCenterZ = (cameraZ) + 0.33D;

		int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
		int cloudDistance = Math.max(32, (renderDistance * 2) + 9);

		int centerCellX = (int) (Math.floor(cloudCenterX / 12));
		int centerCellZ = (int) (Math.floor(cloudCenterZ / 12));

		if (this.vertexBufferWithNormals == null || this.prevCenterCellXIris != centerCellX || this.prevCenterCellYIris != centerCellZ || this.cachedRenderDistanceIris != renderDistance || cloudRenderMode != Minecraft.getInstance().options.getCloudsType()) {
			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

			this.cloudRenderMode = Minecraft.getInstance().options.getCloudsType();

			this.rebuildGeometry(bufferBuilder, cloudDistance, centerCellX, centerCellZ);

			if (this.vertexBufferWithNormals == null) {
				this.vertexBufferWithNormals = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
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
		this.fogData.end = cloudDistance * 8;
		this.fogData.start = (cloudDistance * 8) - 16;

		applyFogModifiers(level, this.fogData, player, cloudDistance * 8, tickDelta);


		RenderSystem.setShaderFogEnd(this.fogData.end);
		RenderSystem.setShaderFogStart(this.fogData.start);

		float translateX = (float) (cloudCenterX - (centerCellX * 12));
		float translateZ = (float) (cloudCenterZ - (centerCellZ * 12));

		RenderSystem.enableDepthTest();

		this.vertexBufferWithNormals.bind();

		boolean insideClouds = cameraY < cloudHeight + 4.5f && cameraY > cloudHeight - 0.5f;
		boolean fastClouds = cloudRenderMode == CloudStatus.FAST;

		if (insideClouds || fastClouds) {
			RenderSystem.disableCull();
		} else {
			RenderSystem.enableCull();
		}

		if (Minecraft.useShaderTransparency()) {
			Minecraft.getInstance().levelRenderer.getCloudsTarget().bindWrite(false);
		}

		RenderSystem.setShaderColor((float) color.x, (float) color.y, (float) color.z, 0.8f);

		matrices.pushPose();

		Matrix4f modelViewMatrix = matrices.last().pose();
		modelViewMatrix.translate(-translateX, cloudHeight - (float) cameraY + 0.33F, -translateZ);

		// PASS 1: Set up depth buffer
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(false, false, false, false);

		this.vertexBufferWithNormals.drawWithShader(modelViewMatrix, projectionMatrix, this.getClouds());

		// PASS 2: Render geometry
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.depthMask(false);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL30C.GL_EQUAL);
		RenderSystem.colorMask(true, true, true, true);

		this.vertexBufferWithNormals.drawWithShader(modelViewMatrix, projectionMatrix, this.getClouds());

		matrices.popPose();

		VertexBuffer.unbind();

		RenderSystem.disableBlend();
		RenderSystem.depthFunc(GL30C.GL_LEQUAL);

		RenderSystem.enableCull();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		if (Minecraft.useShaderTransparency()) {
			Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
		}

		RenderSystem.setShaderFogEnd(previousEnd);
		RenderSystem.setShaderFogStart(previousStart);
	}

	@ModifyArg(method = "rebuildGeometry", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;nmalloc(I)J"))
	private int allocateNewSize(int size) {
		return IrisApi.getInstance().isShaderPackInUse() ? 480 : size;
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

		if (pipeline instanceof ShaderRenderingPipeline) {
			return ((ShaderRenderingPipeline) pipeline).getShaderMap().getShader(ShaderKey.CLOUDS_SODIUM);
		}

		return shader;
	}
}
