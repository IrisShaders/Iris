package net.coderbot.iris.gui.property;

import com.google.common.collect.ImmutableList;

import java.util.*;

public class PropertyList extends ArrayList<Property> {
    public PropertyList(Property... properties) {
        this.addAll(ImmutableList.copyOf(properties));
    }

    public void read() {
        // TODO
    }

    public void addAllPairs(List<? extends Property> ps) {
        for(int i = 0; i < ps.size(); i += 2) {
            Property left = ps.get(i);
            Property right = i + 1 < ps.size() ? ps.get(i + 1) : Property.EMPTY;
            this.add(new PairProperty(left, right));
        }
    }
}
