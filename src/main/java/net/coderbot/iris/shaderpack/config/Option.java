package net.coderbot.iris.shaderpack.config;

import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import net.coderbot.iris.Iris;

/**
 * Represents a config option
 * Stores all information about a config option
 * The instance of ShaderpackConfig should change tweak that to the value that is stored
 *
 * @param <T> the option type (boolean, int, float)
 */
public class Option<T> {
	private final String comment;
	private final List<T> allowedValues;
	private final String name;
	private T value;

	public Option(String comment, List<T> allowedValues, String name, T defaultValue) {
		this.comment = comment == null ? "" : comment;
		this.allowedValues = allowedValues;
		this.name = name;
		this.value = defaultValue;
		if (!allowedValues.contains(defaultValue)) {
			allowedValues.add(defaultValue);
		}
	}

	public void save(Properties properties) {
		properties.put(this.name, this.value.toString());
	}

	public void load(Properties properties, Function<String, T> deserializer) {
		setValue(deserializer.apply(properties.getProperty(this.name)));
	}

	public String getName() {
		return name;
	}

	public List<T> getAllowedValues() {
		return allowedValues;
	}

	public String getComment() {
		return comment;
	}

	public T getValue() {
		return value;
	}

	/**
	 * Sets the value of this config option, adhering to the rules of the allowedvalues list
	 * @param value the value to set this option to
	 */
	public void setValue(T value) {
		if (!this.allowedValues.isEmpty()) {//if it's empty then we can set it to any value, else check if the value is inside the list
			if (!this.allowedValues.contains(value)) { //if the value is not inside the list, notify the user
				//to spammy?
				//also probably have better error messages
				Iris.logger.error("Cannot set {} from option {} in a config option to {} when {} is not inside the config list (elements: {})", this.value, this.name, value, value, this.allowedValues);
				Iris.logger.warn("Please set {} to {}, please set it to something inside this list: {}", this.name, value, this.allowedValues);
				Iris.logger.warn("Not setting {} to {}", this.value, value);
				return;
			}
		}
		this.value = value;
	}

	//just for testing, probably get rid of it eventually
	@Override
	public String toString() {
		return "Option{" +
			"comment='" + comment + '\'' +
			", allowedValues=" + allowedValues +
			", name='" + name + '\'' +
			", value=" + value +
			'}';
	}
}
