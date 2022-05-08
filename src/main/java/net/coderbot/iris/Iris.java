package net.coderbot.iris;

import com.google.common.base.Throwables;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.coderbot.iris.compat.sodium.SodiumVersionCheck;
import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.gl.GLDebug;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.pipeline.DeferredWorldRenderingPipeline;
import net.coderbot.iris.pipeline.FixedFunctionWorldRenderingPipeline;
import net.coderbot.iris.pipeline.PipelineManager;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.shaderpack.OptionalBoolean;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.discovery.ShaderpackDirectoryManager;
import net.coderbot.iris.shaderpack.option.OptionSet;
import net.coderbot.iris.shaderpack.option.Profile;
import net.coderbot.iris.shaderpack.option.values.MutableOptionValues;
import net.coderbot.iris.shaderpack.option.values.OptionValues;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.zip.ZipError;
import java.util.zip.ZipException;

public class Iris {
	public static final String MODID = "iris";

	/**
	 * The user-facing name of the mod. Moved into a constant to facilitate
	 * easy branding changes (for forks). You'll still need to change this
	 * separately in mixin plugin classes & the language files.
	 */
	public static final String MODNAME = "Iris";

	public static final IrisLogging logger = new IrisLogging(MODNAME);

	private static Path shaderpacksDirectory;
	private static ShaderpackDirectoryManager shaderpacksDirectoryManager;

	private static ShaderPack currentPack;
	private static String currentPackName;
	private static boolean sodiumInvalid;
	private static boolean sodiumInstalled;
	private static boolean initialized;

	private static PipelineManager pipelineManager;
	private static IrisConfig irisConfig;
	private static FileSystem zipFileSystem;
	private static KeyMapping reloadKeybind;
	private static KeyMapping toggleShadersKeybind;
	private static KeyMapping shaderpackScreenKeybind;

	private static final Map<String, String> shaderPackOptionQueue = new HashMap<>();
	// Flag variable used when reloading
	// Used in favor of queueDefaultShaderPackOptionValues() for resetting as the
	// behavior is more concrete and therefore is more likely to repair a user's issues
	private static boolean resetShaderPackOptions = false;

	private static String IRIS_VERSION;

	/**
	 * Called very early on in Minecraft initialization. At this point we *cannot* safely access OpenGL, but we can do
	 * some very basic setup, config loading, and environment checks.
	 *
	 * <p>This is roughly equivalent to Fabric Loader's ClientModInitializer#onInitializeClient entrypoint, except
	 * it's entirely cross platform & we get to decide its exact semantics.</p>
	 *
	 * <p>This is called right before options are loaded, so we can add key bindings here.</p>
	 */
	public void onEarlyInitialize() {
		FabricLoader.getInstance().getModContainer("sodium").ifPresent(
				modContainer -> {
					sodiumInstalled = true;
					String versionString = modContainer.getMetadata().getVersion().getFriendlyString();

					// This makes it so that if we don't have the right version of Sodium, it will show the user a
					// nice warning, and prevent them from playing the game with a wrong version of Sodium.
					if (!SodiumVersionCheck.isAllowedVersion(versionString)) {
						sodiumInvalid = true;
					}
				}
		);

		ModContainer iris = FabricLoader.getInstance().getModContainer(MODID)
				.orElseThrow(() -> new IllegalStateException("Couldn't find the mod container for Iris"));

		IRIS_VERSION = iris.getMetadata().getVersion().getFriendlyString();

		try {
			if (!Files.exists(getShaderpacksDirectory())) {
				Files.createDirectories(getShaderpacksDirectory());
			}
		} catch (IOException e) {
			logger.warn("Failed to create the shaderpacks directory!");
			logger.warn("", e);
		}

		irisConfig = new IrisConfig(FabricLoader.getInstance().getConfigDir().resolve("iris.properties"));

		try {
			irisConfig.initialize();
		} catch (IOException e) {
			logger.error("Failed to initialize Iris configuration, default values will be used instead");
			logger.error("", e);
		}

		reloadKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping("iris.keybind.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "iris.keybinds"));
		toggleShadersKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping("iris.keybind.toggleShaders", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "iris.keybinds"));
		shaderpackScreenKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping("iris.keybind.shaderPackSelection", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "iris.keybinds"));

		setupCommands(Minecraft.getInstance());

