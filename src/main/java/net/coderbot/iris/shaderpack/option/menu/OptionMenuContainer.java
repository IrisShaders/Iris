package net.coderbot.iris.shaderpack.option.menu;

import com.google.common.collect.Lists;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.ShaderPackOptionList;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.Profile;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OptionMenuContainer {
	private final OptionMenuScreen mainScreen;
	private final Map<String, OptionMenuScreen> subScreens = new HashMap<>();

	private final List<OptionMenuOptionElement> usedOptionElements = new ArrayList<>();
	private final List<String> usedOptions = new ArrayList<>();
	private final List<String> unusedOptions = new ArrayList<>(); // To be used when screens contain a "*" element
	private final Map<List<OptionMenuElement>, Integer> unusedOptionDumpQueue = new HashMap<>(); // Used by screens with "*" element

	private final @Nullable String currentProfile;
	private final Map<String, Profile> profiles;

	public OptionMenuContainer(String shaderPackName, ShaderProperties shaderProperties, ShaderPackOptions shaderPackOptions, @Nullable String currentProfile, Map<String, Profile> profiles) {
		this.currentProfile = currentProfile;
		this.profiles = profiles;

		this.mainScreen = new OptionMenuScreen(
				() -> new TextComponent(shaderPackName).withStyle(ChatFormatting.BOLD),
				this, shaderProperties, shaderPackOptions, shaderProperties.getMainScreenOptions(), shaderProperties.getMainScreenColumnCount(), false);

		this.unusedOptions.addAll(shaderPackOptions.getOptionSet().getBooleanOptions().keySet());
		this.unusedOptions.addAll(shaderPackOptions.getOptionSet().getStringOptions().keySet());

		Map<String, Integer> subScreenColumnCounts = shaderProperties.getSubScreenColumnCount();
		shaderProperties.getSubScreenOptions().forEach((screenKey, options) -> {
			subScreens.put(screenKey, new OptionMenuScreen(
					() -> GuiUtil.translateOrDefault(new TextComponent(screenKey), "screen." + screenKey),
					this, shaderProperties, shaderPackOptions, options, Optional.ofNullable(subScreenColumnCounts.get(screenKey)), true));
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
					Iris.logger.error(error);

					elementsToInsert.add(OptionMenuElement.EMPTY);
				}
			}

			entry.getKey().addAll(entry.getValue(), elementsToInsert);
		}
	}

	public void applyToMinecraftGui(ShaderPackScreen packScreen, ShaderPackOptionList optionList, NavigationController navigation) {
		OptionMenuScreen screen = mainScreen;

		if (navigation.getCurrentScreen() != null && subScreens.containsKey(navigation.getCurrentScreen())) {
			screen = subScreens.get(navigation.getCurrentScreen());
		}

		screen.applyToMinecraftGui(packScreen, optionList);
	}

	public @Nullable String getCurrentProfile() {
		return currentProfile;
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

	public Map<String, OptionMenuOptionElement> createOptionSearchMap() {
		Map<String, OptionMenuOptionElement> map = new HashMap<>();

		usedOptionElements.forEach(opt -> {
			map.put(I18n.get(opt.createDescriptionId()), opt);
		});

		return map;
	}
}
