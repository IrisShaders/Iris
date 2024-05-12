package net.irisshaders.iris.platform;

import net.minecraft.client.KeyMapping;

import java.nio.file.Path;
import java.text.ParseException;
import java.util.ServiceLoader;

public interface IrisPlatformHelpers {
	IrisPlatformHelpers INSTANCE = ServiceLoader.load(IrisPlatformHelpers.class).findFirst().get();

	static IrisPlatformHelpers getInstance() {
		return INSTANCE;
	}

	boolean isModLoaded(String modId);

	String getVersion();

	boolean isDevelopmentEnvironment();

	Path getGameDir();

	Path getConfigDir();

	int compareVersions(String currentVersion, String semanticVersion) throws Exception;

	KeyMapping registerKeyBinding(KeyMapping keyMapping);
}
