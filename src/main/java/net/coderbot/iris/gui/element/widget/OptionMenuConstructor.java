package net.coderbot.iris.gui.element.widget;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.ShaderPackOptionList;
import net.coderbot.iris.gui.element.screen.ElementWidgetScreenData;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuBooleanOptionElement;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuContainer;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuElement;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuElementScreen;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuLinkElement;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuMainElementScreen;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuProfileElement;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuStringOptionElement;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuSubElementScreen;
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

	public static AbstractElementWidget<? extends OptionMenuElement> createWidget(OptionMenuElement element) {
		return WIDGET_CREATORS.getOrDefault(element.getClass(), e -> AbstractElementWidget.EMPTY).create(element);
	}

	public static ElementWidgetScreenData createScreenData(OptionMenuElementScreen screen) {
		return SCREEN_DATA_CREATORS.getOrDefault(screen.getClass(), s -> ElementWidgetScreenData.EMPTY).create(screen);
	}

	@SuppressWarnings("unchecked")
	public static void constructAndApplyToScreen(OptionMenuContainer container, ShaderPackScreen packScreen, ShaderPackOptionList optionList, NavigationController navigation) {
		OptionMenuElementScreen screen = container.mainScreen;

		if (navigation.getCurrentScreen() != null && container.subScreens.containsKey(navigation.getCurrentScreen())) {
			screen = container.subScreens.get(navigation.getCurrentScreen());
		}

		ElementWidgetScreenData data = createScreenData(screen);

		optionList.addHeader(data.heading, data.backButton);
		optionList.addWidgets(screen.getColumnCount(), screen.elements.stream().map(element -> {
			AbstractElementWidget<OptionMenuElement> widget = (AbstractElementWidget<OptionMenuElement>) createWidget(element);
			widget.init(packScreen, navigation);
			return widget;
		}).collect(Collectors.toList()));
	}

	static {
		registerScreen(OptionMenuMainElementScreen.class, screen ->
				new ElementWidgetScreenData(new TextComponent(Iris.getCurrentPackName()).withStyle(ChatFormatting.BOLD), false));

		registerScreen(OptionMenuSubElementScreen.class, screen ->
				new ElementWidgetScreenData(GuiUtil.translateOrDefault(new TextComponent(screen.screenId), "screen." + screen.screenId), true));

		registerWidget(OptionMenuBooleanOptionElement.class, BooleanElementWidget::new);
		registerWidget(OptionMenuProfileElement.class, ProfileElementWidget::new);
		registerWidget(OptionMenuLinkElement.class, LinkElementWidget::new);

		registerWidget(OptionMenuStringOptionElement.class, element ->
				element.slider ? new SliderElementWidget(element) : new StringElementWidget(element));
	}

	public interface WidgetProvider<T extends OptionMenuElement> {
		AbstractElementWidget<T> create(T element);
	}

	public interface ScreenDataProvider<T extends OptionMenuElementScreen> {
		ElementWidgetScreenData create(T screen);
	}
}
