package net.coderbot.iris.layer;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.RenderStateShard;

public class LightningRenderStateShard extends RenderStateShard {
	public static final LightningRenderStateShard INSTANCE = new LightningRenderStateShard();
	private static int backupValue = 0;

	private static final NamespacedId LIGHT = new NamespacedId("minecraft", "lightning_bolt");

	public LightningRenderStateShard() {
		super("iris:lightning", () -> {
			if (BlockRenderingSettings.INSTANCE.getEntityIds() != null) {
				backupValue = CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
				CapturedRenderingState.INSTANCE.setCurrentEntity(BlockRenderingSettings.INSTANCE.getEntityIds().applyAsInt(LIGHT));
				GbufferPrograms.runFallbackEntityListener();
			}
		}, () -> {
			if (BlockRenderingSettings.INSTANCE.getEntityIds() != null) {
				CapturedRenderingState.INSTANCE.setCurrentEntity(backupValue);
				backupValue = 0;
				GbufferPrograms.runFallbackEntityListener();
			}
		});
	}
}
