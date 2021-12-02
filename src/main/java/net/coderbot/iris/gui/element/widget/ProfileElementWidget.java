package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.Profile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class ProfileElementWidget extends BaseOptionElementWidget {
	private static final MutableComponent PROFILE_LABEL = new TranslatableComponent("options.iris.profile");
	private static final MutableComponent PROFILE_CUSTOM = new TranslatableComponent("options.iris.profile.custom").withStyle(ChatFormatting.YELLOW);
	private static final MutableComponent PROFILE_TOOLTIP = new TranslatableComponent("options.iris.profile.tooltip").withStyle(ChatFormatting.RED);

	private final Profile next;
	private final Profile previous;
	private final Component profileLabel;

	public ProfileElementWidget(ShaderPackScreen screen, Optional<String> profileName, Profile next, Profile previous) {
		super(screen, PROFILE_LABEL);

		this.next = next;
		this.previous = previous;

		this.profileLabel = profileName.map(name -> GuiUtil.translateOrDefault(new TextComponent(name), "profile." + name)).orElse(PROFILE_CUSTOM);
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		this.updateRenderParams(width, width - (Minecraft.getInstance().font.width(PROFILE_LABEL) + 16));

		this.renderOptionWithValue(poseStack, x, y, width, height, hovered);
		this.renderTooltip(poseStack, PROFILE_TOOLTIP, mouseX, mouseY, hovered);
	}

	@Override
	protected Component createValueLabel() {
		return this.profileLabel;
	}

	@Override
	public Optional<Component> getCommentTitle() {
		return Optional.of(PROFILE_LABEL);
	}

	@Override
	public Optional<Component> getCommentBody() {
		String key = "profile.comment";
		return Optional.ofNullable(I18n.exists(key) ? new TranslatableComponent(key) : null);
	}

	@Override
	public String getOptionName() {
		return "";
	}

	@Override
	public String getValue() {
		return "";
	}

	@Override
	protected void queueValueToPending() {
		// No-op implementation
	}

	@Override
	public boolean isValueOriginal() {
		return true;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1 || button == GLFW.GLFW_MOUSE_BUTTON_2) {
			Iris.addPendingShaderPackOptionsFromProfile(button == GLFW.GLFW_MOUSE_BUTTON_1 ? this.next : this.previous);
			GuiUtil.playButtonClickSound();

			this.screen.applyChanges();

			return true;
		}
		return super.mouseClicked(mx, my, button);
	}
}
