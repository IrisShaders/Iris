package net.coderbot.iris.gui.element.widget;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.ShaderPackOptionList;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.menu.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class OptionMenuConstructor {
	private static final Map<Class<? extends OptionMenuElement>, WidgetProvider<OptionMenuElement>> WIDGET_CREATORS = new HashMap<>();
	private static final Map<Class<? extends OptionMenuElementScreen>, ScreenDataProvider<OptionMenuElementScreen>> SCREEN_DATA_CREATORS = new HashMap<>();

	private OptionMenuConstructor() {}

	@SuppressWarnings("unchecked")
	public static <T extends OptionMenuElement> void registerWidget(Class<T> element, WidgetProvider<T> widget) {
		WIDGET_CREATORS.put(element, (WidgetProvider<OptionMenuElement>) widget);
	}

	@SuppressWarnings("unchecked")
	public static <T extends OptionMenuElementScreen> void registerScreen(Class<T> screen, ScreenDataProvider<T> data) {
		SCREEN_DATA_CREATORS.put(screen, (ScreenDataProvider<OptionMenuElementScreen>) data);
	}

	public static AbstractElementWidget createWidget(OptionMenuElement element, ShaderPackScreen screen, NavigationController navigation) {
		return WIDGET_CREATORS.getOrDefault(element.getClass(), (e, s, n) -> AbstractElementWidget.EMPTY).create(element, screen, navigation);
	}

	public static ElementWidgetScreenData createScreenData(OptionMenuElementScreen screen) {
		return SCREEN_DATA_CREATORS.getOrDefault(screen.getClass(), s -> ElementWidgetScreenData.EMPTY).create(screen);
	}

	public static void constructAndApplyToScreen(OptionMenuContainer container, ShaderPackScreen packScreen, ShaderPackOptionList optionList, NavigationController navigation) {
		OptionMenuElementScreen screen = container.mainScreen;

		if (navigation.getCurrentScreen() != null && container.subScreens.containsKey(navigation.getCurrentScreen())) {
			screen = container.subScreens.get(navigation.getCurrentScreen());
		}

		ElementWidgetScreenData data = createScreenData(screen);

		optionList.addHeader(data.heading, data.backButton);
		optionList.addWidgets(screen.getColumnCount(), screen.elements.stream().map(element -> createWidget(element, packScreen, navigation)).collect(Collectors.toList()));
	}

	static {
		registerScreen(OptionMenuMainElementScreen.class, screen ->
				new ElementWidgetScreenData(new TextComponent(Iris.getCurrentPackName()).withStyle(ChatFormatting.BOLD), false));

		registerScreen(OptionMenuSubElementScreen.class, screen ->
				new ElementWidgetScreenData(GuiUtil.translateOrDefault(new TextComponent(screen.screenId), "screen." + screen.screenId), true));

		registerWidget(OptionMenuBooleanOptionElement.class, (element, screen, navigation) ->
				new BooleanElementWidget(screen, navigation, element.option,
						element.getPendingOptionValues().isBooleanFlipped(element.optionId),
						element.getAppliedOptionValues().isBooleanFlipped(element.optionId)));

		registerWidget(OptionMenuStringOptionElement.class, (element, screen, navigation) ->
				element.slider ?
					new SliderElementWidget(screen, navigation, element.option,
							element.getPendingOptionValues().getStringValue(element.optionId),
							element.getAppliedOptionValues().getStringValue(element.optionId))
					: new StringElementWidget(screen, navigation, element.option,
							element.getPendingOptionValues().getStringValue(element.optionId),
							element.getAppliedOptionValues().getStringValue(element.optionId)));

		registerWidget(OptionMenuProfileElement.class, (element, screen, navigation) ->
				new ProfileElementWidget(screen, navigation, element.profiles, element.options, element.getPendingOptionValues()));

		registerWidget(OptionMenuLinkElement.class, (element, screen, navigation) ->
				new LinkElementWidget(navigation, GuiUtil.translateOrDefault(new TextComponent(element.targetScreenId), "screen." + element.targetScreenId), element.targetScreenId));
	}

	public interface WidgetProvider<T extends OptionMenuElement> {
		AbstractElementWidget create(T element, ShaderPackScreen screen, NavigationController navigation);
	}

	public interface ScreenDataProvider<T extends OptionMenuElementScreen> {
		ElementWidgetScreenData create(T screen);
	}
}
