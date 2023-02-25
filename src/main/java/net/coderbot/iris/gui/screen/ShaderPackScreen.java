package net.coderbot.iris.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.GLDebug;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;


import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ShaderPackScreen extends Screen implements HudHideable {
	/**
	 * Queue rendering to happen on top of all elements. Useful for tooltips or dialogs.
	 */
	public static final Set<Runnable> TOP_LAYER_RENDER_QUEUE = new HashSet<>();

	private static final Component SELECT_TITLE = Component.translatable("pack.iris.select.title").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
	private static final Component CONFIGURE_TITLE = Component.translatable("pack.iris.configure.title").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
	private static final int COMMENT_PANEL_WIDTH = 314;

	private final Screen parent;
	private final MutableComponent irisTextComponent;

	private ShaderPackSelectionList shaderPackList;

	private @Nullable ShaderPackOptionList shaderOptionList = null;
	private @Nullable NavigationController navigation = null;
	private Button screenSwitchButton;

	private Component notificationDialog = null;
	private int notificationDialogTimer = 0;

	private @Nullable AbstractElementWidget<?> hoveredElement = null;
	private Optional<Component> hoveredElementCommentTitle = Optional.empty();
	private List<FormattedCharSequence> hoveredElementCommentBody = new ArrayList<>();
	private int hoveredElementCommentTimer = 0;

	private boolean optionMenuOpen = false;

	private boolean dropChanges = false;
	private static String development = "Development Environment";
	private MutableComponent developmentComponent;
	private MutableComponent updateComponent;

	private boolean guiHidden = false;
	private float guiButtonHoverTimer = 0.0f;

	public ShaderPackScreen(Screen parent) {
		super(Component.translatable("options.iris.shaderPackSelection.title"));

		this.parent = parent;

		String irisName = Iris.MODNAME + " " + Iris.getVersion();

		if (irisName.contains("-development-environment")) {
			this.developmentComponent = Component.literal("Development Environment").withStyle(ChatFormatting.GOLD);
			irisName = irisName.replace("-development-environment", "");
		}

		this.irisTextComponent = Component.literal(irisName).withStyle(ChatFormatting.GRAY);

		if (Iris.getUpdateChecker().getUpdateMessage().isPresent()) {
			this.updateComponent = Component.literal("New update available!").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.UNDERLINE);
			irisTextComponent.append(Component.literal(" (outdated)").withStyle(ChatFormatting.RED));
		}

		refreshForChangedPack();
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
		if (this.minecraft.level == null) {
			this.renderBackground(poseStack);
		} else if (!this.guiHidden) {
			this.fillGradient(poseStack, 0, 0, width, height, 0x4F232323, 0x4F232323);
		}

		if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_D)) {
			Minecraft.getInstance().setScreen(new ConfirmScreen((option) -> {
				Iris.setDebug(option);
				Minecraft.getInstance().setScreen(this);
			}, Component.literal("Shader debug mode toggle"),
				Component.literal("Debug mode helps investigate problems and shows shader errors. Would you like to enable it?"),
				Component.literal("Yes"),
				Component.literal("No")));
		}

		if (!this.guiHidden) {
			if (optionMenuOpen && this.shaderOptionList != null) {
				this.shaderOptionList.render(poseStack, mouseX, mouseY, delta);
			} else {
				this.shaderPackList.render(poseStack, mouseX, mouseY, delta);
			}
		}

		float previousHoverTimer = this.guiButtonHoverTimer;
		super.render(poseStack, mouseX, mouseY, delta);
		if (previousHoverTimer == this.guiButtonHoverTimer) {
			this.guiButtonHoverTimer = 0.0f;
		}

		if (!this.guiHidden) {
			drawCenteredString(poseStack, this.font, this.title, (int) (this.width * 0.5), 8, 0xFFFFFF);

			if (notificationDialog != null && notificationDialogTimer > 0) {
				drawCenteredString(poseStack, this.font, notificationDialog, (int) (this.width * 0.5), 21, 0xFFFFFF);
			} else {
				if (optionMenuOpen) {
					drawCenteredString(poseStack, this.font, CONFIGURE_TITLE, (int) (this.width * 0.5), 21, 0xFFFFFF);
				} else {
					drawCenteredString(poseStack, this.font, SELECT_TITLE, (int) (this.width * 0.5), 21, 0xFFFFFF);
				}
			}

			// Draw the comment panel
			if (this.isDisplayingComment()) {
				// Determine panel height and position
				int panelHeight = Math.max(50, 18 + (this.hoveredElementCommentBody.size() * 10));
				int x = (int) (0.5 * this.width) - 157;
				int y = this.height - (panelHeight + 4);
				// Draw panel
				GuiUtil.drawPanel(poseStack, x, y, COMMENT_PANEL_WIDTH, panelHeight);
				// Draw text
				this.font.drawShadow(poseStack, this.hoveredElementCommentTitle.orElse(Component.empty()), x + 4, y + 4, 0xFFFFFF);
				for (int i = 0; i < this.hoveredElementCommentBody.size(); i++) {
					this.font.drawShadow(poseStack, this.hoveredElementCommentBody.get(i), x + 4, (y + 16) + (i * 10), 0xFFFFFF);
				}
			}
		}

		// Render everything queued to render last
		for (Runnable render : TOP_LAYER_RENDER_QUEUE) {
			render.run();
		}
		TOP_LAYER_RENDER_QUEUE.clear();

		if (this.developmentComponent != null) {
			this.font.drawShadow(poseStack, developmentComponent, 2, this.height - 10, 0xFFFFFF);
			this.font.drawShadow(poseStack, irisTextComponent, 2, this.height - 20, 0xFFFFFF);
		} else if (this.updateComponent != null) {
			this.font.drawShadow(poseStack, updateComponent, 2, this.height - 10, 0xFFFFFF);
			this.font.drawShadow(poseStack, irisTextComponent, 2, this.height - 20, 0xFFFFFF);
		} else {
			this.font.drawShadow(poseStack, irisTextComponent, 2, this.height - 10, 0xFFFFFF);
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		int widthValue = this.font.width("New update available!");
		if (this.updateComponent != null && d < widthValue && e > (this.height - 10) && e < this.height) {
			this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
				if (bl) {
					Iris.getUpdateChecker().getUpdateLink().ifPresent(Util.getPlatform()::openUri);
				}
				this.minecraft.setScreen(this);
			}, Iris.getUpdateChecker().getUpdateLink().orElse(""), true));
		}
		return super.mouseClicked(d, e, i);
	}

	@Override
	protected void init() {
		super.init();
		int bottomCenter = this.width / 2 - 50;
		int topCenter = this.width / 2 - 76;
		boolean inWorld = this.minecraft.level != null;

		this.removeWidget(this.shaderPackList);
		this.removeWidget(this.shaderOptionList);

		this.shaderPackList = new ShaderPackSelectionList(this, this.minecraft, this.width, this.height, 32, this.height - 58, 0, this.width);

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

		if (!this.guiHidden) {
			if (optionMenuOpen && shaderOptionList != null) {
				this.addRenderableWidget(shaderOptionList);
			} else {
				this.addRenderableWidget(shaderPackList);
			}

			this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).bounds(bottomCenter + 104, this.height - 27, 100, 20
				).build());

			this.addRenderableWidget(Button.builder(Component.translatable("options.iris.apply"), button -> this.applyChanges()).bounds(bottomCenter, this.height - 27, 100, 20
				).build());

			this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.dropChangesAndClose()).bounds(bottomCenter - 104, this.height - 27, 100, 20
				).build());

			this.addRenderableWidget(Button.builder(Component.translatable("options.iris.openShaderPackFolder"), button -> openShaderPackFolder()).bounds(topCenter - 78, this.height - 51, 152, 20
				).build());

			this.screenSwitchButton = this.addRenderableWidget(Button.builder(Component.translatable("options.iris.shaderPackList"), button -> {
					this.optionMenuOpen = !this.optionMenuOpen;

					// UX: Apply changes before switching screens to avoid unintuitive behavior
					//
					// Not doing this leads to unintuitive behavior, since selecting a pack in the
					// list (but not applying) would open the settings for the previous pack, rather
					// than opening the settings for the selected (but not applied) pack.
					this.applyChanges();

					this.init();
				}
			).bounds(topCenter + 78, this.height - 51, 152, 20
				).build());

			refreshScreenSwitchButton();
		}

		if (inWorld) {
			Component showOrHide = this.guiHidden
				? Component.translatable("options.iris.gui.show")
				: Component.translatable("options.iris.gui.hide");

			float endOfLastButton = this.width / 2.0f + 154.0f;
			float freeSpace = this.width - endOfLastButton;
			int x;
			if (freeSpace > 100.0f) {
				x = this.width - 50;
			} else if (freeSpace < 20.0f) {
				x = this.width - 20;
			} else {
				x = (int) (endOfLastButton + (freeSpace / 2.0f)) - 10;
			}

			ImageButton showHideButton = new ImageButton(
				x, this.height - 39,
				20, 20,
				this.guiHidden ? 20 : 0, 146, 20,
				GuiUtil.IRIS_WIDGETS_TEX,
				256, 256,
				(button) -> {
					this.guiHidden = !this.guiHidden;
					this.init();
				},
				showOrHide
			);

			showHideButton.setTooltip(Tooltip.create(showOrHide));
			showHideButton.setTooltipDelay(10);

			this.addRenderableWidget(showHideButton);
		}

		// NB: Don't let comment remain when exiting options screen
		// https://github.com/IrisShaders/Iris/issues/1494
		this.hoveredElement = null;
		this.hoveredElementCommentTimer = 0;
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
							Component.translatable("options.iris.shaderPackList")
							: Component.translatable("options.iris.shaderPackSettings")
			);
			this.screenSwitchButton.active = optionMenuOpen || shaderPackList.getTopButtonRow().shadersEnabled;
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (this.notificationDialogTimer > 0) {
			this.notificationDialogTimer--;
		}

		if (this.hoveredElement != null) {
			this.hoveredElementCommentTimer++;
		} else {
			this.hoveredElementCommentTimer = 0;
		}
	}

	@Override
	public boolean keyPressed(int key, int j, int k) {
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			if (this.guiHidden) {
				this.guiHidden = false;
				this.init();

				return true;
			} else if (this.navigation != null && this.navigation.hasHistory()) {
				this.navigation.back();

				return true;
			} else if (this.optionMenuOpen) {
				this.optionMenuOpen = false;
				this.init();

				return true;
			}
		}

		return this.guiHidden || super.keyPressed(key, j, k);
	}

	@Override
	public void onFilesDrop(List<Path> paths) {
		if (this.optionMenuOpen) {
			onOptionMenuFilesDrop(paths);
		} else {
			onPackListFilesDrop(paths);
		}
	}

	public void onPackListFilesDrop(List<Path> paths) {
		List<Path> packs = paths.stream().filter(Iris::isValidShaderpack).collect(Collectors.toList());

		for (Path pack : packs) {
			String fileName = pack.getFileName().toString();

			try {
				Iris.getShaderpacksDirectoryManager().copyPackIntoDirectory(fileName, pack);
			} catch (FileAlreadyExistsException e) {
				this.notificationDialog = Component.translatable(
						"options.iris.shaderPackSelection.copyErrorAlreadyExists",
						fileName
				).withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);

				this.notificationDialogTimer = 100;
				this.shaderPackList.refresh();

				return;
			} catch (IOException e) {
				Iris.logger.warn("Error copying dragged shader pack", e);

				this.notificationDialog = Component.translatable(
						"options.iris.shaderPackSelection.copyError",
						fileName
				).withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);

				this.notificationDialogTimer = 100;
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

				this.notificationDialog = Component.translatable(
					"options.iris.shaderPackSelection.failedAddSingle",
					fileName
				).withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
			} else {
				// Otherwise, show a generic message.

				this.notificationDialog = Component.translatable(
					"options.iris.shaderPackSelection.failedAdd"
				).withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
			}

		} else if (packs.size() == 1) {
			// In most cases, users will drag a single pack into the selection menu. So, let's special case it.
			String packName = packs.get(0).getFileName().toString();

			this.notificationDialog = Component.translatable(
					"options.iris.shaderPackSelection.addedPack",
					packName
			).withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW);

			// Select the pack that the user just added, since if a user just dragged a pack in, they'll probably want
			// to actually use that pack afterwards.
			this.shaderPackList.select(packName);
		} else {
			// We also support multiple packs being dragged and dropped at a time. Just show a generic success message
			// in that case.
			this.notificationDialog = Component.translatable(
					"options.iris.shaderPackSelection.addedPacks",
					packs.size()
			).withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW);
		}

		// Show the relevant message for 5 seconds (100 ticks)
		this.notificationDialogTimer = 100;
	}

	public void displayNotification(Component component) {
		this.notificationDialog = component;
		this.notificationDialogTimer = 100;
	}

	public void onOptionMenuFilesDrop(List<Path> paths) {
		// If more than one option file has been dragged, display an error
		// as only one option file should be imported at a time
		if (paths.size() != 1) {
			this.notificationDialog = Component.translatable(
					"options.iris.shaderPackOptions.tooManyFiles"
			).withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
			this.notificationDialogTimer = 100; // 5 seconds (100 ticks)

			return;
		}

		this.importPackOptions(paths.get(0));
	}

	public void importPackOptions(Path settingFile) {
		try (InputStream in = Files.newInputStream(settingFile)) {
			Properties properties = new Properties();
			properties.load(in);

			Iris.queueShaderPackOptionsFromProperties(properties);

			this.notificationDialog = Component.translatable(
					"options.iris.shaderPackOptions.importedSettings",
					settingFile.getFileName().toString()
			).withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW);
			this.notificationDialogTimer = 100; // 5 seconds (100 ticks)

			if (this.navigation != null) {
				this.navigation.refresh();
			}
		} catch (Exception e) {
			// If the file could not be properly parsed or loaded,
			// log the error and display a message to the user
			Iris.logger.error("Error importing shader settings file \""+ settingFile.toString() +"\"", e);

			this.notificationDialog = Component.translatable(
					"options.iris.shaderPackOptions.failedImport",
					settingFile.getFileName().toString()
			).withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
			this.notificationDialogTimer = 100; // 5 seconds (100 ticks)
		}
	}

	@Override
	public void onClose() {
		if (!dropChanges) {
			applyChanges();
		} else {
			discardChanges();
		}

		try {
			shaderPackList.close();
		} catch (IOException e) {
			Iris.logger.error("Failed to safely close shaderpack selection!", e);
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

		boolean enabled = this.shaderPackList.getTopButtonRow().shadersEnabled;

		String previousPackName = Iris.getIrisConfig().getShaderPackName().orElse(null);
		boolean previousShadersEnabled = Iris.getIrisConfig().areShadersEnabled();

		// Only reload if the pack would be different from before, or shaders were toggled, or options were changed, or if we're about to reset options.
		if (!name.equals(previousPackName) || enabled != previousShadersEnabled || !Iris.getShaderPackOptionQueue().isEmpty() || Iris.shouldResetShaderPackOptionsOnNextReload()) {
			Iris.getIrisConfig().setShaderPackName(name);
			IrisApi.getInstance().getConfig().setShadersEnabledAndApply(enabled);
		}

		refreshForChangedPack();
	}

	private void discardChanges() {
		Iris.clearShaderPackOptionQueue();
	}

	private void openShaderPackFolder() {
		CompletableFuture.runAsync(() -> {
			Util.getPlatform().openUri(Iris.getShaderpacksDirectoryManager().getDirectoryUri());
		});
	}

	// Let the screen know if an element is hovered or not, allowing for accurately updating which element is hovered
	public void setElementHoveredStatus(AbstractElementWidget<?> widget, boolean hovered) {
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
					List<MutableComponent> splitByPeriods = Arrays.stream(rawCommentBody.split("\\. [ ]*")).map(Component::literal).collect(Collectors.toList());
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
