package net.coderbot.iris.shaderpack;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTestFunction;
import net.coderbot.iris.gl.blending.AlphaTestOverride;

public class ShaderProperties {
	Object2FloatMap<String> viewportScaleOverrides = new Object2FloatOpenHashMap<>();
	Object2ObjectMap<String, AlphaTestOverride> alphaTestOverrides = new Object2ObjectOpenHashMap<>();
	ObjectSet<String> blendDisabled = new ObjectOpenHashSet<>();

	private final Properties properties;

	private ShaderProperties() {
		properties = ImmutableProperties.of(new Properties());
	}

	public ShaderProperties(Properties properties) {
		properties.forEach((keyObject, valueObject) -> {
			String key = (String) keyObject;
			String value = (String) valueObject;

			handlePassDirective("scale.", key, value, pass -> {
				float scale;

				try {
					scale = Float.parseFloat(value);
				} catch (NumberFormatException e) {
					Iris.logger.error("Unable to parse scale directive for " + pass + ": " + value, e);
					return;
				}

				viewportScaleOverrides.put(pass, scale);
			});

			handlePassDirective("alphaTest.", key, value, pass -> {
				if ("off".equals(value)) {
					alphaTestOverrides.put(pass, new AlphaTestOverride.Off());
					return;
				}

				String[] parts = value.split(" ");

				if (parts.length > 2) {
					Iris.logger.warn("Weird alpha test directive for " + pass + " contains more parts than we expected: " + value);
				} else if (parts.length < 2) {
					Iris.logger.error("Invalid alpha test directive for " + pass + ": " + value);
					return;
				}

				Optional<AlphaTestFunction> function = AlphaTestFunction.fromString(parts[0]);

				if (!function.isPresent()) {
					Iris.logger.error("Unable to parse alpha test directive for " + pass + ", unknown alpha test function " + parts[0] + ": " + value);
					return;
				}

				float reference;

				try {
					reference = Float.parseFloat(parts[1]);
				} catch (NumberFormatException e) {
					Iris.logger.error("Unable to parse alpha test directive for " + pass + ": " + value, e);
					return;
				}

				alphaTestOverrides.put(pass, new AlphaTestOverride(function.get(), reference));
			});

			handlePassDirective("blend.", key, value, pass -> {
				if (pass.contains(".")) {
					Iris.logger.warn("Per-buffer pass blending directives are not supported, ignoring blend directive for " + key);
					return;
				}

				if (!"off".equals(value)) {
					Iris.logger.warn("Custom blending mode directives are not supported, ignoring blend directive for " + key);
					return;
				}

				blendDisabled.add(pass);
			});
		});
		this.properties = ImmutableProperties.of(properties);
	}

	private static void handlePassDirective(String prefix, String key, String value, Consumer<String> handler) {
		if (key.startsWith(prefix)) {
			String pass = key.substring(prefix.length());

			handler.accept(pass);
		}
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
