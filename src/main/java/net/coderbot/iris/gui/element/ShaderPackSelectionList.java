package net.coderbot.iris.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collection;
import java.util.function.Function;

public class ShaderPackSelectionList extends IrisObjectSelectionList<ShaderPackSelectionList.BaseEntry> {
	private static final Component PACK_LIST_LABEL = new TranslatableComponent("pack.iris.list.label").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);

	private final TopButtonRowEntry topButtonRow;
	private ShaderPackEntry applied = null;

	public ShaderPackSelectionList(Minecraft client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 20);
		this.topButtonRow = new TopButtonRowEntry(this, Iris.getIrisConfig().areShadersEnabled());

		refresh();
	}

	@Override
	public int getRowWidth() {
		return Math.min(308, width - 50);
	}

	@Override
	protected int getRowTop(int index) {
		return super.getRowTop(index) + 2;
	}

	public void refresh() {
		this.clearEntries();

		Collection<String> names;

		try {
			names = Iris.getShaderpacksDirectoryManager().enumerate();
		} catch (Throwable e) {
			Iris.logger.error("Error reading files while constructing selection UI", e);

			// Not translating this since it's going to be seen very rarely,
			// We're just trying to get more information on a seemingly untraceable bug:
			// - https://github.com/IrisShaders/Iris/issues/785
			this.addLabelEntries(
					TextComponent.EMPTY,
					new TextComponent("There was an error reading your shaderpacks directory")
							.withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
					TextComponent.EMPTY,
					new TextComponent("Check your logs for more information."),
					new TextComponent("Please file an issue report including a log file."),
					new TextComponent("If you are able to identify the file causing this, " +
											 "please include it in your report as well."),
					new TextComponent("Note that this might be an issue with folder " +
											 "permissions; ensure those are correct first.")
			);

			return;
		}

		this.addEntry(topButtonRow);

		// Only allow the enable/disable shaders button if the user has
		// added a shader pack. Otherwise, the button will be disabled.
		topButtonRow.allowEnableShadersButton = names.size() > 0;

		int index = 0;

		for (String name : names) {
			index++;
			addPackEntry(index, name);
		}

		this.addLabelEntries(PACK_LIST_LABEL);
	}

	public void addPackEntry(int index, String name) {
		ShaderPackEntry entry = new ShaderPackEntry(index, this, name);

		Iris.getIrisConfig().getShaderPackName().ifPresent(currentPackName -> {
			if (name.equals(currentPackName)) {
				setSelected(entry);
				setApplied(entry);
			}
		});

		this.addEntry(entry);
	}

	public void addLabelEntries(Component ... lines) {
		for (Component text : lines) {
			this.addEntry(new LabelEntry(text));
		}
	}

	public void select(String name) {
		for (int i = 0; i < getItemCount(); i++) {
			BaseEntry entry = getEntry(i);

			if (entry instanceof ShaderPackEntry && ((ShaderPackEntry)entry).packName.equals(name)) {
				setSelected(entry);

				return;
			}
		}
	}

	public void setApplied(ShaderPackEntry entry) {
		this.applied = entry;
	}

	public ShaderPackEntry getApplied() {
		return this.applied;
	}

	public TopButtonRowEntry getTopButtonRow() {
		return topButtonRow;
	}

	public static abstract class BaseEntry extends ObjectSelectionList.Entry<BaseEntry> {
		protected BaseEntry() {}
	}

	public static class ShaderPackEntry extends BaseEntry {
		private final String packName;
		private final ShaderPackSelectionList list;
		private final int index;

		public ShaderPackEntry(int index, ShaderPackSelectionList list, String packName) {
			this.packName = packName;
			this.list = list;
			this.index = index;
		}

		public boolean isApplied() {
			return list.getApplied() == this;
		}

		public boolean isSelected() {
			return list.getSelected() == this;
		}

		public String getPackName() {
			return packName;
		}

		// Appears to be some accessibility thing
		@Override
		public Component getNarration() {
			return new TranslatableComponent("narrator.select", packName);
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			Font font = Minecraft.getInstance().font;
			int color = 0xFFFFFF;
			String name = packName;

			boolean shadersEnabled = list.getTopButtonRow().shadersEnabled;

			if (font.width(new TextComponent(name).withStyle(ChatFormatting.BOLD)) > this.list.getRowWidth() - 3) {
				name = font.plainSubstrByWidth(name, this.list.getRowWidth() - 8) + "...";
			}

			MutableComponent text = new TextComponent(name);

			if (shadersEnabled && this.isMouseOver(mouseX, mouseY)) {
				text = text.withStyle(ChatFormatting.BOLD);
			}

			if (this.isApplied()) {
				color = 0xFFF263;
			}

			if (!shadersEnabled) {
				color = 0xA2A2A2;
			}

			drawCenteredString(poseStack, font, text, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, color);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (list.getTopButtonRow().shadersEnabled && !this.isSelected() && button == 0) {
				this.list.select(this.index);

				return true;
			}

			return false;
		}
	}

	public static class LabelEntry extends BaseEntry {
		private final Component label;

		public LabelEntry(Component label) {
			this.label = label;
		}

		// Appears to be some accessibility thing
		@Override
		public Component getNarration() {
			return label;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			drawCenteredString(poseStack, Minecraft.getInstance().font, label, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xC2C2C2);
		}
	}

	public static class TopButtonRowEntry extends BaseEntry {
		private static final Component REFRESH_SHADER_PACKS_LABEL = new TranslatableComponent("options.iris.refreshShaderPacks").withStyle(style -> style.withColor(TextColor.fromRgb(0x99ceff)));
		private static final Component NONE_PRESENT_LABEL = new TranslatableComponent("options.iris.shaders.nonePresent").withStyle(ChatFormatting.GRAY);
		private static final Component SHADERS_DISABLED_LABEL = new TranslatableComponent("options.iris.shaders.disabled");
		private static final Component SHADERS_ENABLED_LABEL = new TranslatableComponent("options.iris.shaders.enabled");
		private static final int REFRESH_BUTTON_WIDTH = 18;

		private final ShaderPackSelectionList list;
		private final IrisElementRow buttons = new IrisElementRow();
		private final EnableShadersButtonElement enableDisableButton;
		private final IrisElementRow.Element refreshPacksButton;

		public boolean allowEnableShadersButton = true;
		public boolean shadersEnabled;

		public TopButtonRowEntry(ShaderPackSelectionList list, boolean shadersEnabled) {
			this.list = list;
			this.shadersEnabled = shadersEnabled;
			this.enableDisableButton = new EnableShadersButtonElement(
					getEnableDisableLabel(),
					button -> {
						if (this.allowEnableShadersButton) {
							this.shadersEnabled = !this.shadersEnabled;
							button.text = getEnableDisableLabel();

							GuiUtil.playButtonClickSound();
							return true;
						}

						return false;
					});
			this.refreshPacksButton = new IrisElementRow.IconButtonElement(
					GuiUtil.Icon.REFRESH,
					button -> {
						this.list.refresh();

						GuiUtil.playButtonClickSound();
						return true;
					});
			this.buttons.add(this.enableDisableButton, 0).add(this.refreshPacksButton, REFRESH_BUTTON_WIDTH);
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			this.buttons.setWidth(this.enableDisableButton, (entryWidth - 1) - REFRESH_BUTTON_WIDTH);
			this.enableDisableButton.centerX = x + (int)(entryWidth * 0.5);

			this.buttons.render(poseStack, x - 2, y - 3, 18, mouseX, mouseY, tickDelta, hovered);

			if (this.refreshPacksButton.isHovered()) {
				ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() ->
						GuiUtil.drawTextPanel(Minecraft.getInstance().font, poseStack, REFRESH_SHADER_PACKS_LABEL,
								(mouseX - 8) - Minecraft.getInstance().font.width(REFRESH_SHADER_PACKS_LABEL), mouseY - 16));
			}
		}

		private Component getEnableDisableLabel() {
			return this.allowEnableShadersButton ? this.shadersEnabled ? SHADERS_ENABLED_LABEL : SHADERS_DISABLED_LABEL : NONE_PRESENT_LABEL;
		}

		// Appears to be some accessibility thing
		@Override
		public Component getNarration() {
			return new TranslatableComponent("narration.button", this.shadersEnabled ? SHADERS_ENABLED_LABEL : SHADERS_DISABLED_LABEL);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return this.buttons.mouseClicked(mouseX, mouseY, button);
		}

		// Renders the label at an offset as to not look misaligned with the rest of the menu
		public static class EnableShadersButtonElement extends IrisElementRow.TextButtonElement {
			private int centerX;

			public EnableShadersButtonElement(Component text, Function<IrisElementRow.TextButtonElement, Boolean> onClick) {
				super(text, onClick);
			}

			@Override
			public void renderLabel(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
				int textX = this.centerX - (int)(this.font.width(this.text) * 0.5);
				int textY = y + (int)((height - 8) * 0.5);

				this.font.drawShadow(poseStack, this.text, textX, textY, 0xFFFFFF);
			}
		}
	}
}
