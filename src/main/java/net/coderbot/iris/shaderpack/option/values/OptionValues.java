package net.coderbot.iris.shaderpack.option.values;

import net.coderbot.iris.shaderpack.option.OptionSet;

import java.util.*;

public class OptionValues {
    private final OptionSet options;
    private final Set<String> flippedBooleanValues;
    private final Map<String, String> stringValues;

    public OptionValues(OptionSet options, Map<String, String> values) {
        this.options = options;
        this.flippedBooleanValues = new HashSet<>();
        this.stringValues = new HashMap<>();

        options.getBooleanOptions().forEach((name, option) -> {
            String value = values.get(name);
            boolean booleanValue;

            if (value == null) {
                return;
            }

            if (value.equals("false")) {
                booleanValue = false;
            } else if (value.equals("true")) {
                booleanValue = true;
            } else {
                // Invalid value specified, ignore it
                // TODO: Diagnostic message?
                return;
            }

            if (booleanValue != option.getOption().getDefaultValue()) {
                flippedBooleanValues.add(name);
            }
        });

        options.getStringOptions().forEach((name, option) -> {
            String value = values.get(name);

            if (value == null) {
                return;
            }

            // NB: We don't check if the option is in the allowed values here. This matches OptiFine
			//     behavior, the allowed values is only used when the user is changing options in the
			//     GUI. Profiles might specify values for options that aren't in the allowed values
			//     list, and values typed manually into the config .txt are unchecked.

            if (value.equals(option.getOption().getDefaultValue())) {
                // Ignore the value if it's a default.
                return;
            }

            stringValues.put(name, value);
        });
    }

    public boolean shouldFlip(String name) {
        return flippedBooleanValues.contains(name);
    }

    public Optional<String> getStringValue(String name) {
        return Optional.ofNullable(stringValues.get(name));
    }
}
