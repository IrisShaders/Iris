package net.coderbot.iris.compat.sodium;

import java.util.Optional;

public class AllowedSodiumVersion {
	private final String version;
	private final String downloadLink;
	private final boolean prefixMatch;

	public AllowedSodiumVersion(String version, String downloadLink, boolean prefixMatch) {
		this.version = version;
		this.downloadLink = downloadLink;
		this.prefixMatch = prefixMatch;
	}

	public static AllowedSodiumVersion prefix(String prefix) {
		return new AllowedSodiumVersion(prefix, null, true);
	}

	public static AllowedSodiumVersion exact(String version, String downloadLink) {
		return new AllowedSodiumVersion(version, downloadLink, false);
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

	public Optional<String> getDownloadLink() {
		return Optional.ofNullable(downloadLink);
	}
}
