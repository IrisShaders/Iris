package net.coderbot.iris.uniforms;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.coderbot.iris.gl.uniform.FloatSupplier;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

import java.util.Locale;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public class BiomeParameters {
	private static final Object2IntMap<ResourceKey<Biome>> biomeMap = new Object2IntOpenHashMap<>();

	public static Object2IntMap<ResourceKey<Biome>> getBiomeMap() {
		return biomeMap;
	}

	public static void addBiomeUniforms(UniformHolder uniforms) {

		uniforms
			.uniform1i(PER_TICK, "biome", playerI(player ->
				biomeMap.getInt(player.level.getBiome(player.blockPosition()).unwrapKey().orElse(null))))
			.uniform1i(PER_TICK, "biome_category", playerI(player -> {
				return Biome.getBiomeCategory(player.level.getBiome(player.blockPosition())).ordinal();
			}))
			.uniform1i(PER_TICK, "biome_precipitation", playerI(player -> {
				Biome.Precipitation precipitation = player.level.getBiome(player.blockPosition()).value().getPrecipitation();
				switch (precipitation){
					case NONE: return 0;
					case RAIN: return 1;
					case SNOW: return 2;
				}
				throw new IllegalStateException("Unknown precipitation type:" + precipitation);
			}))
			.uniform1f(PER_TICK, "rainfall", playerF(player ->
				(player.level.getBiome(player.blockPosition()).value()).getDownfall()))
			.uniform1f(PER_TICK, "temperature", playerF(player ->
				player.level.getBiome(player.blockPosition()).value().getBaseTemperature()))


			.uniform1i(ONCE, "PPT_NONE", () -> 0)
			.uniform1i(ONCE, "PPT_RAIN", () -> 1)
			.uniform1i(ONCE, "PPT_SNOW", () -> 2)
			// Temporary fix for Sildur's Vibrant
			.uniform1i(ONCE, "BIOME_SWAMP_HILLS", () -> -1);




		addBiomes(uniforms);
		addCategories(uniforms);

	}

	private static void addBiomes(UniformHolder uniforms) {
		biomeMap.forEach((biome, id) -> uniforms.uniform1i(ONCE, "BIOME_" + biome.location().getPath().toUpperCase(Locale.ROOT), () -> id));
	}

	public static void addCategories(UniformHolder uniforms) {
		Biome.BiomeCategory[] categories = Biome.BiomeCategory.values();
		for (int i = 0; i < categories.length; i++) {
			int finalI = i;
			uniforms.uniform1i(ONCE, "CAT_" + categories[i].getName().toUpperCase(Locale.ROOT), () -> finalI);
		}
	}

	static IntSupplier playerI(ToIntFunction<LocalPlayer> function) {
		return () -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) {
				return 0; // TODO: I'm not sure what I'm supposed to do here?
			} else {
				return function.applyAsInt(player);
			}
		};
	}

	static FloatSupplier playerF(ToFloatFunction<LocalPlayer> function) {
		return () -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) {
				return 0.0f; // TODO: I'm not sure what I'm supposed to do here?
			} else {
				return function.applyAsFloat(player);
			}
		};
	}

	@FunctionalInterface
	public interface ToFloatFunction<T> {
		/**
		 * Applies this function to the given argument.
		 *
		 * @param value the function argument
		 * @return the function result
		 */
		float applyAsFloat(T value);
	}
}
