package net.coderbot.iris.compat.sodium.mixin.vertex_format.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.client.model.vertex.VertexDrain;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexSink;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import me.jellysquid.mods.sodium.client.util.ModelQuadUtil;
import me.jellysquid.mods.sodium.client.util.color.ColorARGB;
import me.jellysquid.mods.sodium.client.util.rand.XoRoShiRoRandom;
import me.jellysquid.mods.sodium.client.world.biome.ItemColorsExtended;
import me.jellysquid.mods.sodium.common.util.DirectionUtil;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
	private final XoRoShiRoRandom random = new XoRoShiRoRandom();

	@Shadow
	@Final
	private ItemColors itemColors;

	/**
	 * @reason Avoid allocations
	 * @author JellySquid
	 */
	@Overwrite
	private void renderModelLists(BakedModel model, ItemStack itemStack, int light, int overlay, PoseStack poseStack, VertexConsumer vertices) {
		XoRoShiRoRandom random = this.random;

		for (Direction direction : DirectionUtil.ALL_DIRECTIONS) {
			List<BakedQuad> quads = model.getQuads(null, direction, random.setSeedAndReturn(42L));

			if (!quads.isEmpty()) {
				this.renderQuadList(poseStack, vertices, quads, itemStack, light, overlay);
			}
		}

		List<BakedQuad> quads = model.getQuads(null, null, random.setSeedAndReturn(42L));

		if (!quads.isEmpty()) {
			this.renderQuadList(poseStack, vertices, quads, itemStack, light, overlay);
		}
	}

	/**
	 * @reason Use vertex building intrinsics
	 * @author JellySquid
	 */
	@Overwrite
	private void renderQuadList(PoseStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
		PoseStack.Pose pose = matrices.last();

		ItemColor colorProvider = null;

		QuadVertexSink drain = VertexDrain.of(vertexConsumer)
			.createSink(IrisModelVertexFormats.ENTITIES);
		drain.ensureCapacity(quads.size() * 4);

		for (BakedQuad bakedQuad : quads) {
			int color = 0xFFFFFFFF;

			if (!stack.isEmpty() && bakedQuad.isTinted()) {
				if (colorProvider == null) {
					colorProvider = ((ItemColorsExtended) this.itemColors).getColorProvider(stack);
				}

				color = ColorARGB.toABGR((colorProvider.getColor(stack, bakedQuad.getTintIndex())), 255);
			}

			ModelQuadView quad = ((ModelQuadView) bakedQuad);

			for (int i = 0; i < 4; i++) {
				drain.writeQuad(pose, quad.getX(i), quad.getY(i), quad.getZ(i), color, quad.getTexU(i), quad.getTexV(i),
					light, overlay, ModelQuadUtil.getFacingNormal(bakedQuad.getDirection()));
			}

			SpriteUtil.markSpriteActive(quad.getSprite());
		}

		drain.flush();
	}
}
