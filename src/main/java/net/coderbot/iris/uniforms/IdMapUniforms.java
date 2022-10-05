package net.coderbot.iris.uniforms;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.api.v0.item.IrisItemLightProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public final class IdMapUniforms {

	private IdMapUniforms() {
	}

	public static void addIdMapUniforms(FrameUpdateNotifier notifier, DynamicUniformHolder uniforms, IdMap idMap, boolean isOldHandLight) {
		HeldItemSupplier mainHandSupplier = new HeldItemSupplier(InteractionHand.MAIN_HAND, idMap.getItemIdMap(), isOldHandLight);
		HeldItemSupplier offHandSupplier = new HeldItemSupplier(InteractionHand.OFF_HAND, idMap.getItemIdMap(), isOldHandLight);
		notifier.addListener(mainHandSupplier::update);
		notifier.addListener(offHandSupplier::update);

		uniforms
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId", mainHandSupplier::getIntID)
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId2", offHandSupplier::getIntID)
			.uniform1i(PER_FRAME, "heldBlockLightValue", mainHandSupplier::getLightValue)
			.uniform1i(PER_FRAME, "heldBlockLightValue2", offHandSupplier::getLightValue);
		// TODO: Figure out API.
			//.uniformVanilla3f(PER_FRAME, "heldBlockLightColor", mainHandSupplier::getLightColor)
			//.uniformVanilla3f(PER_FRAME, "heldBlockLightColor2", offHandSupplier::getLightColor);

		uniforms.uniform1i("entityId", CapturedRenderingState.INSTANCE::getCurrentRenderedEntity,
				CapturedRenderingState.INSTANCE.getEntityIdNotifier());

		uniforms.uniform1i("blockEntityId", CapturedRenderingState.INSTANCE::getCurrentRenderedBlockEntity,
				CapturedRenderingState.INSTANCE.getBlockEntityIdNotifier());
	}

	/**
	 * Provides the currently held item, and it's light value, in the given hand as a uniform. Uses the item.properties ID map to map the item
	 * to an integer, and the old hand light value to map offhand to main hand.
	 */
	private static class HeldItemSupplier {
		private final InteractionHand hand;
		private final Object2IntFunction<NamespacedId> itemIdMap;
		private final boolean applyOldHandLight;
		private int intID;
		private int lightValue;
		private com.mojang.math.Vector3f lightColor;

		HeldItemSupplier(InteractionHand hand, Object2IntFunction<NamespacedId> itemIdMap, boolean shouldApplyOldHandLight) {
			this.hand = hand;
			this.itemIdMap = itemIdMap;
			this.applyOldHandLight = shouldApplyOldHandLight && hand == InteractionHand.MAIN_HAND;
		}

		public void update() {
			if (Minecraft.getInstance().player == null) {
				// Not valid when the player doesn't exist
				intID = -1;
				lightValue = 0;
				lightColor = IrisItemLightProvider.DEFAULT_LIGHT_COLOR;
				return;
			}

			ItemStack heldStack = Minecraft.getInstance().player.getItemInHand(hand);

			if (heldStack != null) {
				if (applyOldHandLight) {
					lightValue = ((IrisItemLightProvider) heldStack.getItem()).getLightEmission(Minecraft.getInstance().player, heldStack);
					ItemStack offHandStack = Minecraft.getInstance().player.getItemInHand(InteractionHand.OFF_HAND);
					if (lightValue < (((IrisItemLightProvider) offHandStack.getItem()).getLightEmission(Minecraft.getInstance().player, Minecraft.getInstance().player.getItemInHand(InteractionHand.OFF_HAND)))) {
						heldStack = offHandStack;
						lightValue = (((IrisItemLightProvider) offHandStack.getItem()).getLightEmission(Minecraft.getInstance().player, Minecraft.getInstance().player.getItemInHand(InteractionHand.OFF_HAND)));
					}
				} else {
					lightValue = ((IrisItemLightProvider) heldStack.getItem()).getLightEmission(Minecraft.getInstance().player, heldStack);
				}

				lightColor = ((IrisItemLightProvider) heldStack.getItem()).getLightColor(Minecraft.getInstance().player, heldStack);

				ResourceLocation heldItemId = Registry.ITEM.getKey(heldStack.getItem());
				intID = itemIdMap.applyAsInt(new NamespacedId(heldItemId.getNamespace(), heldItemId.getPath()));
			}
		}

		public int getIntID() {
			return intID;
		}

		public int getLightValue() {
			return lightValue;
		}

		public com.mojang.math.Vector3f getLightColor() {
			return lightColor;
		}
	}
}
