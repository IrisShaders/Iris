package net.coderbot.iris.shaderpack;

import java.util.Properties;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.coderbot.iris.Iris;

public class ShaderProperties {
	Object2FloatMap<String> viewportScaleOverrides = new Object2FloatOpenHashMap<>();

	private ShaderProperties() {
		// empty
	}

	public ShaderProperties(Properties properties) {
		properties.forEach((keyObject, valueObject) -> {
			String key = (String) keyObject;
			String value = (String) valueObject;

			if (key.startsWith("scale.")) {
				String pass = key.substring("scale.".length());
				float scale;

				try {
					scale = Float.parseFloat(value);
				} catch (NumberFormatException e) {
					Iris.logger.error("Unable to parse scale directive for " + pass + ": " + value, e);
					return;
				}

				viewportScaleOverrides.put(pass, scale);
			}
		});
	}

	public static ShaderProperties empty() {
		return new ShaderProperties();
	}
}
