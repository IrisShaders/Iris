package net.irisshaders.iris.helpers;

import com.mojang.blaze3d.platform.InputConstants;
import net.irisshaders.iris.Iris;
import net.minecraft.client.KeyMapping;

public class CooldownKeyMapping extends KeyMapping {

	private int currentCooldown = 0;

	public CooldownKeyMapping(String string, int i, String string2) {
		super(string, i, string2);
	}

	public CooldownKeyMapping(String string, InputConstants.Type type, int i, String string2) {
		super(string, type, i, string2);
	}

	public boolean consumeClick() {
		// If the config value is 0 or lower, we don't need to worry about cooldowns
		if (Iris.getIrisConfig().getKeyPressCooldown() <= 0)
			return super.consumeClick();

		// Decrement the cooldown if it's active
		currentCooldown--;

		// Return if the key was not pressed
		if (!super.consumeClick()) return false;

		// Check if the key is on cooldown
		if (currentCooldown > 0)
			return false;

		// Cooldown is not active, so we process the click and add a cooldown
		currentCooldown = Iris.getIrisConfig().getKeyPressCooldown();
		return true;
	}
}
