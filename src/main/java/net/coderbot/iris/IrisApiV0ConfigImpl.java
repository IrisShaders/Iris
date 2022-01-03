package net.coderbot.iris;

import net.coderbot.iris.config.IrisConfig;
import net.irisshaders.iris.api.v0.IrisApiConfig;

import java.io.IOException;

public class IrisApiV0ConfigImpl implements IrisApiConfig {
	@Override
	public boolean areShadersEnabled() {
		return Iris.getIrisConfig().areShadersEnabled();
	}

	@Override
	public void setShadersEnabledAndApply(boolean enabled) {
		IrisConfig config = Iris.getIrisConfig();

		config.setShadersEnabled(enabled);

		try {
			config.save();
		} catch (IOException e) {
			Iris.logger.error("Error saving configuration file!");
			Iris.logger.catching(e);
		}

		try {
			Iris.reload();
		} catch (IOException e) {
			Iris.logger.error("Error reloading shader pack while applying changes!");
			Iris.logger.catching(e);
		}
	}
}
