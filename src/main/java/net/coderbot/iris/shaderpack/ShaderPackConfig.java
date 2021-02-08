package net.coderbot.iris.shaderpack;

import static net.coderbot.iris.Iris.SHADERPACK_DIR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ShaderPackConfig {

	public final Path shaderPackConfigPath;
	private final Properties configProperties;
	private final String comment;

	private final Map<String, Option<Boolean>> booleanOptions = new HashMap<>();
	private final Map<String, Option<Float>> floatOptions = new HashMap<>();
	private final Map<String, Option<Integer>> integerOptions = new HashMap<>();

	public ShaderPackConfig(String name) {
		//optifine uses txt files, so we should do the same
		shaderPackConfigPath = SHADERPACK_DIR.resolve(name + ".txt");
		configProperties = new Properties();
		comment = "This file stores the shaderpack configuration for the shaderpack " + name;
	}

	public Properties getConfigProperties() {
		return configProperties;
	}

	//this 3 methods below should be used by the gui to get the available options and then use them
	public Map<String, Option<Boolean>> getBooleanOptions() {
		return booleanOptions;
	}

	public Map<String, Option<Float>> getFloatOptions() {
		return floatOptions;
	}

	public Map<String, Option<Integer>> getIntegerOptions() {
		return integerOptions;
	}

	/**
	 * Process a new option and adds it to the shader properties to be serialized
	 * @param option the option to process
	 * @param <T> the type of the Option
	 * @return a modified option that has read it's value
	 */
	public <T> Option<T> processOption(Option<T> option) {
		if (configProperties.containsKey(option.getName())) {
			option.load(configProperties);
		}
		option.save(configProperties);
		return option;
	}

	/**
	 * Loads values from properties into the configProperties field
	 * @throws IOException file exception
	 */
	public void load() throws IOException {
		if (!Files.exists(shaderPackConfigPath)) {
			return;
		}
		configProperties.load(Files.newInputStream(shaderPackConfigPath));
	}

	/**
	 * Saves the configProperties
	 * @throws IOException file exceptions
	 */
	public void save() throws IOException {
		configProperties.store(Files.newOutputStream(shaderPackConfigPath), comment);
	}

}
