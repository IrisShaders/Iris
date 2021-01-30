package net.coderbot.iris.gui;

import net.coderbot.iris.Iris;
import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class IrisConfigScreen extends Screen implements TransparentBackgroundScreen {
    protected final IrisConfig config = Iris.getIrisConfig();
    protected PropertyDocumentWidget configProperties;

    protected final Screen parent;

    private ButtonWidget doneButton;
    private ButtonWidget refreshButton;
    private ButtonWidget applyButton;

    private boolean initialized = false;

    public IrisConfigScreen(Screen parent) {
        super(new LiteralText(""));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int bottomCenter = this.width / 2 - 50;
        boolean inWorld = this.client.world != null;

        float scrollAmount = 0.0f;
        String page = "main";

        if(this.configProperties != null) {
            scrollAmount = (float)this.configProperties.getScrollAmount() / this.configProperties.getMaxScroll();
            page = this.configProperties.getCurrentPage();
        }

        this.configProperties  = new PropertyDocumentWidget(client, width, height, 20, this.height - 34, 0, this.width, 26, 320);
        if(inWorld) this.configProperties.method_31322(false);
        this.configProperties.setDocument(this.config.createDocument(this.client.textRenderer, this, this.configProperties, 320), "main");

        this.configProperties.setScrollAmount(this.configProperties.getMaxScroll() * scrollAmount);
        this.configProperties.goTo(page);

        this.children.add(configProperties);

        this.doneButton = this.addButton(new ButtonWidget(bottomCenter + 104, this.height - 27, 100, 20, ScreenTexts.DONE, button -> { this.saveConfig(); onClose(); }));
        this.applyButton = this.addButton(new ButtonWidget(bottomCenter, this.height - 27, 100, 20, new TranslatableText("options.iris.apply"), button -> this.saveConfig()));
        this.refreshButton = this.addButton(new ButtonWidget(bottomCenter - 104, this.height - 27, 100, 20, new TranslatableText("options.iris.refresh"), button -> this.loadConfig()));

        loadConfig();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) this.renderBackground(matrices);
        else this.fillGradient(matrices, 0, 0, width, height, 0x4F232323, 0x4F232323);
        this.configProperties.render(matrices, mouseX, mouseY, delta);

        GuiUtil.drawDirtTexture(client, 0, 0, -100, width, 20);
        GuiUtil.drawDirtTexture(client, 0, this.height - 34, -100, width, 34);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        super.tick();
        this.configProperties.tick();
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }


    private void loadConfig() {
        this.configProperties.loadProperties();
    }

    private void saveConfig() {
        this.configProperties.saveProperties();
    }

    @Override
    public boolean renderHud() {
        return false;
    }
}
