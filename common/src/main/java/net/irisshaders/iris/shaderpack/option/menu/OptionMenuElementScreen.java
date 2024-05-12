package net.irisshaders.iris.shaderpack.option.menu;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.option.ShaderPackOptions;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OptionMenuElementScreen {
	public final List<OptionMenuElement> elements = new ArrayList<>();

	private final Optional<Integer> columnCount;

	public OptionMenuElementScreen(OptionMenuContainer container, ShaderProperties shaderProperties, ShaderPackOptions shaderPackOptions, List<String> elementStrings, Optional<Integer> columnCount) {
		this.columnCount = columnCount;

		for (String elementString : elementStrings) {
			if ("*".equals(elementString)) {
				container.queueForUnusedOptionDump(this.elements.size(), this.elements);

				continue;
			}

			try {
				OptionMenuElement element = OptionMenuElement.create(elementString, container, shaderProperties, shaderPackOptions);

				if (element != null) {
					this.elements.add(element);

					if (element instanceof OptionMenuOptionElement) {
						container.notifyOptionAdded(elementString, (OptionMenuOptionElement) element);
					}
				}
			} catch (IllegalArgumentException error) {
				Iris.logger.warn(error.getMessage());

				this.elements.add(OptionMenuElement.EMPTY);
			}
		}
	}

	public int getColumnCount() {
		return columnCount.orElse(elements.size() > 18 ? 3 : 2);
	}
}
