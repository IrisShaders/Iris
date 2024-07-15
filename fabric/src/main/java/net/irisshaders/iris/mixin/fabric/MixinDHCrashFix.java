package net.irisshaders.iris.mixin.fabric;

import com.seibel.distanthorizons.core.pos.DhChunkPos;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IMinecraftRenderWrapper;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Temporary fix for DH looking for Sodium 0.5 instead of Sodium 0.6.
 */
@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.fabric.wrappers.modAccessor.SodiumAccessor")
public class MixinDHCrashFix {
	@Shadow
	@Final
	private IMinecraftRenderWrapper MC_RENDER;

	/**
	 * @author IMS
	 * @reason see above
	 */
	@Overwrite
	public HashSet<DhChunkPos> getNormalRenderedChunks() {
		SodiumWorldRenderer renderer = SodiumWorldRenderer.instance();
		LevelHeightAccessor height = Minecraft.getInstance().level;
		return this.MC_RENDER.getMaximumRenderedChunks().stream().filter((chunk) -> {
			return renderer.isBoxVisible(chunk.getMinBlockX() + 1, height.getMinBuildHeight() + 1, chunk.getMinBlockZ() + 1, chunk.getMinBlockX() + 15, height.getMaxBuildHeight() - 1, chunk.getMinBlockZ() + 15);
		}).collect(Collectors.toCollection(HashSet::new));
	}

	@Overwrite
	public void setFogOcclusion(boolean b) {
		SodiumClientMod.options().performance.useFogOcclusion = b;
	}
}
