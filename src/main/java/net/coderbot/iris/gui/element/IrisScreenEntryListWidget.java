package net.coderbot.iris.gui.element;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

public class IrisScreenEntryListWidget<E extends AlwaysSelectedEntryListWidget.Entry<E>> extends AlwaysSelectedEntryListWidget<E> {
	public IrisScreenEntryListWidget(MinecraftClient client, int width, int height, int top, int bottom, int left, int right, int itemHeight) {
		super(client, width, height, top, bottom, itemHeight);

		this.left = left;
		this.right = right;
	}

	@Override
	protected int getScrollbarPositionX() {
		// Position the scrollbar at the rightmost edge of the screen.
		// By default, the scrollbar is positioned moderately offset from the center.
		return width - 6;
	}

	public void select(int entry) {
		setSelected(this.getEntry(entry));
	}
}
