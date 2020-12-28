package net.coderbot.iris.gui;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.element.ShaderPackListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.IOException;

public class ShaderPackScreen extends Screen {
    private Screen parent;

    private ShaderPackListWidget shaderPacks;

    private ButtonWidget doneButton;
    private ButtonWidget applyButton;
    private ButtonWidget cancelButton;

    private ButtonWidget openFolderButton;
    private ButtonWidget refreshButton;

    public ShaderPackScreen(Screen parent) {
        super(new TranslatableText("options.iris.shaderPackSelection.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int bottomCenter = this.width / 2 - 50;
        int topCenter = this.width / 2 - 76;
        boolean inWorld = this.client.world == null;
        this.shaderPacks = new ShaderPackListWidget(this.client, this.width / 2, this.height, 32, this.height - 58, 20);
        if(!inWorld) this.shaderPacks.method_31322(false);
        this.children.add(shaderPacks);
        this.doneButton = this.addButton(new ButtonWidget(bottomCenter + 104, this.height - 27, 100, 20, ScreenTexts.DONE, button -> { applyChanges(); onClose(); }));
        this.applyButton = this.addButton(new ButtonWidget(bottomCenter, this.height - 27, 100, 20, new TranslatableText("options.iris.apply"), button -> this.applyChanges()));
        this.cancelButton = this.addButton(new ButtonWidget(bottomCenter - 104, this.height - 27, 100, 20, ScreenTexts.CANCEL, button -> this.onClose()));
        this.openFolderButton = this.addButton(new ButtonWidget(topCenter - 78, this.height - 51, 152, 20, new TranslatableText("options.iris.openShaderPackFolder"), button -> Util.getOperatingSystem().open(Iris.getShaderPackDir().toFile())));
        this.refreshButton = this.addButton(new ButtonWidget(topCenter + 78, this.height - 51, 152, 20, new TranslatableText("options.iris.refreshShaderPacks"), button -> this.shaderPacks.refresh()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(this.client.world == null) this.renderBackground(matrices);
        GuiUtil.drawDirtTexture(client, 0, 0, -100, width, 32);
        GuiUtil.drawDirtTexture(client, 0, this.height - 58, -100, width, 58);
        this.shaderPacks.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("pack.iris.select.title").formatted(Formatting.GRAY, Formatting.ITALIC), (int)(this.width * 0.25), 21, 16777215);
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("pack.iris.configure.title").formatted(Formatting.GRAY, Formatting.ITALIC), (int)(this.width * 0.75), 21, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void onClose() {
        this.client.openScreen(this.parent);
    }

    private void applyChanges() {
        ShaderPackListWidget.ShaderPackEntry entry = this.shaderPacks.getSelected();
        String name = "(internal)";
        if(entry != null) name = entry.getPackName();
        Iris.getIrisConfig().setShaderPackName(name);
        try {
            Iris.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
