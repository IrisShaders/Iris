package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.state.EndPortalRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import org.joml.Matrix3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(AbstractEndPortalRenderer.class)
public class MixinTheEndPortalRenderer {
	@Shadow
	@Final
	private static Map<Direction, List<Vector3fc>> FACES;
	@Unique
	private static final float RED = 0.075f;

	@Unique
	private static final float GREEN = 0.15f;

	@Unique
	private static final float BLUE = 0.2f;

	@ModifyArg(method = "submitCube", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V"))
	private static RenderType iris$renderType(RenderType par2) {
		if (Iris.getCurrentPack().isPresent()) {
			return (RenderTypes.entitySolid(TheEndPortalRenderer.END_PORTAL_LOCATION));
		}
		return par2;
	}

	@Inject(method = {
		"lambda$submitCube$0"
	}, at = @At("HEAD"), cancellable = true, require = 1)
	private static <T extends TheEndPortalBlockEntity> void iris$onRender(Collection<Direction> facesToShow, PoseStack.Pose pose, VertexConsumer buffer, CallbackInfo ci) {
		if (Iris.getCurrentPack().isEmpty()) {
			return;
		}

		int overlay = OverlayTexture.NO_OVERLAY;
		int light = LightCoordsUtil.FULL_BRIGHT;

		ci.cancel();

		Matrix3f normal = pose.normal();

		// animation with a period of 100 seconds.
		// note that texture coordinates are wrapping, not clamping.
		float progress = (SystemTimeUniforms.TIMER.getFrameTimeCounter() * 0.01f) % 1f;

		for (Direction direction : facesToShow) {
			float nx = direction.getStepX();
			float ny = direction.getStepY();
			float nz = direction.getStepZ();

			for (Vector3fc faceVertex : FACES.get(direction)) {
				buffer.addVertex(pose, faceVertex.x(), faceVertex.y(), faceVertex.z()).setColor(RED, GREEN, BLUE, 1.0f)
					.setUv(0.0F + progress, 0.0F + progress).setOverlay(overlay).setLight(light)
					.setNormal(pose, nx, ny, nz);
			}
		}
	}

	@Unique
	private void quad(EndPortalRenderState entity, VertexConsumer vertexConsumer, PoseStack.Pose pose, Matrix3f normal,
					  Direction direction, float progress, int overlay, int light,
					  float x1, float y1, float z1,
					  float x2, float y2, float z2,
					  float x3, float y3, float z3,
					  float x4, float y4, float z4) {
		if (!entity.facesToShow.contains(direction)) {
			return;
		}

		float nx = direction.getStepX();
		float ny = direction.getStepY();
		float nz = direction.getStepZ();

		vertexConsumer.addVertex(pose, x1, y1, z1).setColor(RED, GREEN, BLUE, 1.0f)
			.setUv(0.0F + progress, 0.0F + progress).setOverlay(overlay).setLight(light)
			.setNormal(pose, nx, ny, nz);

		vertexConsumer.addVertex(pose, x2, y2, z2).setColor(RED, GREEN, BLUE, 1.0f)
			.setUv(0.0F + progress, 0.2F + progress).setOverlay(overlay).setLight(light)
			.setNormal(pose, nx, ny, nz);

		vertexConsumer.addVertex(pose, x3, y3, z3).setColor(RED, GREEN, BLUE, 1.0f)
			.setUv(0.2F + progress, 0.2F + progress).setOverlay(overlay).setLight(light)
			.setNormal(pose, nx, ny, nz);

		vertexConsumer.addVertex(pose, x4, y4, z4).setColor(RED, GREEN, BLUE, 1.0f)
			.setUv(0.2F + progress, 0.0F + progress).setOverlay(overlay).setLight(light)
			.setNormal(pose, nx, ny, nz);
	}
}
