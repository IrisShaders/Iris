package net.coderbot.iris.gui.screen;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.element.ShaderPackListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.IOException;

public class ShaderPackScreen extends Screen implements HudHideable {
	private final Screen parent;

	private ShaderPackListWidget shaderPacks;

	public ShaderPackScreen(Screen parent) {
		super(new TranslatableText("options.iris.shaderPackSelection.title"));
		this.parent = parent;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (this.client.world == null) {
			this.renderBackground(matrices);
		}
		else {
			this.fillGradient(matrices, 0, 0, width, height, 0x4F232323, 0x4F232323);
		}

		this.shaderPacks.render(matrices, mouseX, mouseY, delta);
		
		drawCenteredText(matrices, this.textRenderer, this.title, (int)(this.width * 0.5), 8, 16777215);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("pack.iris.select.title").formatted(Formatting.GRAY, Formatting.ITALIC), (int)(this.width * 0.5), 21, 16777215);
		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	protected void init() {
		super.init();
		int bottomCenter = this.width / 2 - 50;
		int topCenter = this.width / 2 - 76;
		boolean inWorld = this.client.world != null;

		this.shaderPacks = new ShaderPackListWidget(this.client, this.width, this.height, 32, this.height - 58, 0, this.width);
		if (inWorld) {
			this.shaderPacks.method_31322(false);
		}
		this.children.add(shaderPacks);

		this.addButton(new ButtonWidget(bottomCenter + 104, this.height - 27, 100, 20, ScreenTexts.DONE, button -> {
			applyChanges();
			onClose();
		}));
		this.addButton(new ButtonWidget(bottomCenter, this.height - 27, 100, 20, new TranslatableText("options.iris.apply"), button -> this.applyChanges()));
		this.addButton(new ButtonWidget(bottomCenter - 104, this.height - 27, 100, 20, ScreenTexts.CANCEL, button -> this.onClose()));
		this.addButton(new ButtonWidget(topCenter - 78, this.height - 51, 152, 20, new TranslatableText("options.iris.openShaderPackFolder"), button -> Util.getOperatingSystem().open(Iris.shaderpacksDirectory.toFile())));
		this.addButton(new ButtonWidget(topCenter + 78, this.height - 51, 152, 20, new TranslatableText("options.iris.refreshShaderPacks"), button -> this.shaderPacks.refresh()));
	}

	@Override
	public void onClose() {
		this.client.openScreen(parent);
	}

	private void applyChanges() {
		ShaderPackListWidget.ShaderPackEntry entry = this.shaderPacks.getSelected();
		String name = "(internal)";
		if (entry != null) {
			name = entry.getPackName();
		}
		Iris.getIrisConfig().setShaderPackName(name);
		try {
			Iris.reload();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
