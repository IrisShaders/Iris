package net.irisshaders.iris.mixin;

import net.irisshaders.iris.parsing.ExtendedBiome;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Biome.class, priority = 990)
public class MixinBiome implements ExtendedBiome {
	@Shadow
	@Final
	private Biome.ClimateSettings climateSettings;
	private int biomeCategory = -1;

	@Override
	public int getBiomeCategory() {
		return biomeCategory;
	}

	@Override
	public void setBiomeCategory(int biomeCategory) {
		this.biomeCategory = biomeCategory;
	}

	@Override
	public float getDownfall() {
		return this.climateSettings.downfall();
	}
}
