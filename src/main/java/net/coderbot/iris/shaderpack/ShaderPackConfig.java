package net.coderbot.iris.shaderpack;

import static net.coderbot.iris.Iris.SHADERPACK_DIR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ShaderPackConfig {

	public final Path shaderPackConfigPath;
	private final Properties configProperties;
	private final String comment;

	private final Set<Option<Boolean>> booleanOptions = new HashSet<>();
	private final Set<Option<Float>> floatOptions = new HashSet<>();
	private final Set<Option<Integer>> integerOptions = new HashSet<>();

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
	public Iterable<Option<Boolean>> getBooleanOptions() {
		return booleanOptions;
	}

	public Iterable<Option<Float>> getFloatOptions() {
		return floatOptions;
	}

	public Iterable<Option<Integer>> getIntegerOptions() {
		return integerOptions;
	}

	void addBooleanOption(Option<Boolean> option) {
		booleanOptions.add(option);
	}

	void addIntegerOption(Option<Integer> option) {
		integerOptions.add(option);
	}

	void addFloatOption(Option<Float> option) {
		floatOptions.add(option);
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
