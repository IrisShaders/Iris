package net.coderbot.iris.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;

public class IrisObjectSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> {
	public IrisObjectSelectionList(Minecraft client, int width, int height, int top, int bottom, int left, int right, int itemHeight) {
		super(client, width, height, top, bottom, itemHeight);

		this.x0 = left;
		this.x1 = right;
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
