package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.ProfileElementWidget;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OptionMenuProfileElement extends OptionMenuElement {
	private final Profile previous;
	private final Profile next;
	private final Optional<String> currentProfileName;

	public OptionMenuProfileElement(Optional<String> profileName, Map<String, Profile> profiles) {
		this.currentProfileName = profileName;

		List<String> profileNames = new ArrayList<>(profiles.keySet());

		if (profileNames.size() <= 0) {
			Profile empty = new Profile.Builder().build();

			this.previous = empty;
			this.next = empty;
		} else {
			if (profileName.isPresent()) {
				int currentIndex = profileNames.indexOf(profileName.get());

				int prevIndex = Math.floorMod(currentIndex - 1, profileNames.size());
				int nextIndex = Math.floorMod(currentIndex + 1, profileNames.size());

				this.previous = profiles.get(profileNames.get(prevIndex));
				this.next = profiles.get(profileNames.get(nextIndex));
			} else {
				this.previous = profiles.get(profileNames.get(profileNames.size() - 1));
				this.next = profiles.get(profileNames.get(0));
			}
		}
	}

	@Override
	public AbstractElementWidget createWidget(ShaderPackScreen screen, NavigationController navigation) {
		return new ProfileElementWidget(screen, this.currentProfileName, this.next, this.previous);
	}
}
