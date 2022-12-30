package net.coderbot.iris.mixin;

import net.coderbot.iris.parsing.ExtendedBiome;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Biome.class)
public class MixinBiome implements ExtendedBiome {
	private int biomeCategory = -1;

	@Override
	public void setBiomeCategory(int biomeCategory) {
		this.biomeCategory = biomeCategory;
	}

	@Override
	public int getBiomeCategory() {
		return biomeCategory;
	}
}
