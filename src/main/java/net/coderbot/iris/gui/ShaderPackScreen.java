package net.coderbot.iris.gui;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.element.ShaderPackListWidget;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.IOException;

public class ShaderPackScreen extends Screen {
    private final Screen parent;

    private ShaderPackListWidget shaderPacks;
    private PropertyDocumentWidget shaderProperties;

    private ButtonWidget doneButton;
    private ButtonWidget applyButton;
    private ButtonWidget cancelButton;

    private ButtonWidget openFolderButton;
    private ButtonWidget refreshButton;

    private boolean wasHudHidden;

    public ShaderPackScreen(Screen parent) {
        super(new TranslatableText("options.iris.shaderPackSelection.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int bottomCenter = this.width / 2 - 50;
        int topCenter = this.width / 2 - 76;
        boolean inWorld = this.client.world != null;

        this.shaderPacks = new ShaderPackListWidget(this.client, this.width / 2, this.height, 32, this.height - 58, 0, width / 2);
        if(inWorld) this.shaderPacks.method_31322(false);
        this.children.add(shaderPacks);

        float scrollAmount = 0.0f;
        String page = "screen";

        if(this.shaderProperties != null) {
            scrollAmount = (float)this.shaderProperties.getScrollAmount() / this.shaderProperties.getMaxScroll();
            page = this.shaderProperties.getCurrentPage();
        }

        this.shaderProperties = new PropertyDocumentWidget(this.client, this.width / 2, this.height, 32, this.height - 58, this.width / 2, this.width, 26);
        if(inWorld) this.shaderProperties.method_31322(false);
        this.reloadShaderConfig();

        this.shaderProperties.setScrollAmount(this.shaderProperties.getMaxScroll() * scrollAmount);
        this.shaderProperties.goTo(page);

        this.children.add(shaderProperties);

        this.doneButton = this.addButton(new ButtonWidget(bottomCenter + 104, this.height - 27, 100, 20, ScreenTexts.DONE, button -> { applyChanges(); onClose(); }));
        this.applyButton = this.addButton(new ButtonWidget(bottomCenter, this.height - 27, 100, 20, new TranslatableText("options.iris.apply"), button -> this.applyChanges()));
        this.cancelButton = this.addButton(new ButtonWidget(bottomCenter - 104, this.height - 27, 100, 20, ScreenTexts.CANCEL, button -> this.onClose()));
        this.openFolderButton = this.addButton(new ButtonWidget(topCenter - 78, this.height - 51, 152, 20, new TranslatableText("options.iris.openShaderPackFolder"), button -> Util.getOperatingSystem().open(Iris.getShaderPackDir().toFile())));
        this.refreshButton = this.addButton(new ButtonWidget(topCenter + 78, this.height - 51, 152, 20, new TranslatableText("options.iris.refreshShaderPacks"), button -> this.shaderPacks.refresh()));

        if(inWorld) {
            this.wasHudHidden = this.client.options.hudHidden;
            this.client.options.hudHidden = true;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(this.client.world == null) this.renderBackground(matrices);
        else this.fillGradient(matrices, 0, 0, width, height, 0x4F232323, 0x4F232323);
        this.shaderPacks.render(matrices, mouseX, mouseY, delta);
        this.shaderProperties.render(matrices, mouseX, mouseY, delta);

        GuiUtil.drawDirtTexture(client, 0, 0, -100, width, 32);
        GuiUtil.drawDirtTexture(client, 0, this.height - 58, -100, width, 58);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("pack.iris.select.title").formatted(Formatting.GRAY, Formatting.ITALIC), (int)(this.width * 0.25), 21, 16777215);
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("pack.iris.configure.title").formatted(Formatting.GRAY, Formatting.ITALIC), (int)(this.width * 0.75), 21, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        super.tick();
        this.shaderPacks.tick();
        this.shaderProperties.tick();
    }

    public void onClose() {
        this.client.openScreen(this.parent);
        if(this.client.world != null) {
            this.client.options.hudHidden = this.wasHudHidden;
        }
    }

    @Override
    public void removed() {
        if(this.client.world != null) {
            this.client.options.hudHidden = this.wasHudHidden;
        }
        super.removed();
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
        this.reloadShaderConfig();
    }

    private void reloadShaderConfig() {
        this.shaderProperties.setDocument(PropertyDocumentWidget.createDocument(this.client.textRenderer, this.width / 2, Iris.getIrisConfig().getShaderPackName(), Iris.getCurrentPack(), this.shaderProperties), "screen");
    }
}
