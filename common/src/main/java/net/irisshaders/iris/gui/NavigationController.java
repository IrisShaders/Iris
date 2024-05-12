package net.irisshaders.iris.gui;

import net.irisshaders.iris.gui.element.ShaderPackOptionList;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuContainer;

import java.util.ArrayDeque;
import java.util.Deque;

public class NavigationController {
	private final OptionMenuContainer container;
	private final Deque<String> history = new ArrayDeque<>();
	private ShaderPackOptionList optionList;
	private String currentScreen = null;

	public NavigationController(OptionMenuContainer container) {
		this.container = container;
	}

	public void back() {
		if (!history.isEmpty()) {
			history.removeLast();

			if (!history.isEmpty()) {
				currentScreen = history.getLast();
			} else {
				currentScreen = null;
			}
		} else {
			currentScreen = null;
		}

		this.rebuild();
	}

	public void open(String screen) {
		currentScreen = screen;
		history.addLast(screen);

		this.rebuild();
	}

	public void rebuild() {
		if (optionList != null) {
			optionList.rebuild();
		}
	}

	public void refresh() {
		if (optionList != null) {
			optionList.refresh();
		}
	}

	public boolean hasHistory() {
		return !this.history.isEmpty();
	}

	public void setActiveOptionList(ShaderPackOptionList optionList) {
		this.optionList = optionList;
	}

	public String getCurrentScreen() {
		return currentScreen;
	}
}
