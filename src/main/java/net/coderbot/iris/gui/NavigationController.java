package net.coderbot.iris.gui;

import net.coderbot.iris.gui.element.ShaderPackOptionList;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuContainer;

import java.util.ArrayDeque;
import java.util.Deque;

public class NavigationController {
	private final OptionMenuContainer container;
	private ShaderPackOptionList optionList;

	private String currentScreen = null;
	private final Deque<String> history = new ArrayDeque<>();

	public NavigationController(OptionMenuContainer container) {
		this.container = container;
	}

	public void back() {
		if (history.size() > 0) {
			history.removeLast();

			if (history.size() > 0) {
				currentScreen = history.getLast();
			} else {
				currentScreen = null;
			}
		} else {
			currentScreen = null;
		}

		if (optionList != null) {
			optionList.refresh();
		}
	}

	public void open(String screen) {
		currentScreen = screen;
		history.addLast(screen);

		if (optionList != null) {
			optionList.refresh();
		}
	}

	public void setActiveOptionList(ShaderPackOptionList optionList) {
		this.optionList = optionList;
	}

	public String getCurrentScreen() {
		return currentScreen;
	}
}
