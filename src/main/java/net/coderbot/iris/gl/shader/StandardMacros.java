package net.coderbot.iris.gl.shader;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;

import net.minecraft.SharedConstants;
import net.minecraft.util.Util;

public class StandardMacros {

	private static final Pattern SEMVER_PATTERN = Pattern.compile("(?<major>\\d+)\\.(?<minor>\\d+)\\.*(?<bugfix>\\d*)(.*)");


	/**
	 * Gets the current mc version String in a 5 digit format
	 *
	 * @return mc version string
	 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt#L677">Optifine Doc</a>
	 */
	public static String getMcVersion() {
		String version = SharedConstants.getGameVersion().getReleaseTarget(); //release target so snapshots are set to the higher version

		Matcher matcher = SEMVER_PATTERN.matcher(version);

		if (!matcher.matches()) {
			return null;
		}

		String major = group(matcher, "major");
		String minor = group(matcher, "minor");
		String bugfix = group(matcher, "bugfix");

		if (bugfix == null) {
			bugfix = "00";
		}

		if (minor == null || major == null) return null;

		if (minor.length() == 1) {
			minor = 0 + minor;
		}
		if (bugfix.length() == 1) {
			bugfix = 0 + bugfix;
		}

		return major + minor + bugfix;
	}

	/**
	 * Returns the current OS String
	 *
	 * @return the string based on the current OS
	 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt#L687-L692">Optifine Doc</a>
	 */
	public static String getOsString() {
		switch (Util.getOperatingSystem()) {
			case OSX:
				return "MC_OS_MAC";
			case LINUX:
				return "MC_OS_LINUX";
			case WINDOWS:
				return "MC_OS_WINDOWS";
			case SOLARIS: //Note: Optifine doesn't have a macro for Solaris. https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt#L689-L692
			case UNKNOWN:
			default:
				return "MC_OS_UNKNOWN";
		}
	}

	/**
	 * Returns the current GL Version using regex
	 *
	 * @param name the name of the gl attribute to parse
	 * @return current gl version stripped of semantic versioning
	 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt#L679-L681">Optifine Doc for GL Version</a>
	 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt#L683-L685">Optifine Doc for GLSL Version</a>
	 */
	public static String getGlVersion(int name) {
		String info = GL20.glGetString(name);

		Matcher matcher = SEMVER_PATTERN.matcher(Objects.requireNonNull(info));

		if (!matcher.matches()) {
			return null;
		}

		String major = group(matcher, "major");
		String minor = group(matcher, "minor");
		String bugfix = group(matcher, "bugfix");

		if (bugfix == null) { //if bugfix is not there, it is 0
			bugfix = "0";
		}

		return major + minor + bugfix;

	}

	/**
	 * Expanded version of {@link Matcher#group(String)} that does not throw an exception.
	 * If the argument is incorrect (normally resulting in an exception), it returns null
	 *
	 * @param matcher matcher to check the group by
	 * @param name    name of the group
	 * @return the section of the matcher that is a group, or null, if that matcher does not contain said group
	 */
	public static String group(Matcher matcher, String name) {
		try {
			return matcher.group(name);
		} catch (IllegalArgumentException | IllegalStateException exception) {
			return null;
		}
	}

	/**
	 * Returns the list of currently enabled GL extensions
	 * This is done by calling {@link GL11#glGetString} with the arg {@link GL11#GL_EXTENSIONS}
	 *
	 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt#L713-L716">Optifine Doc</a>
	 * @return list of activated extensions prefixed with "MC_"
	 */
	public static List<String> getGlExtensions() {
		String[] extensions = Objects.requireNonNull(GL11.glGetString(GL11.GL_EXTENSIONS)).split("\\s+");

		return Arrays.stream(extensions).map(s -> "MC_" + s).collect(Collectors.toList());
	}

	/**
	 * Returns the graphics driver being used
	 *
	 * @return graphics driver prefixed with "MC_GL_RENDERER_"
	 */
	public static String getRenderer() {
		String renderer = Objects.requireNonNull(GL11.glGetString(GL11C.GL_RENDERER)).toLowerCase(Locale.ROOT);
		if (renderer.startsWith("amd")) {
			return "MC_GL_RENDERER_RADEON";
		} else if (renderer.startsWith("ati")) {
			return "MC_GL_RENDERER_RADEON";
		} else if (renderer.startsWith("radeon")) {
			return "MC_GL_RENDERER_RADEON";
		} else if (renderer.startsWith("gallium")) {
			return "MC_GL_RENDERER_GALLIUM";
		} else if (renderer.startsWith("intel")) {
			return "MC_GL_RENDERER_INTEL";
		} else if (renderer.startsWith("geforce")) {
			return "MC_GL_RENDERER_GEFORCE";
		} else if (renderer.startsWith("nvidia")) {
			return "MC_GL_RENDERER_GEFORCE";
		} else if (renderer.startsWith("quadro")) {
			return "MC_GL_RENDERER_QUADRO";
		} else if (renderer.startsWith("nvs")) {
			return "MC_GL_RENDERER_QUADRO";
		} else if (renderer.startsWith("mesa")) {
			return "MC_GL_RENDERER_MESA";
		}
		return "MC_GL_RENDERER_OTHER";
	}

	public static String getVendor() {
		String vendor = Objects.requireNonNull(GL11.glGetString(GL11C.GL_VENDOR)).toLowerCase(Locale.ROOT);
		if (vendor.startsWith("ati")) {
			return "MC_GL_VENDOR_ATI";
		} else if (vendor.startsWith("intel")) {
			return "MC_GL_VENDOR_INTEL";
		} else if (vendor.startsWith("nvidia")) {
			return "MC_GL_VENDOR_NVIDIA";
		} else if (vendor.startsWith("amd")) {
			return "MC_GL_VENDOR_AMD";
		} else if (vendor.startsWith("x.org")) {
			return "MC_GL_VENDOR_XORG";
		}
		return "MC_GL_VENDOR_OTHER";
	}
}
