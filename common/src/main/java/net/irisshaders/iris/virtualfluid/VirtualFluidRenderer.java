package net.irisshaders.iris.virtualfluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.api.v0.virtualfluid.VirtualFluidType;
import net.irisshaders.iris.api.v0.virtualfluid.VirtualFluidVolume;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class VirtualFluidRenderer {
	private static final byte FLUID_RENDER_TYPE = 1;
	private static final byte NO_EMISSION = 0;
	private static final int FULL_BRIGHT = 0xF000F0;

	private VirtualFluidRenderer() {
	}

	public static void renderBeforeDeferred() {
		if (VirtualFluidProviderRegistry.currentFrameVolumes().isEmpty()) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			return;
		}

		MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
		RenderType renderType = CustomRenderType.VIRTUAL_FLUID;
		VertexConsumer consumer = buffer.getBuffer(renderType);
		Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
		PoseStack poseStack = new PoseStack();
		poseStack.translate(-camera.x, -camera.y, -camera.z);
		PoseStack.Pose pose = poseStack.last();

		boolean rendered = false;
		for (VirtualFluidVolume volume : VirtualFluidProviderRegistry.currentFrameVolumes()) {
			if (volume.type() != VirtualFluidType.WATER_LIKE && volume.type() != VirtualFluidType.LAVA_LIKE) {
				continue;
			}
			int materialId = volume.type() == VirtualFluidType.WATER_LIKE ? 8 : volume.visualProperties().materialId();
			volume.forEachSurfaceQuad((x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, red, green, blue, alpha) -> {
				tryBeginFluidBlock(consumer, materialId, x0, y0, z0);
				Vector3f normal = computeNormal(x0, y0, z0, x1, y1, z1, x2, y2, z2);
				addVertex(consumer, pose, x0, y0, z0, red, green, blue, alpha, 0.0F, 0.0F, normal);
				addVertex(consumer, pose, x1, y1, z1, red, green, blue, alpha, 1.0F, 0.0F, normal);
				addVertex(consumer, pose, x2, y2, z2, red, green, blue, alpha, 1.0F, 1.0F, normal);
				addVertex(consumer, pose, x3, y3, z3, red, green, blue, alpha, 0.0F, 1.0F, normal);
				tryEndFluidBlock(consumer);
			});
			rendered = true;
		}

		if (rendered) {
			buffer.endBatch(renderType);
		}
	}

	private static void tryBeginFluidBlock(VertexConsumer consumer, int materialId, double x, double y, double z) {
		if (consumer instanceof BlockSensitiveBufferBuilder blockSensitive) {
			BlockPos pos = BlockPos.containing(x, y, z);
			blockSensitive.beginBlock(materialId, FLUID_RENDER_TYPE, NO_EMISSION, pos.getX(), pos.getY(), pos.getZ());
		}
	}

	private static void tryEndFluidBlock(VertexConsumer consumer) {
		if (consumer instanceof BlockSensitiveBufferBuilder blockSensitive) {
			blockSensitive.endBlock();
		}
	}

	private static void addVertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z,
								  float red, float green, float blue, float alpha, float u, float v, Vector3f normal) {
		consumer.addVertex(pose, (float) x, (float) y, (float) z)
			.setColor(red, green, blue, alpha)
			.setUv(u, v)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(FULL_BRIGHT)
			.setNormal(pose, normal.x(), normal.y(), normal.z());
	}

	private static Vector3f computeNormal(double x0, double y0, double z0,
										 double x1, double y1, double z1,
										 double x2, double y2, double z2) {
		Vector3f a = new Vector3f((float) (x1 - x0), (float) (y1 - y0), (float) (z1 - z0));
		Vector3f b = new Vector3f((float) (x2 - x0), (float) (y2 - y0), (float) (z2 - z0));
		Vector3f normal = a.cross(b, new Vector3f());
		if (normal.lengthSquared() <= 1.0E-6F) {
			return new Vector3f(0.0F, 1.0F, 0.0F);
		}
		return normal.normalize();
	}

	private static class CustomRenderType extends RenderType {
		private CustomRenderType(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
			super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
		}

		public static final RenderType VIRTUAL_FLUID = create(
			"iris_virtual_fluid",
			DefaultVertexFormat.BLOCK,
			VertexFormat.Mode.QUADS,
			1536,
			true,
			true,
			RenderType.CompositeState.builder()
				.setLightmapState(LIGHTMAP)
				.setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
				.setTextureState(BLOCK_SHEET)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setWriteMaskState(COLOR_DEPTH_WRITE)
				.createCompositeState(true)
		);
	}
}
