package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.option.OptionSet;
import net.coderbot.iris.shaderpack.option.ProfileSet;
import net.coderbot.iris.shaderpack.option.values.MutableOptionValues;
import net.coderbot.iris.shaderpack.option.values.OptionValues;

public class OptionMenuProfileElement extends OptionMenuElement {
	public final ProfileSet profiles;
	public final OptionSet options;

	private final OptionValues packAppliedValues;

	public OptionMenuProfileElement(ProfileSet profiles, OptionSet options, OptionValues packAppliedValues) {
		this.profiles = profiles;
		this.options = options;
		this.packAppliedValues = packAppliedValues;
	}

	/**
	 * @return an {@link OptionValues} that also contains values currently
	 * pending application.
	 */
	public OptionValues getPendingOptionValues() {
		MutableOptionValues values = packAppliedValues.mutableCopy();
		values.addAll(Iris.getShaderPackOptionQueue());

		return values;
	}
}
