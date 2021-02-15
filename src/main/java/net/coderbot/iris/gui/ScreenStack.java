package net.coderbot.iris.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * To prevent long loops of cycling through screens that are linked to each other
 */
public class ScreenStack {
	private static final Deque<Screen> SCREENS = new ArrayDeque<>();

	public static void push(Screen screen) {
		Optional<Screen> match = matchFor(screen.getClass());
		if(match.isPresent()) {
			SCREENS.remove(match.get());
			SCREENS.push(match.get());
		} else {
			SCREENS.push(screen);
		}
	}

	public static Screen pop() {
		return SCREENS.pop();
	}

	public static boolean pull(Class<? extends Screen> clazz) {
		Optional<Screen> match = matchFor(clazz);
		if(match.isPresent()) {
			SCREENS.remove(match.get());
			return true;
		}
		return false;
	}

	private static Optional<Screen> matchFor(Class<? extends Screen> clazz) {
		if(clazz == null) return Optional.empty();
		Screen match = null;
		for(Screen s : SCREENS) {
			if(s.getClass().equals(clazz)) {
				match = s;
			}
		}
		if(match != null) {
			return Optional.of(match);
		}
		return Optional.empty();
	}
}
