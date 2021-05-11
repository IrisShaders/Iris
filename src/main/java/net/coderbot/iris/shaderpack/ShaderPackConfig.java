package net.coderbot.iris.shaderpack;

import static net.coderbot.iris.Iris.SHADERPACKS_DIRECTORY;

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
		shaderPackConfigPath = SHADERPACKS_DIRECTORY.resolve(name + ".txt");
		configProperties = new Properties();
		comment = "This file stores the shaderpack configuration for the shaderpack " + name;
	}

	public Properties getConfigProperties() {
		return configProperties;
	}

	//this 3 methods below should be used by the gui to get the available options and then use them
	public Iterable<Option<Boolean>> getBooleanOptions() {
		return booleanOptions.values();
	}

	public Iterable<Option<Float>> getFloatOptions() {
		return floatOptions.values();
	}

	public Iterable<Option<Integer>> getIntegerOptions() {
		return integerOptions.values();
	}

	Map<String, Option<Boolean>> getBooleanOptionsMap(){
		return booleanOptions;
	}

	Map<String, Option<Float>> getFloatOptionsMap() {
		return floatOptions;
	}

	Map<String, Option<Integer>> getIntegerOptionsMap() {
		return integerOptions;
	}

	public Option<Boolean> getBooleanOption(String key) {
		return booleanOptions.get(key);
	}

	public Option<Integer> getIntegerOption(String key) {
		return integerOptions.get(key);
	}

	public Option<Float> getFloatOption(String key) {
		return floatOptions.get(key);
	}

	public Option<Boolean> addBooleanOption(Option<Boolean> option) {

		Option<Boolean> booleanOption = booleanOptions.get(option.getName());

		//if there is already a proccessed option with the same name
		if (booleanOption != null) {
			// If the already processed option has a different value than ours, that means that we have an option that is already been modified
			if (booleanOption.getDefaultValue() != option.getDefaultValue()) {
				return booleanOption;
			}
		}

		booleanOptions.put(option.getName(), option);

		return option;
	}

	public Option<Integer> addIntegerOption(Option<Integer> option) {

		Option<Integer> integerOption = integerOptions.get(option.getName());

		//if there is already a proccessed option with the same name
		if (integerOption != null) {
			// If the already processed option has a different value than ours, that means that we have an option that is already been modified
			if (!integerOption.getDefaultValue().equals(option.getDefaultValue())) {
				return integerOption;
			}
		}
		integerOptions.put(option.getName(), option);

		return option;
	}

	public Option<Float> addFloatOption(Option<Float> option) {

		Option<Float> floatOption = floatOptions.get(option.getName());

		//if there is already a proccessed option with the same name
		if (floatOption != null) {
			// If the already processed option has a different value than ours, that means that we have an option that is already been modified
			if (!floatOption.getDefaultValue().equals(option.getDefaultValue())) {
				return floatOption;
			}
		}

		floatOptions.put(option.getName(), option);
		return floatOption;
	}

	/**
	 * Process a new option and adds it to the shader properties to be serialized
	 *
	 * @param option the option to process
	 * @param <T>    the type of the Option
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
	 *
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
	 *
	 * @throws IOException file exceptions
	 */
	public void save() throws IOException {
		configProperties.store(Files.newOutputStream(shaderPackConfigPath), comment);
	}

}
