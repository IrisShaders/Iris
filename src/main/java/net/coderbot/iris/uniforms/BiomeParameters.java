package net.coderbot.iris.uniforms;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.coderbot.iris.gl.uniform.FloatSupplier;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.parsing.BiomeCategories;
import net.coderbot.iris.parsing.ExtendedBiome;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
					Holder<Biome> holder = player.level.getBiome(player.blockPosition());
					ExtendedBiome extendedBiome = ((ExtendedBiome) (Object) holder.value());
					if (extendedBiome.getBiomeCategory() == -1) {
						extendedBiome.setBiomeCategory(getBiomeCategory(holder).ordinal());
						return extendedBiome.getBiomeCategory();
					} else {
						return extendedBiome.getBiomeCategory();
					}
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
						player.level.getBiome(player.blockPosition()).value().getDownfall()))
				.uniform1f(PER_TICK, "temperature", playerF(player ->
						player.level.getBiome(player.blockPosition()).value().getBaseTemperature()));
	}

	private static BiomeCategories getBiomeCategory(Holder<Biome> holder) {
		if (holder.is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
			// Literally only the void has this...
			return BiomeCategories.NONE;
		} else if (holder.is(BiomeTags.HAS_VILLAGE_SNOWY)) {
			return BiomeCategories.ICY;
		} else if (holder.is(BiomeTags.IS_HILL)) {
			return BiomeCategories.EXTREME_HILLS;
		} else if (holder.is(BiomeTags.IS_TAIGA)) {
			return BiomeCategories.TAIGA;
		} else if (holder.is(BiomeTags.IS_OCEAN)) {
			return BiomeCategories.OCEAN;
		} else if (holder.is(BiomeTags.IS_JUNGLE)) {
			return BiomeCategories.JUNGLE;
		} else if (holder.is(BiomeTags.IS_FOREST)) {
			return BiomeCategories.FOREST;
		} else if (holder.is(BiomeTags.IS_BADLANDS)) {
			return BiomeCategories.MESA;
		} else if (holder.is(BiomeTags.IS_NETHER)) {
			return BiomeCategories.NETHER;
		} else if (holder.is(BiomeTags.IS_END)) {
			return BiomeCategories.THE_END;
		} else if (holder.is(BiomeTags.IS_BEACH)) {
			return BiomeCategories.BEACH;
		} else if (holder.is(BiomeTags.HAS_DESERT_PYRAMID)) {
			return BiomeCategories.DESERT;
		} else if (holder.is(BiomeTags.IS_RIVER)) {
			return BiomeCategories.RIVER;
		} else if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
			return BiomeCategories.SWAMP;
		} else if (holder.is(BiomeTags.PLAYS_UNDERWATER_MUSIC)) {
			return BiomeCategories.UNDERGROUND;
		} else if (holder.is(BiomeTags.WITHOUT_ZOMBIE_SIEGES)) {
			return BiomeCategories.MUSHROOM;
		} else if (holder.is(BiomeTags.IS_MOUNTAIN)) {
			return BiomeCategories.MOUNTAIN;
		} else {
			return BiomeCategories.PLAINS;
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
