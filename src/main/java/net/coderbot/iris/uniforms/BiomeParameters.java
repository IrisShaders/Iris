package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.FloatSupplier;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.biome.Biome;

import java.util.Locale;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public class BiomeParameters {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	
	public static void biomeParameters(UniformHolder uniforms) {
		
		uniforms
				.uniform1i(PER_TICK, "biome", playerI(player ->
						BuiltinRegistries.BIOME.getRawId(player.world.getBiome(player.getBlockPos()))))
				.uniform1i(PER_TICK, "biome_category", playerI(player ->
						player.world.getBiome(player.getBlockPos()).getCategory().ordinal()))
				.uniform1i(PER_TICK, "biome_precipitation", playerI(player -> {
					Biome.Precipitation precipitation = player.world.getBiome(player.getBlockPos()).getPrecipitation();
					switch (precipitation){
						case NONE: return 0;
						case RAIN: return 1;
						case SNOW: return 2;
					}
					throw new IllegalStateException("Unknown precipitation type:" + precipitation);
				}))
				.uniform1f(PER_TICK, "rainfall", playerF(player ->
						player.world.getBiome(player.getBlockPos()).getDownfall()))
				.uniform1f(PER_TICK, "temperature", playerF(player ->
						player.world.getBiome(player.getBlockPos()).getTemperature(player.getBlockPos())))
				
				
				.uniform1i(ONCE, "PPT_NONE", () -> 0)
				.uniform1i(ONCE, "PPT_RAIN", () -> 1)
				.uniform1i(ONCE, "PPT_SNOW", () -> 2);
		
		
		
		
		addBiomes(uniforms);
		addCategories(uniforms);
		
	}
	
	public static void addBiomes(UniformHolder uniforms) {
		for (Biome biome : BuiltinRegistries.BIOME) {
			Identifier id = BuiltinRegistries.BIOME.getId(biome);
			if (id == null || !id.getNamespace().equals("minecraft")) {
				continue; // TODO: What should we do with non-standard biomes?
			}
			int rawId = BuiltinRegistries.BIOME.getRawId(biome);
			uniforms.uniform1i(ONCE, "BIOME_" + id.getPath().toUpperCase(Locale.ROOT), () -> rawId);
		}
	}
	
	public static void addCategories(UniformHolder uniforms) {
		Biome.Category[] categories = Biome.Category.values();
		for (int i = 0; i < categories.length; i++) {
			int finalI = i;
			uniforms.uniform1i(ONCE, "CAT_" + categories[i].getName().toUpperCase(Locale.ROOT), () -> finalI);
		}
	}
	
	static IntSupplier playerI(ToIntFunction<ClientPlayerEntity> function) {
		return () -> {
			ClientPlayerEntity player = client.player;
			if (player == null) {
				return 0; // TODO: I'm not sure what I'm supposed to do here?
			} else {
				return function.applyAsInt(player);
			}
		};
	}
	
	static FloatSupplier playerF(ToFloatFunction<ClientPlayerEntity> function) {
		return () -> {
			ClientPlayerEntity player = client.player;
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
