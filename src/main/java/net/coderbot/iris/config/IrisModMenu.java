package net.coderbot.iris.config;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.coderbot.iris.gui.IrisConfigScreen;

public class IrisModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return IrisConfigScreen::new;
    }
}
