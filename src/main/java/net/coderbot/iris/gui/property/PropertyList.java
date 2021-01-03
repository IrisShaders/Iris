package net.coderbot.iris.gui.property;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;

public class PropertyList extends ArrayList<Property> {
    public PropertyList(Property... properties) {
        this.addAll(ImmutableList.copyOf(properties));
    }

    public void save() {
        // TODO: Saving to file (will add Properties or similar as an arg), iterate through list members and save all ValuePropertys
        forEach(p -> {
            if(p instanceof ValueProperty) {
                // do something
            }
        });
    }
}
