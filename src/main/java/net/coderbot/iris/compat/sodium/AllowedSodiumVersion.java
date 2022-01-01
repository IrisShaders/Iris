package net.coderbot.iris.compat.sodium;

public class AllowedSodiumVersion {
	private final String version;
	private final boolean prefixMatch;

	public AllowedSodiumVersion(String version, boolean prefixMatch) {
		this.version = version;
		this.prefixMatch = prefixMatch;
	}

	public static AllowedSodiumVersion prefix(String prefix) {
		return new AllowedSodiumVersion(prefix, true);
	}

	public static AllowedSodiumVersion exact(String version) {
		return new AllowedSodiumVersion(version, false);
	}

	public boolean matches(String candidate) {
		if (prefixMatch) {
			return candidate.startsWith(version);
		} else {
			return candidate.equals(version);
		}
	}

	public String getVersion() {
		return version;
	}

	public boolean isPrefixMatch() {
		return prefixMatch;
	}
}