		initialized = true;
	}

	private void setupCommands(Minecraft instance) {
		ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("iris").then(ClientCommandManager.literal("debug").then(
			ClientCommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
				boolean enable = BoolArgumentType.getBool(context, "enabled");

				Iris.setDebug(enable);

				return 0;
			})
		)).then(ClientCommandManager.literal("enabled").then(ClientCommandManager.argument("option", BoolArgumentType.bool()).executes(context -> {
			try {
				toggleShaders(instance, BoolArgumentType.getBool(context, "option"));
			} catch (IOException e) {
				e.printStackTrace();
			}

			return 0;
		}))).then(ClientCommandManager.literal("reload").executes(context -> {
			try {
				reload();

				if (instance.player != null) {
					instance.player.displayClientMessage(new TranslatableComponent("iris.shaders.reloaded"), false);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}

			return 0;
		})));
	}

	/**
	 * Called once RenderSystem#initRenderer has completed. This means that we can safely access OpenGL.
	 */
	public static void onRenderSystemInit() {
		if (!initialized) {
			Iris.logger.warn("Iris::onRenderSystemInit was called, but Iris::onEarlyInitialize was not called." +
					" Trying to avoid a crash but this is an odd state.");
			return;
		}

		setDebug(irisConfig.isDebugEnabled());

		// Only load the shader pack when we can access OpenGL
		loadShaderpack();
	}

	public static void handleKeybinds(Minecraft minecraft) {
		if (reloadKeybind.consumeClick()) {
			try {
				reload();

				if (minecraft.player != null) {
					minecraft.player.displayClientMessage(new TranslatableComponent("iris.shaders.reloaded"), false);
				}

			} catch (Exception e) {
				logger.error("Error while reloading Shaders for Iris!", e);

				if (minecraft.player != null) {
					minecraft.player.displayClientMessage(new TranslatableComponent("iris.shaders.reloaded.failure", Throwables.getRootCause(e).getMessage()).withStyle(ChatFormatting.RED), false);
				}
			}
		} else if (toggleShadersKeybind.consumeClick()) {
			try {
				toggleShaders(minecraft, !irisConfig.areShadersEnabled());
			} catch (Exception e) {
				logger.error("Error while toggling shaders!", e);

				if (minecraft.player != null) {
					minecraft.player.displayClientMessage(new TranslatableComponent("iris.shaders.toggled.failure", Throwables.getRootCause(e).getMessage()).withStyle(ChatFormatting.RED), false);
				}

				setShadersDisabled();
				currentPackName = "(off) [fallback, check your logs for errors]";
			}
		} else if (shaderpackScreenKeybind.consumeClick()) {
			minecraft.setScreen(new ShaderPackScreen(null));
		}
	}

	public static void toggleShaders(Minecraft minecraft, boolean enabled) throws IOException {
		irisConfig.setShadersEnabled(enabled);
		irisConfig.save();

		reload();
		if (minecraft.player != null) {
			minecraft.player.displayClientMessage(enabled ? new TranslatableComponent("iris.shaders.toggled", currentPackName) : new TranslatableComponent("iris.shaders.disabled"), false);
		}
	}

	public static void loadShaderpack() {
		if (irisConfig == null) {
			if (!initialized) {
				throw new IllegalStateException("Iris::loadShaderpack was called, but Iris::onInitializeClient wasn't" +
						" called yet. How did this happen?");
			} else {
				throw new NullPointerException("Iris.irisConfig was null unexpectedly");
			}
		}

		if (!irisConfig.areShadersEnabled()) {
			logger.info("Shaders are disabled because enableShaders is set to false in iris.properties");

			setShadersDisabled();

			return;
		}

		// Attempt to load an external shaderpack if it is available
		Optional<String> externalName = irisConfig.getShaderPackName();

		if (!externalName.isPresent()) {
			logger.info("Shaders are disabled because no valid shaderpack is selected");

			setShadersDisabled();

			return;
		}

		if (!loadExternalShaderpack(externalName.get())) {
			logger.warn("Falling back to normal rendering without shaders because the shaderpack could not be loaded");
			setShadersDisabled();
			currentPackName = "(off) [fallback, check your logs for errors]";
		}
	}

	private static boolean loadExternalShaderpack(String name) {
		Path shaderPackRoot;
		Path shaderPackConfigTxt;

		try {
			shaderPackRoot = getShaderpacksDirectory().resolve(name);
			shaderPackConfigTxt = getShaderpacksDirectory().resolve(name + ".txt");
		} catch (InvalidPathException e) {
			logger.error("Failed to load the shaderpack \"{}\" because it contains invalid characters in its path", name);

			return false;
		}

		Path shaderPackPath;

		if (shaderPackRoot.toString().endsWith(".zip")) {
			Optional<Path> optionalPath;

			try {
				optionalPath = loadExternalZipShaderpack(shaderPackRoot);
			} catch (FileSystemNotFoundException | NoSuchFileException e) {
				logger.error("Failed to load the shaderpack \"{}\" because it does not exist in your shaderpacks folder!", name);

				return false;
			} catch (ZipException e) {
				logger.error("The shaderpack \"{}\" appears to be corrupted, please try downloading it again!", name);

				return false;
			} catch (IOException e) {
				logger.error("Failed to load the shaderpack \"{}\"!", name);
				logger.error("", e);

				return false;
			}

			if (optionalPath.isPresent()) {
				shaderPackPath = optionalPath.get();
			} else {
				logger.error("Could not load the shaderpack \"{}\" because it appears to lack a \"shaders\" directory", name);
				return false;
			}
		} else {
			if (!Files.exists(shaderPackRoot)) {
				logger.error("Failed to load the shaderpack \"{}\" because it does not exist!", name);
				return false;
			}

			// If it's a folder-based shaderpack, just use the shaders subdirectory
			shaderPackPath = shaderPackRoot.resolve("shaders");
		}

		if (!Files.exists(shaderPackPath)) {
			logger.error("Could not load the shaderpack \"{}\" because it appears to lack a \"shaders\" directory", name);
			return false;
		}

		Map<String, String> changedConfigs = tryReadConfigProperties(shaderPackConfigTxt)
				.map(properties -> (Map<String, String>) (Map) properties)
				.orElse(new HashMap<>());

		changedConfigs.putAll(shaderPackOptionQueue);
		clearShaderPackOptionQueue();

		if (resetShaderPackOptions) {
			changedConfigs.clear();
		}
		resetShaderPackOptions = false;

		try {
			currentPack = new ShaderPack(shaderPackPath, changedConfigs);

			MutableOptionValues changedConfigsValues = currentPack.getShaderPackOptions().getOptionValues().mutableCopy();

			// Store changed values from those currently in use by the shader pack
			Properties configsToSave = new Properties();
			changedConfigsValues.getBooleanValues().forEach((k, v) -> configsToSave.setProperty(k, Boolean.toString(v)));
			changedConfigsValues.getStringValues().forEach(configsToSave::setProperty);

			tryUpdateConfigPropertiesFile(shaderPackConfigTxt, configsToSave);
		} catch (Exception e) {
			logger.error("Failed to load the shaderpack \"{}\"!", name);
			logger.error("", e);

			return false;
		}

		currentPackName = name;

		logger.info("Using shaderpack: " + name);

		return true;
	}

	private static Optional<Path> loadExternalZipShaderpack(Path shaderpackPath) throws IOException {
		FileSystem zipSystem = FileSystems.newFileSystem(shaderpackPath, Iris.class.getClassLoader());
		zipFileSystem = zipSystem;

		// Should only be one root directory for a zip shaderpack
		Path root = zipSystem.getRootDirectories().iterator().next();

		Path potentialShaderDir = zipSystem.getPath("shaders");

		// If the shaders dir was immediately found return it
		// Otherwise, manually search through each directory path until it ends with "shaders"
		if (Files.exists(potentialShaderDir)) {
			return Optional.of(potentialShaderDir);
		}

		// Sometimes shaderpacks have their shaders directory within another folder in the shaderpack
		// For example Sildurs-Vibrant-Shaders.zip/shaders
		// While other packs have Trippy-Shaderpack-master.zip/Trippy-Shaderpack-master/shaders
		// This makes it hard to determine what is the actual shaders dir
		return Files.walk(root)
				.filter(Files::isDirectory)
				.filter(path -> path.endsWith("shaders"))
				.findFirst();
	}

	private static void setShadersDisabled() {
		currentPack = null;
		currentPackName = "(off)";

		logger.info("Shaders are disabled");
	}

	private static void setDebug(boolean enable) {
		int success;
		if (enable) {
			success = GLDebug.setupDebugMessageCallback();
		} else {
			GlDebug.enableDebugCallback(Minecraft.getInstance().options.glDebugVerbosity, false);
			success = 1;
		}

		logger.info("Debug functionality is " + (enable ? "enabled, logging will be more verbose!" : "disabled."));
		if (Minecraft.getInstance().player != null) {
			Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent(success != 0 ? (enable ? "iris.shaders.debug.enabled" : "iris.shaders.debug.disabled") : "iris.shaders.debug.failure"), false);
			if (success == 2) {
				Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("iris.shaders.debug.restart"), false);
			}
		}

		try {
			irisConfig.setDebugEnabled(enable);
			irisConfig.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Optional<Properties> tryReadConfigProperties(Path path) {
		Properties properties = new Properties();

		if (Files.exists(path)) {
			try {
				// NB: config properties are specified to be encoded with ISO-8859-1 by OptiFine,
				//     so we don't need to do the UTF-8 workaround here.
				properties.load(Files.newInputStream(path));
			} catch (IOException e) {
				// TODO: Better error handling
				return Optional.empty();
			}
		}

		return Optional.of(properties);
	}

	private static void tryUpdateConfigPropertiesFile(Path path, Properties properties) {
		try {
			if (properties.isEmpty()) {
				// Delete the file or don't create it if there are no changed configs
				if (Files.exists(path)) {
					Files.delete(path);
				}

				return;
			}

			try (OutputStream out = Files.newOutputStream(path)) {
				properties.store(out, null);
			}
		} catch (IOException e) {
			// TODO: Better error handling
		}
	}

	public static boolean isValidShaderpack(Path pack) {
		if (Files.isDirectory(pack)) {
			// Sometimes the shaderpack directory itself can be
			// identified as a shader pack due to it containing
			// folders which contain "shaders" folders, this is
			// necessary to check against that
			if (pack.equals(getShaderpacksDirectory())) {
				return false;
			}
			try {
				return Files.walk(pack)
						.filter(Files::isDirectory)
						// Prevent a pack simply named "shaders" from being
						// identified as a valid pack
						.filter(path -> !path.equals(pack))
						.anyMatch(path -> path.endsWith("shaders"));
			} catch (IOException ignored) {
				// ignored, not a valid shader pack.
			}
		}

		if (pack.toString().endsWith(".zip")) {
			try (FileSystem zipSystem = FileSystems.newFileSystem(pack, Iris.class.getClassLoader())) {
				Path root = zipSystem.getRootDirectories().iterator().next();
				return Files.walk(root)
						.filter(Files::isDirectory)
						.anyMatch(path -> path.endsWith("shaders"));
			} catch (ZipError zipError) {
				// Java 8 seems to throw a ZipError instead of a subclass of IOException
				Iris.logger.warn("The ZIP at " + pack + " is corrupt");
			} catch (IOException ignored) {
				// ignored, not a valid shader pack.
			}
		}

		return false;
	}

	public static Map<String, String> getShaderPackOptionQueue() {
		return shaderPackOptionQueue;
	}

	public static void queueShaderPackOptionsFromProfile(Profile profile) {
		getShaderPackOptionQueue().putAll(profile.optionValues);
	}

	public static void queueShaderPackOptionsFromProperties(Properties properties) {
		queueDefaultShaderPackOptionValues();

		properties.stringPropertyNames().forEach(key ->
				getShaderPackOptionQueue().put(key, properties.getProperty(key)));
	}

	// Used in favor of resetShaderPackOptions as the aforementioned requires the pack to be reloaded
	public static void queueDefaultShaderPackOptionValues() {
		clearShaderPackOptionQueue();

		getCurrentPack().ifPresent(pack -> {
			OptionSet options = pack.getShaderPackOptions().getOptionSet();
			OptionValues values = pack.getShaderPackOptions().getOptionValues();

			options.getStringOptions().forEach((key, mOpt) -> {
				if (values.getStringValue(key).isPresent()) {
					getShaderPackOptionQueue().put(key, mOpt.getOption().getDefaultValue());
				}
			});
			options.getBooleanOptions().forEach((key, mOpt) -> {
				if (values.getBooleanValue(key) != OptionalBoolean.DEFAULT) {
					getShaderPackOptionQueue().put(key, Boolean.toString(mOpt.getOption().getDefaultValue()));
				}
			});
		});
	}

	public static void clearShaderPackOptionQueue() {
		getShaderPackOptionQueue().clear();
	}

	public static void resetShaderPackOptionsOnNextReload() {
		resetShaderPackOptions = true;
	}

	public static void reload() throws IOException {
		// allows shaderpacks to be changed at runtime
		irisConfig.initialize();

		// Destroy all allocated resources
		destroyEverything();

		// Load the new shaderpack
		loadShaderpack();
	}

	/**
	 * Destroys and deallocates all created OpenGL resources. Useful as part of a reload.
	 */
	private static void destroyEverything() {
		currentPack = null;

		getPipelineManager().destroyPipeline();

		// Close the zip filesystem that the shaderpack was loaded from
		//
		// This prevents a FileSystemAlreadyExistsException when reloading shaderpacks.
		if (zipFileSystem != null) {
			try {
				zipFileSystem.close();
			} catch (NoSuchFileException e) {
				logger.warn("Failed to close the shaderpack zip when reloading because it was deleted, proceeding anyways.");
			} catch (IOException e) {
				logger.error("Failed to close zip file system?", e);
			}
		}
	}

	public static DimensionId lastDimension = null;

	public static DimensionId getCurrentDimension() {
		ClientLevel level = Minecraft.getInstance().level;

		if (level != null) {
			if (level.dimensionType().effectsLocation().equals(DimensionType.END_EFFECTS) || level.dimension().equals(net.minecraft.world.level.Level.END)) {
				return DimensionId.END;
			} else if (level.dimensionType().effectsLocation().equals(DimensionType.NETHER_EFFECTS) || level.dimension().equals(net.minecraft.world.level.Level.NETHER)) {
				return DimensionId.NETHER;
			} else {
				return DimensionId.OVERWORLD;
			}
		} else {
			// This prevents us from reloading the shaderpack unless we need to. Otherwise, if the player is in the
			// nether and quits the game, we might end up reloading the shaders on exit and on entry to the level
			// because the code thinks that the dimension changed.
			return lastDimension;
		}
	}

	private static WorldRenderingPipeline createPipeline(DimensionId dimensionId) {
		if (currentPack == null) {
			// Completely disables shader-based rendering
			return new FixedFunctionWorldRenderingPipeline();
		}

		ProgramSet programs = currentPack.getProgramSet(dimensionId);

		try {
			return new DeferredWorldRenderingPipeline(programs);
		} catch (Exception e) {
			logger.error("Failed to create shader rendering pipeline, disabling shaders!", e);
			// TODO: This should be reverted if a dimension change causes shaders to compile again
			currentPackName = "(off) [fallback, check your logs for details]";

			return new FixedFunctionWorldRenderingPipeline();
		}
	}

	@NotNull
	public static PipelineManager getPipelineManager() {
		if (pipelineManager == null) {
			pipelineManager = new PipelineManager(Iris::createPipeline);
		}

		return pipelineManager;
	}

	@NotNull
	public static Optional<ShaderPack> getCurrentPack() {
		return Optional.ofNullable(currentPack);
	}

	public static String getCurrentPackName() {
		return currentPackName;
	}

	public static IrisConfig getIrisConfig() {
		return irisConfig;
	}

	public static String getVersion() {
		if (IRIS_VERSION == null) {
			return "Version info unknown!";
		}

		return IRIS_VERSION;
	}

	public static String getFormattedVersion() {
		ChatFormatting color;
		String version = getVersion();

		if (version.endsWith("-development-environment")) {
			color = ChatFormatting.GOLD;
			version = version.replace("-development-environment", " (Development Environment)");
		} else if (version.endsWith("-dirty") || version.contains("unknown") || version.endsWith("-nogit")) {
			color = ChatFormatting.RED;
		} else if (version.contains("+rev.")) {
			color = ChatFormatting.LIGHT_PURPLE;
		} else {
			color = ChatFormatting.GREEN;
		}

		return color + version;
	}

	public static boolean isSodiumInvalid() {
		return sodiumInvalid;
  }
  
	public static boolean isSodiumInstalled() {
		return sodiumInstalled;
	}

	public static boolean isPackActive() {
		return IrisApi.getInstance().isShaderPackInUse();
	}

	public static Path getShaderpacksDirectory() {
		if (shaderpacksDirectory == null) {
			shaderpacksDirectory = FabricLoader.getInstance().getGameDir().resolve("shaderpacks");
		}

		return shaderpacksDirectory;
	}

	public static ShaderpackDirectoryManager getShaderpacksDirectoryManager() {
		if (shaderpacksDirectoryManager == null) {
			shaderpacksDirectoryManager = new ShaderpackDirectoryManager(getShaderpacksDirectory());
		}

		return shaderpacksDirectoryManager;
	}
}
