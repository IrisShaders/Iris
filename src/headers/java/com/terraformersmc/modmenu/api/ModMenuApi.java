/*
 * MIT License
 *
 * Copyright (c) 2018-2020 Prospector
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraformersmc.modmenu.api;

import com.terraformersmc.modmenu.util.ModMenuApiMarker;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;

public interface ModMenuApi extends ModMenuApiMarker {
	/**
	 * Used for creating a {@link Screen} instance of the Mod Menu
	 * "Mods" screen
	 *
	 * @param previous The screen before opening
	 * @return A "Mods" Screen
	 */
	static Screen createModsScreen(Screen previous) {
		throw new AssertionError("Attempt to call API header stub method");
	}

	/**
	 * Used for creating a {@link Component} just like what would appear
	 * on a Mod Menu Mods button
	 *
	 * @return The text that would be displayed on a Mods button
	 */
	static Component createModsButtonText() {
		throw new AssertionError("Attempt to call API header stub method");
	}

	/**
	 * Used to construct a new config screen instance when your mod's
	 * configuration button is selected on the mod menu screen. The
	 * screen instance parameter is the active mod menu screen.
	 *
	 * @return A factory for constructing config screen instances.
	 */
	default ConfigScreenFactory<?> getModConfigScreenFactory() {
		throw new AssertionError("Attempt to call API header stub method");
	}

	/**
	 * Used to provide config screen factories for other mods. This takes second
	 * priority to a mod's own config screen factory provider. For example, if
	 * mod `xyz` supplies a config screen factory, mod `abc` providing a config
	 * screen to `xyz` will be pointless, as the one provided by `xyz` will be
	 * used.
	 * <p>
	 * This method is NOT meant to be used to add a config screen factory to
	 * your own mod.
	 *
	 * @return a map of mod ids to screen factories.
	 */
	default Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		throw new AssertionError("Attempt to call API header stub method");
	}
}
