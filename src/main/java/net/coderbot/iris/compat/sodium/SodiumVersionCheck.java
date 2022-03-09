package net.coderbot.iris.compat.sodium;

import com.google.common.collect.ImmutableList;

import java.util.Optional;

public class SodiumVersionCheck {
	// The allowed versions of Sodium for use with Iris
	//
	// Make sure to update the comments / download links when editing this!!!
	// If you forget to edit the download links you'll cause the support team a bunch
	// of pain. So don't forget!
	private static final ImmutableList<AllowedSodiumVersion> ALLOWED_SODIUM_VERSIONS = ImmutableList.of(
			// Development build for 22w03a
			AllowedSodiumVersion.exact("0.4.0-alpha6+rev.7bd2b7d",
					"https://www.curseforge.com/minecraft/mc-mods/sodium/files/3605309"),

			// For use by ReplayMod
			AllowedSodiumVersion.prefix("0.4.0-alpha6+replaymod")
	);

	public static String getDownloadLink() {
		return ALLOWED_SODIUM_VERSIONS.stream().map(AllowedSodiumVersion::getDownloadLink)
				.filter(Optional::isPresent).findFirst().get().get();
	}

	public static boolean isAllowedVersion(String sodiumVersion) {
		for (AllowedSodiumVersion allowed : ALLOWED_SODIUM_VERSIONS) {
			if (allowed.matches(sodiumVersion)) {
				return true;
			}
		}

		return false;
	}
}
