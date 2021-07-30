package net.coderbot.iris.shaderpack.option;

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

            if (!option.getAllowedValues().contains(value)) {
                // TODO: Diagnostics for this
                return;
            }

            if (value.equals(option.getDefaultValue())) {
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
