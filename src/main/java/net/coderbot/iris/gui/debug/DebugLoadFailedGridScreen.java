package net.coderbot.iris.gui.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FrameWidget;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DebugLoadFailedGridScreen extends Screen {
	private final Exception exception;

	public DebugLoadFailedGridScreen(Component arg, Exception exception) {
		super(arg);
		this.exception = exception;
	}

	@Override
	protected void init() {
		super.init();
		GridWidget widget = new GridWidget();
		LayoutSettings layoutSettings = widget.newCellSettings().alignVerticallyTop().alignHorizontallyCenter();
		int numWidgets = 0;
		widget.addChild(new DebugTextWidget(0, 0, 1, 2, font, exception), ++numWidgets, 0, 1, 2, layoutSettings);
		widget.addChild(Button.builder(Component.translatable("menu.returnToGame"), arg2 -> {
			this.minecraft.setScreen(null);
			this.minecraft.mouseHandler.grabMouse();
		}).width(204).build(), ++numWidgets, 0, 1, 2, layoutSettings);
		widget.pack();

		FrameWidget.centerInRectangle(widget, 0, 0, this.width, this.height);

		this.addRenderableWidget(widget);
	}

	@Override
	public void render(PoseStack arg, int i, int j, float f) {
		renderBackground(arg);
		super.render(arg, i, j, f);
	}
}
