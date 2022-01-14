package net.coderbot.iris.compat.sodium;

import com.google.common.collect.ImmutableList;

public class SodiumVersionCheck {
	// The allowed versions of Sodium for use with Iris
	private static final ImmutableList<AllowedSodiumVersion> ALLOWED_SODIUM_VERSIONS = ImmutableList.of(
			// Official 0.3.4
			AllowedSodiumVersion.exact("0.3.4+build.13"),

			// For use by ReplayMod
			AllowedSodiumVersion.prefix("0.3.4+replaymod")
	);

	public static boolean isAllowedVersion(String sodiumVersion) {
		for (AllowedSodiumVersion allowed : ALLOWED_SODIUM_VERSIONS) {
			if (allowed.matches(sodiumVersion)) {
				return true;
			}
		}

		return false;
	}
}
