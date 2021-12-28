package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.OptionSet;
import net.coderbot.iris.shaderpack.option.Profile;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuProfileElement;
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

public class ProfileElementWidget extends BaseOptionElementWidget<OptionMenuProfileElement> {
	private static final MutableComponent PROFILE_LABEL = new TranslatableComponent("options.iris.profile");
	private static final MutableComponent PROFILE_CUSTOM = new TranslatableComponent("options.iris.profile.custom").withStyle(ChatFormatting.YELLOW);

	private Profile next;
	private Profile previous;
	private Component profileLabel;

	public ProfileElementWidget(OptionMenuProfileElement element) {
		super(element);
	}

	@Override
	public void init(ShaderPackScreen screen, NavigationController navigation) {
		super.init(screen, navigation);
		this.setLabel(PROFILE_LABEL);

		Map<String, Profile> profiles = this.element.profiles;
		OptionSet options = this.element.options;
		OptionValues pendingValues = this.element.getPendingOptionValues();

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
	public String getCommentKey() {
		return "profile.comment";
	}

	@Override
	public boolean applyNextValue() {
		Iris.queueShaderPackOptionsFromProfile(this.next);

		return true;
	}

	@Override
	public boolean applyPreviousValue() {
		Iris.queueShaderPackOptionsFromProfile(this.previous);

		return true;
	}

	@Override
	public boolean applyOriginalValue() {
		return false; // Resetting options is the way to return to the "default profile"
	}

	@Override
	public boolean isValueModified() {
		return false;
	}
}
