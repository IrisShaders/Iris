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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ShaderPackScreen extends Screen implements HudHideable {
	private static final Text SELECT_TITLE = new TranslatableText("pack.iris.select.title").formatted(Formatting.GRAY, Formatting.ITALIC);

	private final Screen parent;

	private ShaderPackListWidget shaderPackList;

	private Text addedPackDialog = null;
	private int addedPackDialogTimer = 0;

	private boolean dropChanges = false;

	public ShaderPackScreen(Screen parent) {
		super(new TranslatableText("options.iris.shaderPackSelection.title"));

		this.parent = parent;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (this.client.world == null) {
			this.renderBackground(matrices);
		} else {
			this.fillGradient(matrices, 0, 0, width, height, 0x4F232323, 0x4F232323);
		}

		this.shaderPackList.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, this.title, (int)(this.width * 0.5), 8, 0xFFFFFF);

		if (addedPackDialog != null && addedPackDialogTimer > 0) {
			drawCenteredText(matrices, this.textRenderer, addedPackDialog, (int)(this.width * 0.5), 21, 0xFFFFFF);
		} else {
			drawCenteredText(matrices, this.textRenderer, SELECT_TITLE, (int)(this.width * 0.5), 21, 0xFFFFFF);
		}

		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	protected void init() {
		super.init();
		int bottomCenter = this.width / 2 - 50;
		int topCenter = this.width / 2 - 76;
		boolean inWorld = this.client.world != null;

		this.shaderPackList = new ShaderPackListWidget(this.client, this.width, this.height, 32, this.height - 58, 0, this.width);

		if (inWorld) {
			this.shaderPackList.method_31322(false);
		}

		this.children.add(shaderPackList);

		this.addButton(new ButtonWidget(bottomCenter + 104, this.height - 27, 100, 20,
			ScreenTexts.DONE, button -> onClose()));

		this.addButton(new ButtonWidget(bottomCenter, this.height - 27, 100, 20,
			new TranslatableText("options.iris.apply"), button -> this.applyChanges()));

		this.addButton(new ButtonWidget(bottomCenter - 104, this.height - 27, 100, 20,
			ScreenTexts.CANCEL, button -> this.dropChangesAndClose()));

		this.addButton(new ButtonWidget(topCenter - 78, this.height - 51, 152, 20,
			new TranslatableText("options.iris.openShaderPackFolder"), button -> openShaderPackFolder()));

		this.addButton(new ButtonWidget(topCenter + 78, this.height - 51, 152, 20,
			new TranslatableText("options.iris.refreshShaderPacks"), button -> this.shaderPackList.refresh()));
	}

	@Override
	public void tick() {
		super.tick();

		if (this.addedPackDialogTimer > 0) {
			this.addedPackDialogTimer--;
		}
	}

	@Override
	public void filesDragged(List<Path> paths) {
		List<Path> packs = paths.stream().filter(Iris::isValidShaderpack).collect(Collectors.toList());

		for (Path pack : packs) {
			String fileName = pack.getFileName().toString();

			try {
				Files.copy(pack, Iris.SHADERPACKS_DIRECTORY.resolve(fileName));
			} catch (FileAlreadyExistsException e) {
				this.addedPackDialog = new TranslatableText(
						"options.iris.shaderPackSelection.copyErrorAlreadyExists",
						fileName
				).formatted(Formatting.ITALIC, Formatting.RED);

				this.addedPackDialogTimer = 100;
				this.shaderPackList.refresh();

				return;
			} catch (IOException e) {
				Iris.logger.warn("Error copying dragged shader pack", e);

				this.addedPackDialog = new TranslatableText(
						"options.iris.shaderPackSelection.copyError",
						fileName
				).formatted(Formatting.ITALIC, Formatting.RED);

				this.addedPackDialogTimer = 100;
				this.shaderPackList.refresh();

				return;
			}
		}

		// After copying the relevant files over to the folder, make sure to refresh the shader pack list.
		this.shaderPackList.refresh();

		if (packs.size() == 0) {
			// If zero packs were added, then notify the user that the files that they added weren't actually shader
			// packs.

			if (paths.size() == 1) {
				// If a single pack could not be added, provide a message with that pack in the file name
				String fileName = paths.get(0).getFileName().toString();

				this.addedPackDialog = new TranslatableText(
					"options.iris.shaderPackSelection.failedAddSingle",
					fileName
				).formatted(Formatting.ITALIC, Formatting.RED);
			} else {
				// Otherwise, show a generic message.

				this.addedPackDialog = new TranslatableText(
					"options.iris.shaderPackSelection.failedAdd"
				).formatted(Formatting.ITALIC, Formatting.RED);
			}

		} else if (packs.size() == 1) {
			// In most cases, users will drag a single pack into the selection menu. So, let's special case it.
			String packName = packs.get(0).getFileName().toString();

			this.addedPackDialog = new TranslatableText(
					"options.iris.shaderPackSelection.addedPack",
					packName
			).formatted(Formatting.ITALIC, Formatting.YELLOW);

			// Select the pack that the user just added, since if a user just dragged a pack in, they'll probably want
			// to actually use that pack afterwards.
			this.shaderPackList.select(packName);
		} else {
			// We also support multiple packs being dragged and dropped at a time. Just show a generic success message
			// in that case.
			this.addedPackDialog = new TranslatableText(
					"options.iris.shaderPackSelection.addedPacks",
					packs.size()
			).formatted(Formatting.ITALIC, Formatting.YELLOW);
		}

		// Show the relevant message for 5 seconds (100 ticks)
		this.addedPackDialogTimer = 100;
	}

	@Override
	public void onClose() {
		if (!dropChanges) {
			// TODO: Don't apply changes unnecessarily
			applyChanges();
		}

		this.client.openScreen(parent);
	}

	private void dropChangesAndClose() {
		dropChanges = true;
		onClose();
	}

	private void applyChanges() {
		ShaderPackListWidget.BaseEntry base = this.shaderPackList.getSelected();

		if (!(base instanceof ShaderPackListWidget.ShaderPackEntry)) {
			return;
		}

		ShaderPackListWidget.ShaderPackEntry entry = (ShaderPackListWidget.ShaderPackEntry)base;
		String name = entry.getPackName();
		Iris.getIrisConfig().setShaderPackName(name);

		try {
			Iris.reload();
		} catch (IOException e) {
			Iris.logger.error("Error reloading shader pack while applying changes!");
			Iris.logger.catching(e);
		}
	}

	private void openShaderPackFolder() {
		Util.getOperatingSystem().open(Iris.SHADERPACKS_DIRECTORY.toFile());
	}
}
