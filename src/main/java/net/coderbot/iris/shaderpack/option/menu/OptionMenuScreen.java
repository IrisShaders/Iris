package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.element.ShaderPackOptionList;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OptionMenuScreen {
	private final Supplier<Component> heading;
	private final List<OptionMenuElement> elements = new ArrayList<>();
	private final Optional<Integer> columnCount;
	private final boolean hasBackButton;

	public OptionMenuScreen(Supplier<Component> heading, OptionMenuContainer container, ShaderProperties shaderProperties, ShaderPackOptions shaderPackOptions, List<String> elementStrings, Optional<Integer> columnCount, boolean hasBackButton) {
		this.heading = heading;
		this.columnCount = columnCount;
		this.hasBackButton = hasBackButton;

		for (String elementString : elementStrings) {
			if ("*".equals(elementString)) {
				container.queueForUnusedOptionDump(this.elements.size(), this.elements);

				continue;
			}

			try {
				OptionMenuElement element = OptionMenuElement.create(elementString, container, shaderProperties, shaderPackOptions);

				this.elements.add(element);

				if (element instanceof OptionMenuOptionElement) {
					container.notifyOptionAdded(elementString, (OptionMenuOptionElement) element);
				}
			} catch (IllegalArgumentException error) {
				Iris.logger.error(error);

				this.elements.add(OptionMenuElement.EMPTY);
			}
		}
	}

	public int getColumnCount() {
		return columnCount.orElse(elements.size() > 18 ? 3 : 2);
	}

	public void applyToMinecraftGui(ShaderPackScreen screen, ShaderPackOptionList optionList) {
		optionList.addHeader(heading.get(), hasBackButton);
		optionList.addWidgets(getColumnCount(), elements.stream().map(element -> element.createWidget(screen, optionList.getNavigation())).collect(Collectors.toList()));
	}
}
