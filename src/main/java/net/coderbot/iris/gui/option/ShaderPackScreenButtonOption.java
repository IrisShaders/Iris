package net.coderbot.iris.gui.option;

import net.coderbot.iris.gui.ShaderPackScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.VideoOptionsScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.text.TranslatableText;

public class ShaderPackScreenButtonOption extends Option {
    private final Screen parent;
    private final MinecraftClient client;

    public ShaderPackScreenButtonOption(Screen parent, MinecraftClient client) {
        super("options.iris.shaderPackSelection");
        this.parent = parent;
        this.client = client;
    }

    @Override
    public AbstractButtonWidget createButton(GameOptions options, int x, int y, int width) {
        return new OptionButtonWidget(x, y, width, 20, this, new TranslatableText("options.iris.shaderPackSelection"), button -> this.client.openScreen(new ShaderPackScreen(this.parent)));
    }
}
