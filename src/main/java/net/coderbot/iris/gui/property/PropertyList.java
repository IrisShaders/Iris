package net.coderbot.iris.gui.property;

import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.function.Consumer;

/**
 * A Property ArrayList with some utility methods.
 */
public class PropertyList extends ArrayList<Property> {
    public PropertyList(Property... properties) {
        this.addAll(ImmutableList.copyOf(properties));
    }

    /**
     * Utility method which dds a collection of
     * Properties, grouped into PairProperties.
     *
     * @param properties The Properties to add
     */
    public void addAllPairs(List<? extends Property> properties) {
        for(int i = 0; i < properties.size(); i += 2) {
            Property left = properties.get(i);
            Property right = i + 1 < properties.size() ? properties.get(i + 1) : Property.EMPTY;
            this.add(new PairProperty(left, right));
        }
    }

    /**
     * Iterates through all Properties including
     * those contained in TupleProperties. Use
     * forEach() to iterate through all properties
     * including TupleProperties.
     *
     * @param action The action to perform on each Property
     */
    public void forEvery(Consumer<? super Property> action) {
        this.forEach(p -> {
            if(p instanceof TupleProperty) {
                for(Property contained : ((TupleProperty)p).getContainedProperties()) {
                    action.accept(contained);
                }
            } else action.accept(p);
        });
    }
}
