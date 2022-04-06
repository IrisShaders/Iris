package net.coderbot.iris.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.option.IrisVideoSettings;

/**
 * A class dedicated to storing the config values of shaderpacks. Right now it only stores the path to the current shaderpack
 */
public class IrisConfig {
	private static final String COMMENT =
		"This file stores configuration options for Iris, such as the currently active shaderpack";

	/**
	 * The path to the current shaderpack. Null if the internal shaderpack is being used.
	 */
	private String shaderPackName;

	/**
	 * Whether or not shaders are used for rendering. False to disable all shader-based rendering, true to enable it.
	 */
	private boolean enableShaders;

	/**
	 * If debug features should be enabled. Gives much more detailed OpenGL error outputs at the cost of performance.
	 */
	private boolean enableDebug;

	private final Path propertiesPath;

	public IrisConfig(Path propertiesPath) {
		shaderPackName = null;
		enableShaders = true;
		enableDebug = false;
		this.propertiesPath = propertiesPath;
	}

	/**
	 * Initializes the configuration, loading it if it is present and creating a default config otherwise.
	 *
	 * @throws IOException file exceptions
	 */
	public void initialize() throws IOException {
		load();
		if (!Files.exists(propertiesPath)) {
			save();
		}
	}

	/**
	 * returns whether or not the current shaderpack is internal
	 *
	 * @return if the shaderpack is internal
	 */
	public boolean isInternal() {
		return false;
	}

	/**
	 * Returns the name of the current shaderpack
	 *
	 * @return Returns the current shaderpack name - if internal shaders are being used it returns "(internal)"
	 */
	public Optional<String> getShaderPackName() {
		return Optional.ofNullable(shaderPackName);
	}

	/**
	 * Sets the name of the current shaderpack
	 */
	public void setShaderPackName(String name) {
		if (name == null || name.equals("(internal)") || name.isEmpty()) {
			this.shaderPackName = null;
		} else {
			this.shaderPackName = name;
		}
	}

	/**
	 * Determines whether or not shaders are used for rendering.
	 *
	 * @return False to disable all shader-based rendering, true to enable shader-based rendering.
	 */
	public boolean areShadersEnabled() {
		return enableShaders;
	}

	public boolean isDebugEnabled() {
		return enableDebug;
	}

	public void setDebugEnabled(boolean enabled) {
		enableDebug = enabled;
	}

	/**
	 * Sets whether shaders should be used for rendering.
	 */
	public void setShadersEnabled(boolean enabled) {
		this.enableShaders = enabled;
	}

	/**
	 * loads the config file and then populates the string, int, and boolean entries with the parsed entries
	 *
	 * @throws IOException if the file cannot be loaded
	 */

	public void load() throws IOException {
		if (!Files.exists(propertiesPath)) {
			return;
		}

		Properties properties = new Properties();
		// NB: This uses ISO-8859-1 with unicode escapes as the encoding
		properties.load(Files.newInputStream(propertiesPath));
		shaderPackName = properties.getProperty("shaderPack");
		enableShaders = !"false".equals(properties.getProperty("enableShaders"));
		enableDebug = !"false".equals(properties.getProperty("enableDebug"));
		try {
			IrisVideoSettings.shadowDistance = Integer.parseInt(properties.getProperty("maxShadowRenderDistance", "32"));
		} catch (NumberFormatException e) {
			Iris.logger.error("Shadow distance setting reset; value is invalid.");
			IrisVideoSettings.shadowDistance = 32;
			save();
		}

		if (shaderPackName != null) {
			if (shaderPackName.equals("(internal)") || shaderPackName.isEmpty()) {
				shaderPackName = null;
			}
		}
	}

	/**
	 * Serializes the config into a file. Should be called whenever any config values are modified.
	 *
	 * @throws IOException file exceptions
	 */
	public void save() throws IOException {
		Properties properties = new Properties();
		properties.setProperty("shaderPack", getShaderPackName().orElse(""));
		properties.setProperty("enableShaders", enableShaders ? "true" : "false");
		properties.setProperty("enableDebug", enableDebug ? "true" : "false");
		properties.setProperty("maxShadowRenderDistance", String.valueOf(IrisVideoSettings.shadowDistance));
		// NB: This uses ISO-8859-1 with unicode escapes as the encoding
		properties.store(Files.newOutputStream(propertiesPath), COMMENT);
	}
}
