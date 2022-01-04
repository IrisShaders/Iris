package net.coderbot.iris.compat.sodium;

import com.google.common.collect.ImmutableList;

public class SodiumVersionCheck {
	// The allowed versions of Sodium for use with Iris
	private static final ImmutableList<AllowedSodiumVersion> ALLOWED_SODIUM_VERSIONS = ImmutableList.of(
			// Official 0.2.0
			AllowedSodiumVersion.exact("0.2.0+build.4"),

			// ReplayMod's existing compatible forked 0.2.0 version
			AllowedSodiumVersion.prefix("0.2.0+rev.f42b4ca"),

			// For future use by ReplayMod
			AllowedSodiumVersion.prefix("0.2.0+replaymod")
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
