package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.ProfileElementWidget;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.Profile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OptionMenuProfileElement extends OptionMenuElement {
	private final Profile previous;
	private final Profile next;
	private final @Nullable String currentProfileName;

	public OptionMenuProfileElement(@Nullable String profileName, Map<String, Profile> profiles) {
		this.currentProfileName = profileName;

		List<String> profileNames = new ArrayList<>(profiles.keySet());

		if (profileName != null) {
			int currentIndex = profileNames.indexOf(profileName);

			int prevIndex = Math.floorMod(currentIndex - 1, profileNames.size());
			int nextIndex = Math.floorMod(currentIndex + 1, profileNames.size());

			this.previous = profiles.get(profileNames.get(prevIndex));
			this.next = profiles.get(profileNames.get(nextIndex));
		} else {
			this.previous = profiles.get(profileNames.get(profileNames.size() - 1));
			this.next = profiles.get(profileNames.get(0));
		}
	}

	@Override
	public AbstractElementWidget createWidget(ShaderPackScreen screen, NavigationController navigation) {
		return new ProfileElementWidget(screen, this.currentProfileName, this.next, this.previous);
	}
}
