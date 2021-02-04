package net.coderbot.iris.shaderpack.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	private final T defaultValue;

	public Option(String comment, List<T> allowedValues, String name, T defaultValue, OptionType type) {
		this.comment = comment == null ? "" : comment.trim();
		this.allowedValues = allowedValues;
		this.name = name;
		this.value = defaultValue;
		//only add to the list if it is already not empty.
		//If it is empty, then we know that it can be set to any value
		if (!allowedValues.contains(defaultValue) && !allowedValues.isEmpty()) {
			allowedValues.add(defaultValue);
		}
		this.type = type;
		this.defaultValue = defaultValue;
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

	/**
	 * Returns the list of allowed values that {@link Option#value can be set to}
	 * Please check if a value is in this list before calling {@link Option#setValue(Object)}
	 *
	 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.properties#L132">Gui Note: Left and Right Clicking</a>
	 *
	 * @return the list of allowed values
	 */
	public List<T> getAllowedValues() {
		return allowedValues;
	}

	/**
	 * Returns the comment of this option that is split based on the regex for ". "
	 *
	 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.properties#L128">Optifine Doc For Tooltip</a>
	 *
	 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.properties#L129">Gui Note: Tooltip Coloring</a>
	 * @return comment that is split sentences
	 */
	public List<String> getComment() {
		return Arrays.stream(comment.split("\\.\\s")).map(string -> string.endsWith(".") ? string.substring(0, string.length() - 1) : string).collect(Collectors.toList());//remove periods from the end, idk if needed
	}

	/**
	 * Returns the type of config this option holds (boolean, int, float)
	 * @return current option type
	 */
	public OptionType getType() {
		return type;
	}

	/**
	 * Returns the current value of this option
	 * @return current value
	 */
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
				//TODO maybe throw an exception instead?
				Iris.logger.error("Cannot set {} from option {} in a config option to {} when {} is not inside the config list (elements: {})", this.value, this.name, value, value, this.allowedValues);
				Iris.logger.error("You currently have set {} to {}, please set it to something inside this list: {}", this.name, value, this.allowedValues);
				return;
			}
		}
		this.value = value;
	}

	/**
	 * Returns the Default Value of this option
	 *
	 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.properties#L133">Gui Note: Shift Clicking</a>
	 *
	 * @return the default value
	 */
	public T getDefaultValue() {
		return defaultValue;
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

	public enum OptionType {
		INTEGER,
		BOOLEAN,
		FLOAT
	}
}
