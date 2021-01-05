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
	private final OptionType type;

	public Option(String comment, List<T> allowedValues, String name, T defaultValue, OptionType type) {
		this.comment = comment == null ? "" : comment;
		this.allowedValues = allowedValues;
		this.name = name;
		this.value = defaultValue;
		if (!allowedValues.contains(defaultValue)) {
			allowedValues.add(defaultValue);
		}
		this.type = type;
	}

	/**
	 * Saves this option's value to the property along with the name of the property
	 * @param properties the properties to append to
	 */
	public void save(Properties properties) {
		properties.put(this.name, this.value.toString());
	}

	/**
	 * Sets this Option's value to the one stored in a specific {@link Properties}
	 * @param properties the properties to load from
	 * @param parser converts the string to the desired type
	 */
	public void load(Properties properties, Function<String, T> parser) {
		setValue(parser.apply(properties.getProperty(this.name)));
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

	/**
	 * Returns the type of config this option holds (boolean, int, float)
	 * @return
	 */
	public OptionType getType() {
		return type;
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
				Iris.logger.error("You currently have set {} to {}, please set it to something inside this list: {}", this.name, value, this.allowedValues);
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
