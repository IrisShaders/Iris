package net.irisshaders.iris.mixinterface;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.pathways.HandRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.jspecify.annotations.Nullable;

public interface ItemInHandInterface {
	default void iris$renderHandsWithCustomRenderer(HandRenderer handRenderer, float tickDelta, PoseStack poseStack, SubmitNodeStorage submitNodeCollector, @Nullable LocalPlayer player, int packedLightCoords) {
		throw new AssertionError();
	}
}
