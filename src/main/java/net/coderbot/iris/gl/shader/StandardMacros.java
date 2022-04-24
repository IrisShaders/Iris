package net.coderbot.iris.gl.shader;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlUtil;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.texture.format.TextureFormat;
import net.coderbot.iris.texture.format.TextureFormatLoader;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardMacros {
	private static final Pattern SEMVER_PATTERN = Pattern.compile("(?<major>\\d+)\\.(?<minor>\\d+)\\.*(?<bugfix>\\d*)(.*)");

	public static ImmutableList<String> createDefines() {
		MacroBuilder builder = new MacroBuilder();

		builder
			.define("MC_VERSION", getMcVersion())
			.define("MC_GL_VERSION", getGlVersion(GL20C.GL_VERSION))
			.define("MC_GLSL_VERSION", getGlVersion(GL20C.GL_SHADING_LANGUAGE_VERSION))
			.define(getOsString())
			.define(getVendor())
			.define(getRenderer());

		addGlExtensions(builder);

		builder
			.define("MC_NORMAL_MAP")
			.define("MC_SPECULAR_MAP")
			.define("MC_RENDER_QUALITY", "1.0")
			.define("MC_SHADOW_QUALITY", "1.0")
			.define("MC_HAND_DEPTH", Float.toString(HandRenderer.DEPTH));

		TextureFormat textureFormat = TextureFormatLoader.getFormat();
		if (textureFormat != null) {
			textureFormat.addMacros(builder);
		}

		addRenderStages(builder);
		addIrisMacros(builder);

		return builder.build();
	}

	/**
	 * Gets the current mc version String in a 5 digit format
	 *
	 * @return mc version string
	 * @see <a href="https://github.com/sp614x/optifine/blob/9c6a5b5326558ccc57c6490b66b3be3b2dc8cbef/OptiFineDoc/doc/shaders.txt#L696-L699">Optifine Doc</a>
	 */
	public static String getMcVersion() {
		String version = SharedConstants.getCurrentVersion().getReleaseTarget();
			// release target so snapshots are set to the higher version
			//
			// For example if we were running iris on 21w07a, getReleaseTarget() would return 1.17

		if (version == null) {
			throw new IllegalStateException("Could not get the current minecraft version!");
		}

		String[] splitVersion = version.split("\\.");

		if (splitVersion.length < 2) {
			throw new IllegalStateException("Could not parse game version \"" + version +  "\"");
		}

		String major = splitVersion[0];
		String minor = splitVersion[1];
		String bugfix;

		if (splitVersion.length < 3) {
			bugfix = "00";
		} else {
			bugfix = splitVersion[2];
		}

		if (minor.length() == 1) {
			minor = 0 + minor;
		}
		if (bugfix.length() == 1) {
			bugfix = 0 + bugfix;
		}

		return major + minor + bugfix;
	}

	/**
	 * Returns the current GL Version using regex
	 *
	 * @param name the name of the gl attribute to parse
	 * @return current gl version stripped of semantic versioning
	 * @see <a href="https://github.com/sp614x/optifine/blob/9c6a5b5326558ccc57c6490b66b3be3b2dc8cbef/OptiFineDoc/doc/shaders.txt#L701-L703">Optifine Doc for GL Version</a>
	 * @see <a href="https://github.com/sp614x/optifine/blob/9c6a5b5326558ccc57c6490b66b3be3b2dc8cbef/OptiFineDoc/doc/shaders.txt#L705-L707">Optifine Doc for GLSL Version</a>
	 */
	public static String getGlVersion(int name) {
		String info = GlStateManager._getString(name);

		Matcher matcher = SEMVER_PATTERN.matcher(Objects.requireNonNull(info));

		if (!matcher.matches()) {
			throw new IllegalStateException("Could not parse GL version from \"" + info + "\"");
		}

		String major = group(matcher, "major");
		String minor = group(matcher, "minor");
		String bugfix = group(matcher, "bugfix");

		if (bugfix == null) {
			// if bugfix is not there, it is 0
			bugfix = "0";
		}

		if (major == null || minor == null) {
			throw new IllegalStateException("Could not parse GL version from \"" + info + "\"");
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
	 * Returns the current OS String
	 *
	 * @return the string based on the current OS
	 * @see <a href="https://github.com/sp614x/optifine/blob/9c6a5b5326558ccc57c6490b66b3be3b2dc8cbef/OptiFineDoc/doc/shaders.txt#L709-L714">Optifine Doc</a>
	 */
	public static String getOsString() {
		switch (Util.getPlatform()) {
			case OSX:
				return "MC_OS_MAC";
			case LINUX:
				return "MC_OS_LINUX";
			case WINDOWS:
				return "MC_OS_WINDOWS";
			case SOLARIS: // Note: Optifine doesn't have a macro for Solaris. https://github.com/sp614x/optifine/blob/9c6a5b5326558ccc57c6490b66b3be3b2dc8cbef/OptiFineDoc/doc/shaders.txt#L709-L714
			case UNKNOWN:
			default:
				return "MC_OS_UNKNOWN";
		}
	}

	/**
	 * Returns a string indicating the graphics card being used
	 *
	 * @return the graphics card prefixed with "MC_GL_VENDOR_"
	 * @see <a href="https://github.com/sp614x/optifine/blob/9c6a5b5326558ccc57c6490b66b3be3b2dc8cbef/OptiFineDoc/doc/shaders.txt#L716-L723">Optifine doc</a>
	 */
	public static String getVendor() {
		String vendor = Objects.requireNonNull(GlUtil.getVendor()).toLowerCase(Locale.ROOT);
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

	/**
	 * Returns the graphics driver being used
	 *
	 * @return graphics driver prefixed with "MC_GL_RENDERER_"
	 * @see <a href="https://github.com/sp614x/optifine/blob/9c6a5b5326558ccc57c6490b66b3be3b2dc8cbef/OptiFineDoc/doc/shaders.txt#L725-L733">Optifine Doc</a>
	 */
	public static String getRenderer() {
		String renderer = Objects.requireNonNull(GlUtil.getRenderer()).toLowerCase(Locale.ROOT);
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

	/**
	 * Adds the currently enabled GL extensions as macros to the builder
	 * This is done by calling {@link GL11#glGetString} with the arg {@link GL11#GL_EXTENSIONS}
	 *
	 * @see <a href="https://github.com/sp614x/optifine/blob/9c6a5b5326558ccc57c6490b66b3be3b2dc8cbef/OptiFineDoc/doc/shaders.txt#L735-L738">Optifine Doc</a>
	 */
	public static void addGlExtensions(MacroBuilder builder) {
		String[] extensions = Objects.requireNonNull(GlStateManager._getString(GL11.GL_EXTENSIONS)).split("\\s+");

		// TODO note that we do not add extensions based on if the shader uses them and if they are supported
		// see https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt#L738

		for (String extension : extensions) {
			builder.define("MC_" + extension);
		}
	}

	public static void addRenderStages(MacroBuilder builder) {
		for (WorldRenderingPhase phase : WorldRenderingPhase.values()) {
			builder.define("MC_RENDER_STAGE_" + phase.name(), String.valueOf(phase.ordinal()));
		}
	}

	/**
	 * Adds all Iris-exclusive uniforms supported in the current version of Iris as macros to the builder.
	 */
	public static void addIrisMacros(MacroBuilder builder) {
		// All Iris-exclusive uniforms should have a corresponding definition here. Example:
		// builder.define("MC_UNIFORM_DRAGON_DEATH_PROGRESS");
	}
}
