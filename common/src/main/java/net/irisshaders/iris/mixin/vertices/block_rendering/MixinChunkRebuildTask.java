package net.irisshaders.iris.mixin.vertices.block_rendering;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Captures and tracks the current block being rendered.
 * <p>
 * Uses a priority of 999 so that we apply before Indigo's mixins.
 */
@Mixin(value = SectionRenderDispatcher.RenderSection.RebuildTask.class, priority = 999)
public class MixinChunkRebuildTask {
	// Resolve the ID map on the main thread to avoid thread safety issues
	@Unique
	private final Object2IntMap<BlockState> blockStateIds = getBlockStateIds();

	@Unique
	private Object2IntMap<BlockState> getBlockStateIds() {
		return WorldRenderingSettings.INSTANCE.getBlockStateIds();
	}

	// TODO: consider adding this back.
}
