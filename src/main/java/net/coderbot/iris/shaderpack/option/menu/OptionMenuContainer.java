package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.Iris;

import com.google.common.collect.Lists;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.Profile;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;

import java.util.*;

public class OptionMenuContainer {
	public final OptionMenuElementScreen mainScreen;
	public final Map<String, OptionMenuElementScreen> subScreens = new HashMap<>();

	private final List<OptionMenuOptionElement> usedOptionElements = new ArrayList<>();
	private final List<String> usedOptions = new ArrayList<>();
	private final List<String> unusedOptions = new ArrayList<>(); // To be used when screens contain a "*" element
	private final Map<List<OptionMenuElement>, Integer> unusedOptionDumpQueue = new HashMap<>(); // Used by screens with "*" element
	private final Map<String, Profile> profiles;

	public OptionMenuContainer(ShaderProperties shaderProperties, ShaderPackOptions shaderPackOptions, Map<String, Profile> profiles) {
		this.profiles = profiles;

		this.mainScreen = new OptionMenuMainElementScreen(
				this, shaderProperties, shaderPackOptions, shaderProperties.getMainScreenOptions(), shaderProperties.getMainScreenColumnCount());

		this.unusedOptions.addAll(shaderPackOptions.getOptionSet().getBooleanOptions().keySet());
		this.unusedOptions.addAll(shaderPackOptions.getOptionSet().getStringOptions().keySet());

		Map<String, Integer> subScreenColumnCounts = shaderProperties.getSubScreenColumnCount();
		shaderProperties.getSubScreenOptions().forEach((screenKey, options) -> {
			subScreens.put(screenKey, new OptionMenuSubElementScreen(
					screenKey, this, shaderProperties, shaderPackOptions, options, Optional.ofNullable(subScreenColumnCounts.get(screenKey))));
		});

		// Dump all unused options into screens containing "*"
		for (Map.Entry<List<OptionMenuElement>, Integer> entry : unusedOptionDumpQueue.entrySet()) {
			List<OptionMenuElement> elementsToInsert = new ArrayList<>();
			List<String> unusedOptionsCopy = Lists.newArrayList(this.unusedOptions);

			for (String optionId : unusedOptionsCopy) {
				try {
					OptionMenuElement element = OptionMenuElement.create(optionId, this, shaderProperties, shaderPackOptions);
					elementsToInsert.add(element);

					if (element instanceof OptionMenuOptionElement) {
						this.notifyOptionAdded(optionId, (OptionMenuOptionElement) element);
					}
				} catch (IllegalArgumentException error) {
					Iris.logger.warn(error);

					elementsToInsert.add(OptionMenuElement.EMPTY);
				}
			}

			entry.getKey().addAll(entry.getValue(), elementsToInsert);
		}
	}

	public Map<String, Profile> getProfiles() {
		return profiles;
	}

	// Screens will call this when they contain a "*" element, so that the list of
	// unused options can be added after all other screens have been resolved
	public void queueForUnusedOptionDump(int index, List<OptionMenuElement> elementList) {
		this.unusedOptionDumpQueue.put(elementList, index);
	}

	public void notifyOptionAdded(String optionId, OptionMenuOptionElement option) {
		if (!usedOptions.contains(optionId)) {
			usedOptionElements.add(option);
			usedOptions.add(optionId);
		}

		unusedOptions.remove(optionId);
	}
}
