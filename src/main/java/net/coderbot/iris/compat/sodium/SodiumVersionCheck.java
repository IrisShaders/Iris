package net.coderbot.iris.compat.sodium;

import com.google.common.collect.ImmutableList;

public class SodiumVersionCheck {
	// The allowed versions of Sodium for use with Iris
	private static final ImmutableList<AllowedSodiumVersion> ALLOWED_SODIUM_VERSIONS = ImmutableList.of(
			// Official 0.4.0-alpha5
			AllowedSodiumVersion.exact("0.4.0-alpha5+build.9"),

			// ReplayMod's existing compatible forked 0.4.0-alpha5 version
			AllowedSodiumVersion.prefix("0.4.0-alpha5+rev.76d0e6e"),

			// For future use by ReplayMod
			AllowedSodiumVersion.prefix("0.4.0-alpha5+replaymod")
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
