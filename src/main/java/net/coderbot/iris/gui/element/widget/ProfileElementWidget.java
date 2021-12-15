package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.OptionSet;
import net.coderbot.iris.shaderpack.option.Profile;
import net.coderbot.iris.shaderpack.option.values.MutableOptionValues;
import net.coderbot.iris.shaderpack.option.values.OptionValues;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProfileElementWidget extends BaseOptionElementWidget {
	private static final MutableComponent PROFILE_LABEL = new TranslatableComponent("options.iris.profile");
	private static final MutableComponent PROFILE_CUSTOM = new TranslatableComponent("options.iris.profile.custom").withStyle(ChatFormatting.YELLOW);

	private final Profile next;
	private final Profile previous;
	private final Component profileLabel;

	public ProfileElementWidget(ShaderPackScreen screen, NavigationController navigation, Map<String, Profile> profiles, OptionSet options, OptionValues pendingValues) {
		super(screen, navigation, PROFILE_LABEL);

		Optional<String> profileName = Optional.empty();

		List<String> indexedProfileNames = new ArrayList<>(profiles.keySet());

		Profile next = null;
		Profile previous = null;

		if (indexedProfileNames.size() > 0) {
			next = profiles.get(indexedProfileNames.get(0));
			previous = profiles.get(indexedProfileNames.get(indexedProfileNames.size() - 1));

			for (String name : profiles.keySet()) {
				if (profiles.get(name).matches(options, pendingValues)) {
					profileName = Optional.of(name);

					int profileIdx = indexedProfileNames.indexOf(name);
					next = profiles.get(indexedProfileNames.get(Math.floorMod(profileIdx + 1, indexedProfileNames.size())));
					previous = profiles.get(indexedProfileNames.get(Math.floorMod(profileIdx - 1, indexedProfileNames.size())));

					break;
				}
			}
		}

		this.next = next;
		this.previous = previous;

		this.profileLabel = profileName.map(name -> GuiUtil.translateOrDefault(new TextComponent(name), "profile." + name)).orElse(PROFILE_CUSTOM);
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		this.updateRenderParams(width, width - (Minecraft.getInstance().font.width(PROFILE_LABEL) + 16));

		this.renderOptionWithValue(poseStack, x, y, width, height, hovered);
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
			Profile toQueue = button == GLFW.GLFW_MOUSE_BUTTON_1 ? this.next : this.previous;
			if (toQueue != null) {
				Iris.queueShaderPackOptionsFromProfile(toQueue);
			}

			GuiUtil.playButtonClickSound();
			this.navigation.refresh();

			return true;
		}
		return super.mouseClicked(mx, my, button);
	}
}
