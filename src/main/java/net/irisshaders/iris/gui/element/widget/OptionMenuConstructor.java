package net.irisshaders.iris.gui.element.widget;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gui.GuiUtil;
import net.irisshaders.iris.gui.NavigationController;
import net.irisshaders.iris.gui.element.ShaderPackOptionList;
import net.irisshaders.iris.gui.element.screen.ElementWidgetScreenData;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuBooleanOptionElement;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuContainer;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuElement;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuElementScreen;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuLinkElement;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuMainElementScreen;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuProfileElement;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuStringOptionElement;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuSubElementScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class OptionMenuConstructor {
	private static final Map<Class<? extends OptionMenuElement>, WidgetProvider<OptionMenuElement>> WIDGET_CREATORS = new HashMap<>();
	private static final Map<Class<? extends OptionMenuElementScreen>, ScreenDataProvider<OptionMenuElementScreen>> SCREEN_DATA_CREATORS = new HashMap<>();

	static {
		registerScreen(OptionMenuMainElementScreen.class, screen ->
			new ElementWidgetScreenData(Component.literal(Iris.getCurrentPackName()).append(Iris.isFallback() ? " (fallback)" : "").withStyle(ChatFormatting.BOLD), false));

		registerScreen(OptionMenuSubElementScreen.class, screen ->
			new ElementWidgetScreenData(GuiUtil.translateOrDefault(Component.literal(screen.screenId), "screen." + screen.screenId), true));

		registerWidget(OptionMenuBooleanOptionElement.class, BooleanElementWidget::new);
		registerWidget(OptionMenuProfileElement.class, ProfileElementWidget::new);
		registerWidget(OptionMenuLinkElement.class, LinkElementWidget::new);

		registerWidget(OptionMenuStringOptionElement.class, element ->
			element.slider ? new SliderElementWidget(element) : new StringElementWidget(element));
	}

	private OptionMenuConstructor() {
	}

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

		optionList.addHeader(data.heading(), data.backButton());
		optionList.addWidgets(screen.getColumnCount(), screen.elements.stream().map(element -> {
			AbstractElementWidget<OptionMenuElement> widget = (AbstractElementWidget<OptionMenuElement>) createWidget(element);
			widget.init(packScreen, navigation);
			return widget;
		}).collect(Collectors.toList()));
	}

	public interface WidgetProvider<T extends OptionMenuElement> {
		AbstractElementWidget<T> create(T element);
	}

	public interface ScreenDataProvider<T extends OptionMenuElementScreen> {
		ElementWidgetScreenData create(T screen);
	}
}
