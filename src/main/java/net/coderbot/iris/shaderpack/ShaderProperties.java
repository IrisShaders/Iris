package net.coderbot.iris.shaderpack;

import java.util.Properties;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.coderbot.iris.Iris;

public class ShaderProperties {
	Object2FloatMap<String> viewportScaleOverrides = new Object2FloatOpenHashMap<>();
	private final Properties properties;

	private ShaderProperties() {
		properties = ImmutableProperties.of(new Properties());
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
		this.properties = ImmutableProperties.of(properties);
	}

	public static ShaderProperties empty() {
		return new ShaderProperties();
	}

	public Properties asProperties() {
		return properties;
	}

	private static class ImmutableProperties extends Properties {
		@Override
		public Object setProperty(String key, String value) {
			throw new IllegalStateException("Cannot modify ImmutableProperties");
		}

		@Override
		public Object put(Object key, Object value) {
			throw new IllegalStateException("Cannot modify ImmutableProperties");
		}

		private void set(Properties properties) {
			for(String s : properties.stringPropertyNames()) {
				super.put(s, properties.getProperty(s));
			}
		}

		private ImmutableProperties() {}

		public static ImmutableProperties of(Properties properties) {
			ImmutableProperties i = new ImmutableProperties();
			i.set(properties);
			return i;
		}
	}
}
