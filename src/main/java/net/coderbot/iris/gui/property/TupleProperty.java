package net.coderbot.iris.gui.property;

import net.minecraft.text.Text;

public abstract class TupleProperty extends Property {
    public TupleProperty(Text label) {
        super(label);
    }

    public abstract Property[] getContainedProperties();
}
