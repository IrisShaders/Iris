package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.gui.element.ShaderPackOptionList;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.*;

public class OptionMenuContainer {
	private final OptionMenuScreen mainScreen;
	private final Map<String, OptionMenuScreen> subScreens = new HashMap<>();
	private final List<OptionMenuOptionElement> searchOptions = new ArrayList<>();

	public OptionMenuContainer(String shaderPackName, ShaderProperties shaderProperties, ShaderPackOptions shaderPackOptions) {
		this.mainScreen = new OptionMenuScreen(new TextComponent(shaderPackName).withStyle(ChatFormatting.BOLD), this, shaderProperties, shaderPackOptions, shaderProperties.getMainScreenOptions(), shaderProperties.getMainScreenColumnCount(), false);

		Map<String, Integer> subScreenColumnCounts = shaderProperties.getSubScreenColumnCount();
		shaderProperties.getSubScreenOptions().forEach((screenKey, options) -> {
			subScreens.put(screenKey, new OptionMenuScreen(new TranslatableComponent("screen."+screenKey), this, shaderProperties, shaderPackOptions, options, Optional.ofNullable(subScreenColumnCounts.get(screenKey)), true));
		});
	}

	public void applyToMinecraftGui(ShaderPackOptionList optionList, NavigationController navigation) {
		OptionMenuScreen screen = mainScreen;

		if (navigation.getCurrentScreen() != null && subScreens.containsKey(navigation.getCurrentScreen())) {
			screen = subScreens.get(navigation.getCurrentScreen());
		}

		screen.applyToMinecraftGui(optionList);
	}

	public void putOptionForSearching(OptionMenuOptionElement option) {
		searchOptions.add(option);
	}

	public Map<String, OptionMenuOptionElement> createOptionSearchMap() {
		Map<String, OptionMenuOptionElement> map = new HashMap<>();

		searchOptions.forEach(opt -> {
			map.put(I18n.get(opt.createDescriptionId()), opt);
		});

		return map;
	}
}
