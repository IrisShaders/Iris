package net.coderbot.iris.compat.sodium;

import com.google.common.collect.ImmutableList;

public class SodiumVersionCheck {
	// The allowed versions of Sodium for use with Iris
	private static final ImmutableList<AllowedSodiumVersion> ALLOWED_SODIUM_VERSIONS = ImmutableList.of(
			// Official 0.3.3
			AllowedSodiumVersion.exact("0.3.3+build.8"),

			// ReplayMod's existing compatible forked 0.3.3 version
			AllowedSodiumVersion.prefix("0.3.3+rev.14a0485"),

			// For future use by ReplayMod
			AllowedSodiumVersion.prefix("0.3.3+replaymod")
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
