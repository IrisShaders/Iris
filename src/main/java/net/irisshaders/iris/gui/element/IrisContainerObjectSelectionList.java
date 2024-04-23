package net.irisshaders.iris.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public class IrisContainerObjectSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> {
	public IrisContainerObjectSelectionList(Minecraft client, int width, int height, int top, int bottom, int left, int right, int itemHeight) {
		super(client, width, height, top, itemHeight);
	}

	@Override
	protected int getScrollbarPosition() {
		// Position the scrollbar at the rightmost edge of the screen.
		// By default, the scrollbar is positioned moderately offset from the center.
		return width - 6;
	}

	public void select(int entry) {
		setSelected(this.getEntry(entry));
	}
}
