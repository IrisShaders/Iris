package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndPortalBlockEntityRenderer.class)
public class MixinEndPortalBlockEntityRenderer {
	@Unique
	private static final float RED = 0.075f;

	@Unique
	private static final float GREEN = 0.15f;

	@Unique
	private static final float BLUE = 0.2f;

	// getHeight
	@Shadow
	protected float method_3594() {
		return 0.75F;
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void iris$onRender(EndPortalBlockEntity entity, float tickDelta, MatrixStack matrices,
							  VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
		if (!Iris.getCurrentPack().isPresent()) {
			return;
		}

		ci.cancel();

		// POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
		VertexConsumer vertexConsumer =
				vertexConsumers.getBuffer(RenderLayer.getEntitySolid(EndPortalBlockEntityRenderer.PORTAL_TEXTURE));

		Matrix4f pose = matrices.peek().getModel();
		Matrix3f normal = matrices.peek().getNormal();

		// animation with a period of 100 seconds.
		// note that texture coordinates are wrapping, not clamping.
		float progress = (SystemTimeUniforms.TIMER.getFrameTimeCounter() * 0.01f) % 1f;
		float height = method_3594();

		quad(entity, vertexConsumer, pose, normal, Direction.UP, progress, overlay, light,
				0.0f, height, 1.0f,
				1.0f, height, 1.0f,
				1.0f, height, 0.0f,
				0.0f, height, 0.0f);

		quad(entity, vertexConsumer, pose, normal, Direction.DOWN, progress, overlay, light,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 1.0f);

		quad(entity, vertexConsumer, pose, normal, Direction.NORTH, progress, overlay, light,
				0.0f, height, 0.0f,
				1.0f, height, 0.0f,
				1.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 0.0f);

		quad(entity, vertexConsumer, pose, normal, Direction.WEST, progress, overlay, light,
				0.0f, height, 1.0f,
				0.0f, height, 0.0f,
				0.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 1.0f);

		quad(entity, vertexConsumer, pose, normal, Direction.SOUTH, progress, overlay, light,
				0.0f, height, 1.0f,
				0.0f, 0.0f, 1.0f,
				1.0f, 0.0f, 1.0f,
				1.0f, height, 1.0f);

		quad(entity, vertexConsumer, pose, normal, Direction.EAST, progress, overlay, light,
				1.0f, height, 1.0f,
				1.0f, 0.0f, 1.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, height, 0.0f);
	}

	@Unique
	private void quad(EndPortalBlockEntity entity, VertexConsumer vertexConsumer, Matrix4f pose, Matrix3f normal,
					  Direction direction, float progress, int overlay, int light,
					  float x1, float y1, float z1,
					  float x2, float y2, float z2,
					  float x3, float y3, float z3,
					  float x4,float y4, float z4) {
		if (!entity.shouldDrawSide(direction)) {
			return;
		}

		float nx = direction.getOffsetX();
		float ny = direction.getOffsetY();
		float nz = direction.getOffsetZ();

		vertexConsumer.vertex(pose, x1, y1, z1).color(RED, GREEN, BLUE, 1.0f)
				.texture(0.0F + progress, 0.0F + progress).overlay(overlay).light(light)
				.normal(normal, nx, ny, nz).next();

		vertexConsumer.vertex(pose, x2, y2, z2).color(RED, GREEN, BLUE, 1.0f)
				.texture(0.0F + progress, 0.2F + progress).overlay(overlay).light(light)
				.normal(normal, nx, ny, nz).next();

		vertexConsumer.vertex(pose, x3, y3, z3).color(RED, GREEN, BLUE, 1.0f)
				.texture(0.2F + progress, 0.2F + progress).overlay(overlay).light(light)
				.normal(normal, nx, ny, nz).next();

		vertexConsumer.vertex(pose, x4, y4, z4).color(RED, GREEN, BLUE, 1.0f)
				.texture(0.2F + progress, 0.0F + progress).overlay(overlay).light(light)
				.normal(normal, nx, ny, nz).next();
	}
}
