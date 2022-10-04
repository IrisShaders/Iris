package net.coderbot.iris.compat.lambdynlights;

import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public interface ILambDynLightsCompat {
	ILambDynLightsCompat INSTANCE = getInstance();

	Optional<Integer> getLuminance(ItemStack stack, boolean submergedInWater);

	static ILambDynLightsCompat getInstance() {
		ILambDynLightsCompat instance;

		try {
			Class<?> itemLightSources = Class.forName("dev.lambdaurora.lambdynlights.api.item.ItemLightSources", true, ILambDynLightsCompat.class.getClassLoader());
			Method method = itemLightSources.getMethod("getLuminance", ItemStack.class, Boolean.class);

			instance = (stack, submergedInWater) -> {
				try {
					return Optional.of((int) method.invoke(null, stack, submergedInWater));
				} catch (IllegalAccessException | InvocationTargetException e) {
					return Optional.empty();
				}
			};
		} catch (LinkageError | ClassNotFoundException | NoSuchMethodException e) {
			instance = (stack, submergedInWater) -> Optional.empty();
		}

		return instance;
	}
}
