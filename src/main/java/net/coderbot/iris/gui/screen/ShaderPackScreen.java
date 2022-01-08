package net.coderbot.iris.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.ShaderPackOptionList;
import net.coderbot.iris.gui.element.ShaderPackSelectionList;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.CommentedElementWidget;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ShaderPackScreen extends Screen implements HudHideable {
	/**
	 * Queue rendering to happen on top of all elements. Useful for tooltips or dialogs.
	 */
	public static final Set<Runnable> TOP_LAYER_RENDER_QUEUE = new HashSet<>();

	private static final Component SELECT_TITLE = new TranslatableComponent("pack.iris.select.title").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
	private static final Component CONFIGURE_TITLE = new TranslatableComponent("pack.iris.configure.title").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
	private static final int COMMENT_PANEL_WIDTH = 314;

	private final Screen parent;

	private ShaderPackSelectionList shaderPackList;

	private @Nullable ShaderPackOptionList shaderOptionList = null;
	private @Nullable NavigationController navigation = null;
	private Button screenSwitchButton;

	private Component addedPackDialog = null;
	private int addedPackDialogTimer = 0;

	private @Nullable AbstractElementWidget hoveredElement = null;
	private Optional<Component> hoveredElementCommentTitle = Optional.empty();
	private List<FormattedCharSequence> hoveredElementCommentBody = new ArrayList<>();
	private int hoveredElementCommentTimer = 0;

	private boolean optionMenuOpen = false;

	private boolean dropChanges = false;

	public ShaderPackScreen(Screen parent) {
		super(new TranslatableComponent("options.iris.shaderPackSelection.title"));

		this.parent = parent;
		refreshForChangedPack();
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
		if (this.minecraft.level == null) {
			this.renderBackground(poseStack);
		} else {
			this.fillGradient(poseStack, 0, 0, width, height, 0x4F232323, 0x4F232323);
		}

		if (optionMenuOpen && this.shaderOptionList != null) {
			this.shaderOptionList.render(poseStack, mouseX, mouseY, delta);
		} else {
			this.shaderPackList.render(poseStack, mouseX, mouseY, delta);
		}

		super.render(poseStack, mouseX, mouseY, delta);

		super.render(poseStack, mouseX, mouseY, delta);

		drawCenteredString(poseStack, this.font, this.title, (int)(this.width * 0.5), 8, 0xFFFFFF);

		if (addedPackDialog != null && addedPackDialogTimer > 0) {
			drawCenteredString(poseStack, this.font, addedPackDialog, (int)(this.width * 0.5), 21, 0xFFFFFF);
		} else {
			if (optionMenuOpen) {
				drawCenteredString(poseStack, this.font, CONFIGURE_TITLE, (int)(this.width * 0.5), 21, 0xFFFFFF);
			} else {
				drawCenteredString(poseStack, this.font, SELECT_TITLE, (int)(this.width * 0.5), 21, 0xFFFFFF);
			}
		}

		// Draw the comment panel
		if (this.isDisplayingComment()) {
			// Determine panel height and position
			int panelHeight = Math.max(50, 18 + (this.hoveredElementCommentBody.size() * 10));
			int x = (int)(0.5 * this.width) - 157;
			int y = this.height - (panelHeight + 4);
			// Draw panel
			GuiUtil.drawPanel(poseStack, x, y, COMMENT_PANEL_WIDTH, panelHeight);
			// Draw text
			this.font.drawShadow(poseStack, this.hoveredElementCommentTitle.orElse(TextComponent.EMPTY), x + 4, y + 4, 0xFFFFFF);
			for (int i = 0; i < this.hoveredElementCommentBody.size(); i++) {
				this.font.drawShadow(poseStack, this.hoveredElementCommentBody.get(i), x + 4, (y + 16) + (i * 10), 0xFFFFFF);
			}
		}

		// Render everything queued to render last
		for (Runnable render : TOP_LAYER_RENDER_QUEUE) {
			render.run();
		}
		TOP_LAYER_RENDER_QUEUE.clear();
	}

	@Override
	protected void init() {
		super.init();
		int bottomCenter = this.width / 2 - 50;
		int topCenter = this.width / 2 - 76;
		boolean inWorld = this.minecraft.level != null;

		this.removeWidget(this.shaderPackList);
		this.removeWidget(this.shaderOptionList);

		this.shaderPackList = new ShaderPackSelectionList(this.minecraft, this.width, this.height, 32, this.height - 58, 0, this.width);

		if (Iris.getCurrentPack().isPresent() && this.navigation != null) {
			ShaderPack currentPack = Iris.getCurrentPack().get();

			this.shaderOptionList = new ShaderPackOptionList(this, this.navigation, currentPack, this.minecraft, this.width, this.height, 32, this.height - 58, 0, this.width);
			this.navigation.setActiveOptionList(this.shaderOptionList);

			this.shaderOptionList.rebuild();
		} else {
			optionMenuOpen = false;
			this.shaderOptionList = null;
		}

		if (inWorld) {
			this.shaderPackList.setRenderBackground(false);
			if (shaderOptionList != null) {
				this.shaderOptionList.setRenderBackground(false);
			}
		}

		this.clearWidgets();

		if (optionMenuOpen && shaderOptionList != null) {
			this.addRenderableWidget(shaderOptionList);
		} else {
			this.addRenderableWidget(shaderPackList);
		}

		this.addRenderableWidget(new Button(bottomCenter + 104, this.height - 27, 100, 20,
			CommonComponents.GUI_DONE, button -> onClose()));

		this.addRenderableWidget(new Button(bottomCenter, this.height - 27, 100, 20,
			new TranslatableComponent("options.iris.apply"), button -> this.applyChanges()));

		this.addRenderableWidget(new Button(bottomCenter - 104, this.height - 27, 100, 20,
			CommonComponents.GUI_CANCEL, button -> this.dropChangesAndClose()));

		this.addRenderableWidget(new Button(topCenter - 78, this.height - 51, 152, 20,
			new TranslatableComponent("options.iris.openShaderPackFolder"), button -> openShaderPackFolder()));

		this.screenSwitchButton = this.addRenderableWidget(new Button(topCenter + 78, this.height - 51, 152, 20,
			new TranslatableComponent("options.iris.shaderPackList"), button -> {
				this.optionMenuOpen = !this.optionMenuOpen;
				this.init();
			}
		));

		refreshScreenSwitchButton();
	}

	public void refreshForChangedPack() {
		if (Iris.getCurrentPack().isPresent()) {
			ShaderPack currentPack = Iris.getCurrentPack().get();

			this.navigation = new NavigationController(currentPack.getMenuContainer());

			if (this.shaderOptionList != null) {
				this.shaderOptionList.applyShaderPack(currentPack);
				this.shaderOptionList.rebuild();
			}
		} else {
			this.navigation = null;
		}

		refreshScreenSwitchButton();
	}

	public void refreshScreenSwitchButton() {
		if (this.screenSwitchButton != null) {
			this.screenSwitchButton.setMessage(
					optionMenuOpen ?
							new TranslatableComponent("options.iris.shaderPackList")
							: new TranslatableComponent("options.iris.shaderPackSettings")
			);
			this.screenSwitchButton.active = optionMenuOpen || Iris.getCurrentPack().isPresent();
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (this.addedPackDialogTimer > 0) {
			this.addedPackDialogTimer--;
		}

		if (this.hoveredElement != null) {
			this.hoveredElementCommentTimer++;
		} else {
			this.hoveredElementCommentTimer = 0;
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
		} else {
			discardChanges();
		}

		this.minecraft.setScreen(parent);
	}

	private void dropChangesAndClose() {
		dropChanges = true;
		onClose();
	}

	public void applyChanges() {
		ShaderPackSelectionList.BaseEntry base = this.shaderPackList.getSelected();

		if (!(base instanceof ShaderPackSelectionList.ShaderPackEntry)) {
			return;
		}

		ShaderPackSelectionList.ShaderPackEntry entry = (ShaderPackSelectionList.ShaderPackEntry)base;
		this.shaderPackList.setApplied(entry);

		String name = entry.getPackName();

		// If the pack is being changed, clear pending options from the previous pack to
		// avoid possible undefined behavior from applying one pack's options to another pack
		if (!name.equals(Iris.getCurrentPackName())) {
			Iris.clearShaderPackOptionQueue();
		}

		Iris.getIrisConfig().setShaderPackName(name);

		boolean enabled = this.shaderPackList.getTopButtonRow().shadersEnabled;
		IrisApi.getInstance().getConfig().setShadersEnabledAndApply(enabled);

		refreshForChangedPack();
	}

	private void discardChanges() {
		Iris.clearShaderPackOptionQueue();
	}

	private void openShaderPackFolder() {
		Util.getPlatform().openUri(Iris.getShaderpacksDirectoryManager().getDirectoryUri());
	}

	// Let the screen know if an element is hovered or not, allowing for accurately updating which element is hovered
	public void setElementHoveredStatus(AbstractElementWidget widget, boolean hovered) {
		if (hovered && widget != this.hoveredElement) {
			this.hoveredElement = widget;

			if (widget instanceof CommentedElementWidget) {
				this.hoveredElementCommentTitle = ((CommentedElementWidget<?>) widget).getCommentTitle();

				Optional<Component> commentBody = ((CommentedElementWidget<?>) widget).getCommentBody();
				if (!commentBody.isPresent()) {
					this.hoveredElementCommentBody.clear();
				} else {
					String rawCommentBody = commentBody.get().getString();

					// Strip any trailing "."s
					if (rawCommentBody.endsWith(".")) {
						rawCommentBody = rawCommentBody.substring(0, rawCommentBody.length() - 1);
					}
					// Split comment body into lines by separator ". "
					List<MutableComponent> splitByPeriods = Arrays.stream(rawCommentBody.split("\\. [ ]*")).map(TextComponent::new).collect(Collectors.toList());
					// Line wrap
					this.hoveredElementCommentBody = new ArrayList<>();
					for (MutableComponent text : splitByPeriods) {
						this.hoveredElementCommentBody.addAll(this.font.split(text, COMMENT_PANEL_WIDTH - 8));
					}
				}
			} else {
				this.hoveredElementCommentTitle = Optional.empty();
				this.hoveredElementCommentBody.clear();
			}

			this.hoveredElementCommentTimer = 0;
		} else if (!hovered && widget == this.hoveredElement) {
			this.hoveredElement = null;
			this.hoveredElementCommentTitle = Optional.empty();
			this.hoveredElementCommentBody.clear();
			this.hoveredElementCommentTimer = 0;
		}
	}

	public boolean isDisplayingComment() {
		return this.hoveredElementCommentTimer > 20 &&
				this.hoveredElementCommentTitle.isPresent() &&
				!this.hoveredElementCommentBody.isEmpty();
	}
}
