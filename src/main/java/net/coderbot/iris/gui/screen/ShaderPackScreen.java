package net.coderbot.iris.gui.screen;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.element.ShaderPackSelectionList;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.io.FileUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ShaderPackScreen extends Screen implements HudHideable {
	private static final Component SELECT_TITLE = new TranslatableComponent("pack.iris.select.title").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

	private final Screen parent;

	private ShaderPackSelectionList shaderPackList;

	private Component addedPackDialog = null;
	private int addedPackDialogTimer = 0;

	private boolean dropChanges = false;

	public ShaderPackScreen(Screen parent) {
		super(new TranslatableComponent("options.iris.shaderPackSelection.title"));

		this.parent = parent;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
		if (this.minecraft.level == null) {
			this.renderBackground(poseStack);
		} else {
			this.fillGradient(poseStack, 0, 0, width, height, 0x4F232323, 0x4F232323);
		}

		this.shaderPackList.render(poseStack, mouseX, mouseY, delta);

		super.render(poseStack, mouseX, mouseY, delta);

		drawCenteredString(poseStack, this.font, this.title, (int)(this.width * 0.5), 8, 0xFFFFFF);

		if (addedPackDialog != null && addedPackDialogTimer > 0) {
			drawCenteredString(poseStack, this.font, addedPackDialog, (int)(this.width * 0.5), 21, 0xFFFFFF);
		} else {
			drawCenteredString(poseStack, this.font, SELECT_TITLE, (int)(this.width * 0.5), 21, 0xFFFFFF);
		}
	}

	@Override
	protected void init() {
		super.init();
		int bottomCenter = this.width / 2 - 50;
		int topCenter = this.width / 2 - 76;
		boolean inWorld = this.minecraft.level != null;

		this.shaderPackList = new ShaderPackSelectionList(this.minecraft, this.width, this.height, 32, this.height - 58, 0, this.width);

		if (inWorld) {
			this.shaderPackList.setRenderBackground(false);
		}

		this.addRenderableWidget(shaderPackList);

		this.addRenderableWidget(new Button(bottomCenter + 104, this.height - 27, 100, 20,
			CommonComponents.GUI_DONE, button -> onClose()));

		this.addRenderableWidget(new Button(bottomCenter, this.height - 27, 100, 20,
			new TranslatableComponent("options.iris.apply"), button -> this.applyChanges()));

		this.addRenderableWidget(new Button(bottomCenter - 104, this.height - 27, 100, 20,
			CommonComponents.GUI_CANCEL, button -> this.dropChangesAndClose()));

		this.addRenderableWidget(new Button(topCenter - 78, this.height - 51, 152, 20,
			new TranslatableComponent("options.iris.openShaderPackFolder"), button -> openShaderPackFolder()));

		this.addRenderableWidget(new Button(topCenter + 78, this.height - 51, 152, 20,
			new TranslatableComponent("options.iris.refreshShaderPacks"), button -> this.shaderPackList.refresh()));
	}

	@Override
	public void tick() {
		super.tick();

		if (this.addedPackDialogTimer > 0) {
			this.addedPackDialogTimer--;
		}
	}

	@Override
	public void onFilesDrop(List<Path> paths) {
		List<Path> packs = paths.stream().filter(Iris::isValidShaderpack).collect(Collectors.toList());

		for (Path pack : packs) {
			String fileName = pack.getFileName().toString();

			try {
				Iris.getShaderpacksDirectoryManager().copyPackIntoDirectory(fileName, pack);
			} catch (FileAlreadyExistsException e) {
				this.addedPackDialog = new TranslatableComponent(
						"options.iris.shaderPackSelection.copyErrorAlreadyExists",
						fileName
				).withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);

				this.addedPackDialogTimer = 100;
				this.shaderPackList.refresh();

				return;
			} catch (IOException e) {
				Iris.logger.warn("Error copying dragged shader pack", e);

				this.addedPackDialog = new TranslatableComponent(
						"options.iris.shaderPackSelection.copyError",
						fileName
				).withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);

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

				this.addedPackDialog = new TranslatableComponent(
					"options.iris.shaderPackSelection.failedAddSingle",
					fileName
				).withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
			} else {
				// Otherwise, show a generic message.

				this.addedPackDialog = new TranslatableComponent(
					"options.iris.shaderPackSelection.failedAdd"
				).withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
			}

		} else if (packs.size() == 1) {
			// In most cases, users will drag a single pack into the selection menu. So, let's special case it.
			String packName = packs.get(0).getFileName().toString();

			this.addedPackDialog = new TranslatableComponent(
					"options.iris.shaderPackSelection.addedPack",
					packName
			).withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW);

			// Select the pack that the user just added, since if a user just dragged a pack in, they'll probably want
			// to actually use that pack afterwards.
			this.shaderPackList.select(packName);
		} else {
			// We also support multiple packs being dragged and dropped at a time. Just show a generic success message
			// in that case.
			this.addedPackDialog = new TranslatableComponent(
					"options.iris.shaderPackSelection.addedPacks",
					packs.size()
			).withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW);
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

		this.minecraft.setScreen(parent);
	}

	private void dropChangesAndClose() {
		dropChanges = true;
		onClose();
	}

	private void applyChanges() {
		ShaderPackSelectionList.BaseEntry base = this.shaderPackList.getSelected();

		if (!(base instanceof ShaderPackSelectionList.ShaderPackEntry)) {
			return;
		}

		ShaderPackSelectionList.ShaderPackEntry entry = (ShaderPackSelectionList.ShaderPackEntry)base;
		String name = entry.getPackName();
		Iris.getIrisConfig().setShaderPackName(name);

		boolean enabled = this.shaderPackList.getEnableShadersButton().enabled;
		IrisApi.getInstance().getConfig().setShadersEnabledAndApply(enabled);
	}

	private void openShaderPackFolder() {
		Util.getPlatform().openUri(Iris.getShaderpacksDirectoryManager().getDirectoryUri());
	}
}
