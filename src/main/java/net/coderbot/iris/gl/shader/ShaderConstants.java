// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.shader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShaderConstants {
    private final List<String> defines;

    private ShaderConstants(List<String> defines) {
        this.defines = defines;
    }

    public List<String> getDefineStrings() {
        return this.defines;
    }

    public static ShaderConstants fromStringList(List<String> defines) {
        Builder builder = new Builder();

        builder.defineAll(defines);

        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private static final String EMPTY_VALUE = "";

        private final HashMap<String, String> constants = new HashMap<>();

        private Builder() {

        }

        public Builder define(String name) {

			this.define(Objects.requireNonNull(name), EMPTY_VALUE);
            return this;
        }

        public Builder define(String name, String value) {
            String prev = this.constants.get(name);

            if (prev != null) {
                throw new IllegalArgumentException("Constant " + name + " is already defined with value " + prev);
            }

			this.constants.put(Objects.requireNonNull(name), Objects.requireNonNull(value));

            return this;
        }

        public Builder defineAll(List<String> names) {
        	names.forEach(this::define);
        	return this;
		}

		public Builder defineAll(Map<String, String> defines) {
        	defines.forEach(this::define);
        	return this;
		}

        public ShaderConstants build() {
            List<String> defines = new ArrayList<>(this.constants.size());

            for (Map.Entry<String, String> entry : this.constants.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (value.length() <= 0) {
                    defines.add("#define " + key);
                } else {
                    defines.add("#define " + key + " " + value);
                }
            }

            return new ShaderConstants(Collections.unmodifiableList(defines));
        }
    }
}
