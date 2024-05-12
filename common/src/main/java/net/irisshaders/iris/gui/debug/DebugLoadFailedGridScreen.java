package net.irisshaders.iris.gui.debug;

import net.irisshaders.iris.Iris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;

public class DebugLoadFailedGridScreen extends Screen {
	private final Exception exception;
	private final Screen parent;

	public DebugLoadFailedGridScreen(Screen parent, Component arg, Exception exception) {
		super(arg);
		this.parent = parent;
		this.exception = exception;
	}

	@Override
	protected void init() {
		super.init();
		GridLayout widget = new GridLayout();
		LayoutSettings layoutSettings = widget.newCellSettings().alignVerticallyTop().alignHorizontallyCenter();
		LayoutSettings layoutSettings4 = widget.newCellSettings().alignVerticallyTop().paddingTop(30).alignHorizontallyCenter();
		LayoutSettings layoutSettings2 = widget.newCellSettings().alignVerticallyTop().paddingTop(30).alignHorizontallyLeft();
		LayoutSettings layoutSettings3 = widget.newCellSettings().alignVerticallyTop().paddingTop(30).alignHorizontallyRight();
		int numWidgets = 0;
		widget.addChild(new DebugTextWidget(0, 0, this.width - 80, font.lineHeight * 15, font, exception), ++numWidgets, 0, 1, 2, layoutSettings);
		widget.addChild(Button.builder(Component.translatable("menu.returnToGame"), arg2 -> this.minecraft.setScreen(parent)).width(100).build(), ++numWidgets, 0, 1, 2, layoutSettings2);
		widget.addChild(Button.builder(Component.literal("Reload pack"), arg2 -> {
			Minecraft.getInstance().setScreen(parent);
			try {
				Iris.reload();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).width(100).build(), numWidgets, 0, 1, 2, layoutSettings3);

		widget.addChild(Button.builder(Component.literal("Copy error"), arg2 -> this.minecraft.keyboardHandler.setClipboard(ExceptionUtils.getStackTrace(exception))).width(100).build(), numWidgets, 0, 1, 2, layoutSettings4);
		widget.arrangeElements();

		FrameLayout.centerInRectangle(widget, 0, 0, this.width, this.height);

		widget.visitWidgets(this::addRenderableWidget);
	}
}
