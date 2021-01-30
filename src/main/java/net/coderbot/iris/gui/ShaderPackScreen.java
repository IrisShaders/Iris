package net.coderbot.iris.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.gui.element.ShaderPackListWidget;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;

public class ShaderPackScreen extends Screen implements TransparentBackgroundScreen {
    private final Screen parent;

    private ShaderPackListWidget shaderPacks;
    private PropertyDocumentWidget shaderProperties;

    private ButtonWidget doneButton;
    private ButtonWidget applyButton;
    private ButtonWidget cancelButton;

    private ButtonWidget openFolderButton;
    private ButtonWidget refreshButton;

    private ButtonWidget irisConfigButton;

    private ShaderConfigScreenViewOptionButtonWidget condensedConfigButton;

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

        this.refreshShaderPropertiesWidget();

        this.doneButton = this.addButton(new ButtonWidget(bottomCenter + 104, this.height - 27, 100, 20, ScreenTexts.DONE, button -> { applyChanges(); onClose(); }));
        this.applyButton = this.addButton(new ButtonWidget(bottomCenter, this.height - 27, 100, 20, new TranslatableText("options.iris.apply"), button -> this.applyChanges()));
        this.cancelButton = this.addButton(new ButtonWidget(bottomCenter - 104, this.height - 27, 100, 20, ScreenTexts.CANCEL, button -> this.onClose()));
        this.openFolderButton = this.addButton(new ButtonWidget(topCenter - 78, this.height - 51, 152, 20, new TranslatableText("options.iris.openShaderPackFolder"), button -> Util.getOperatingSystem().open(Iris.getShaderPackDir().toFile())));
        this.refreshButton = this.addButton(new ButtonWidget(topCenter + 78, this.height - 51, 152, 20, new TranslatableText("options.iris.refreshShaderPacks"), button -> this.shaderPacks.refresh()));
        this.irisConfigButton = this.addButton(new IrisConfigScreenButtonWidget(this.width - 26, 6, button -> this.client.openScreen(new IrisConfigScreen(this))));

        this.condensedConfigButton = this.addButton(new ShaderConfigScreenViewOptionButtonWidget(this.width - 23, 35, 0, 20, Iris.getIrisConfig().getIfCondensedShaderConfig(), button -> {
            condensedConfigButton.selected = !condensedConfigButton.selected;
            Iris.getIrisConfig().setIfCondensedShaderConfig(condensedConfigButton.selected);
            this.refreshShaderPropertiesWidget();
            try {
                Iris.getIrisConfig().save();
            } catch (IOException e) {
                Iris.logger.error("Error setting config for condensed shader pack config view!");
                e.printStackTrace();
            }
        }));
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

        if(irisConfigButton.isMouseOver(mouseX, mouseY)) {
            this.renderTooltip(matrices, new TranslatableText("tooltip.iris.config"), mouseX, mouseY);
        }

        boolean mouseInHoverArea = mouseX > width / 2 && mouseY > 32 && mouseY < ((height - 32) * 0.17) + 32 && this.shaderProperties.getScrollAmount() < 3;

        this.condensedConfigButton.areaHovered = mouseInHoverArea;
    }

    @Override
    public void tick() {
        super.tick();
        this.shaderPacks.tick();
        this.shaderProperties.tick();
        this.condensedConfigButton.tick();
    }

    @Override
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
        this.reloadShaderConfig();
    }

    private void refreshShaderPropertiesWidget() {
        this.children.remove(shaderProperties);

        float scrollAmount = 0.0f;
        String page = "screen";

        if(this.shaderProperties != null) {
            scrollAmount = (float)this.shaderProperties.getScrollAmount() / this.shaderProperties.getMaxScroll();
            page = this.shaderProperties.getCurrentPage();
        }

        this.shaderProperties = new PropertyDocumentWidget(this.client, this.width / 2, this.height, 32, this.height - 58, this.width / 2, this.width, 26);
        if(this.client.world != null) this.shaderProperties.method_31322(false);
        this.reloadShaderConfig();

        this.shaderProperties.goTo(page);
        this.shaderProperties.setScrollAmount(this.shaderProperties.getMaxScroll() * scrollAmount);

        this.children.add(shaderProperties);
    }

    private void reloadShaderConfig() {
        this.shaderProperties.setDocument(PropertyDocumentWidget.createShaderpackConfigDocument(this.client.textRenderer, this.width / 2, Iris.getIrisConfig().getShaderPackName(), Iris.getCurrentPack(), this.shaderProperties), "screen");
    }

    @Override
    public boolean renderHud() {
        return false;
    }

    public static class IrisConfigScreenButtonWidget extends ButtonWidget {
        public IrisConfigScreenButtonWidget(int x, int y, PressAction press) {
            super(x, y, 20, 20, LiteralText.EMPTY, press);
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(GuiUtil.WIDGETS_TEXTURE);
            drawTexture(matrices, x, y, isMouseOver(mouseX, mouseY) ? 20 : 0, 0, 20, 20);
        }
    }

    public static class ShaderConfigScreenViewOptionButtonWidget extends ButtonWidget {
        private int u;
        private int v;
        private int fadeTicks;
        public boolean areaHovered;

        public boolean selected;

        public ShaderConfigScreenViewOptionButtonWidget(int x, int y, int u, int v, boolean selected, PressAction press) {
            super(x, y, 18, 13, LiteralText.EMPTY, press);
            this.selected = selected;
            this.u = u;
            this.v = v;
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(GuiUtil.WIDGETS_TEXTURE);
            float alpha = Math.max(0, Math.min(3, this.fadeTicks + (hovered ? delta : -delta))) / 3;
            RenderSystem.enableBlend();
            RenderSystem.color4f(1f, 1f, 1f, alpha);
            drawTexture(matrices, x, y, u + (isMouseOver(mouseX, mouseY) ? 18 : 0), v + (selected ? 13 : 0), 18, 13);
            //GuiUtil.texture(x, y, -100, 18, 13, /*u + (isMouseOver(mouseX, mouseY) ? 18 : 0), v + (selected ? 13 : 0)*/0,0, 18, 13, 1f, 1f, 1f, alpha);
        }

        public void tick() {
            if(areaHovered) {
                if(fadeTicks < 3) fadeTicks++;
            } else if(fadeTicks > 0) fadeTicks--;
        }
    }
}
