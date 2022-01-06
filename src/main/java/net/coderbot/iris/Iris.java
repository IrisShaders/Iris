package net.coderbot.iris;

import java.io.IOException;
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
import java.util.zip.ZipException;

import com.google.common.base.Throwables;
import com.mojang.blaze3d.platform.InputConstants;
import net.coderbot.iris.compat.sodium.SodiumVersionCheck;
import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.pipeline.*;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.option.Profile;
import net.coderbot.iris.shaderpack.discovery.ShaderpackDirectoryManager;
import net.fabricmc.loader.api.ModContainer;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	public static final String MODID = "iris";
	public static final Logger logger = LogManager.getLogger(MODID);
	public static final String SODIUM_DOWNLOAD_LINK = "https://www.curseforge.com/minecraft/mc-mods/sodium/files/3535040";

	private static Path shaderpacksDirectory;
	private static ShaderpackDirectoryManager shaderpacksDirectoryManager;

	private static ShaderPack currentPack;
	private static String currentPackName;
	private static boolean internal;
	private static boolean sodiumInvalid;
	private static boolean sodiumInstalled;
	private static boolean physicsModInstalled;
	private static boolean initialized;

	private static PipelineManager pipelineManager;
	private static IrisConfig irisConfig;
	private static FileSystem zipFileSystem;
	private static KeyMapping reloadKeybind;
	private static KeyMapping toggleShadersKeybind;
	private static KeyMapping shaderpackScreenKeybind;

	private static final Map<String, String> shaderPackOptionQueue = new HashMap<>();
	private static boolean resetShaderPackOptions = false;

	private static String IRIS_VERSION;

	@Override
	public void onInitializeClient() {
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

		physicsModInstalled = FabricLoader.getInstance().isModLoaded("physicsmod");

		try {
			if (!Files.exists(getShaderpacksDirectory())) {
				Files.createDirectories(getShaderpacksDirectory());
			}
		} catch (IOException e) {
			logger.warn("Failed to create the shaderpacks directory!");
			logger.catching(Level.WARN, e);
		}

		irisConfig = new IrisConfig(FabricLoader.getInstance().getConfigDir().resolve("iris.properties"));

		try {
			irisConfig.initialize();
		} catch (IOException e) {
			logger.error("Failed to initialize Iris configuration, default values will be used instead");
			logger.catching(Level.ERROR, e);
		}

		reloadKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping("iris.keybind.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "iris.keybinds"));
		toggleShadersKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping("iris.keybind.toggleShaders", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "iris.keybinds"));
		shaderpackScreenKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping("iris.keybind.shaderPackSelection", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "iris.keybinds"));

		initialized = true;
	}

	public static void onRenderSystemInit() {
		if (!initialized) {
			Iris.logger.warn("Iris::onRenderSystemInit was called, but Iris::onInitializeClient was not called." +
					" Is Not Enough Crashes doing something weird? Trying to avoid a crash but this is an odd state.");
			return;
		}

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
			IrisConfig config = getIrisConfig();
			try {
				config.setShadersEnabled(!config.areShadersEnabled());
				config.save();

				reload();
				if (minecraft.player != null) {
					minecraft.player.displayClientMessage(new TranslatableComponent("iris.shaders.toggled", config.areShadersEnabled() ? currentPackName : "off"), false);
				}
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
		if (!irisConfig.isInternal()) {
			Optional<String> externalName = irisConfig.getShaderPackName();

			if (!externalName.isPresent()) {
				logger.info("Shaders are disabled because no valid shaderpack is selected");

				setShadersDisabled();

				return;
			}

			if (!loadExternalShaderpack(externalName.get())) {
				logger.warn("Falling back to normal rendering without shaders because the external shaderpack could not be loaded");
				setShadersDisabled();
				currentPackName = "(off) [fallback, check your logs for errors]";
			}
		} else {
			try {
				loadInternalShaderpack();
			} catch (Exception e) {
				logger.error("Something went terribly wrong, Iris was unable to load the internal shaderpack!");
				logger.catching(Level.ERROR, e);

				logger.warn("Falling back to normal rendering without shaders because the internal shaderpack could not be loaded");
				setShadersDisabled();
				currentPackName = "(off) [fallback, check your logs for errors]";
			}
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
				logger.catching(Level.ERROR, e);

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

		Map<String, String> changedConfigs = loadConfigProperties(shaderPackConfigTxt)
				.map(properties -> (Map<String, String>) (Map) properties)
				.orElse(new HashMap<>());

		changedConfigs.putAll(shaderPackOptionQueue);
		clearShaderPackOptionQueue();

		if (resetShaderPackOptions) {
			changedConfigs.clear();
		}
		resetShaderPackOptions = false;

		Properties configsToSave = new Properties();
		configsToSave.putAll(changedConfigs);
		saveConfigProperties(shaderPackConfigTxt, configsToSave);

		try {
			currentPack = new ShaderPack(shaderPackPath, changedConfigs);
		} catch (Exception e) {
			logger.error("Failed to load the shaderpack \"{}\"!", name);
			logger.catching(e);

			return false;
		}

		currentPackName = name;
		internal = false;

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

	private static void loadInternalShaderpack() {
		Path root = FabricLoader.getInstance().getModContainer("iris")
				.orElseThrow(() -> new RuntimeException("Failed to get the mod container for Iris!")).getRootPath();

		try {
			currentPack = new ShaderPack(root.resolve("shaders"));
		} catch (IOException e) {
			logger.error("Failed to load internal shaderpack!");
			throw new RuntimeException("Failed to load internal shaderpack!", e);
		}

		logger.info("Using internal shaders");
		currentPackName = "(internal)";
		internal = true;
	}

	private static void setShadersDisabled() {
		currentPack = null;
		currentPackName = "(off)";
		internal = false;

		logger.info("Shaders are disabled");
	}

	private static Optional<Properties> loadConfigProperties(Path path) {
		Properties properties = new Properties();

		try {
			// NB: config properties are specified to be encoded with ISO-8859-1 by OptiFine,
			//     so we don't need to do the UTF-8 workaround here.
			properties.load(Files.newInputStream(path));
		} catch (IOException e) {
			// TODO: Better error handling
			return Optional.empty();
		}

		return Optional.of(properties);
	}

	private static void saveConfigProperties(Path path, Properties properties) {
		try {
			properties.store(Files.newOutputStream(path), null);
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
			try {
				FileSystem zipSystem = FileSystems.newFileSystem(pack, Iris.class.getClassLoader());
				Path root = zipSystem.getRootDirectories().iterator().next();
				return Files.walk(root)
						.filter(Files::isDirectory)
						.anyMatch(path -> path.endsWith("shaders"));
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

	public static DimensionId lastDimension = DimensionId.OVERWORLD;

	public static DimensionId getCurrentDimension() {
		ClientLevel level = Minecraft.getInstance().level;

		if (level != null) {
			ResourceKey<net.minecraft.world.level.Level> worldRegistryKey = level.dimension();

			if (worldRegistryKey.equals(net.minecraft.world.level.Level.END)) {
				return DimensionId.END;
			} else if (worldRegistryKey.equals(net.minecraft.world.level.Level.NETHER)) {
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

		// We use DeferredWorldRenderingPipeline on 1.16, and NewWorldRendering pipeline on 1.17 when rendering shaders.
		try {
			return new NewWorldRenderingPipeline(programs);
		} catch (Throwable e) {
			Iris.logger.error("Couldn't load NewWorldRenderingPipeline, falling back to vanilla shaders.", e);
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
		if (IRIS_VERSION == null || IRIS_VERSION.contains("${version}")) {
			return "Version info unknown!";
		}

		return IRIS_VERSION;
	}

	public static String getFormattedVersion() {
		ChatFormatting color;
		String version = getVersion();

		if (version.endsWith("-dirty") || version.contains("unknown")) {
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

	public static boolean isPhysicsModInstalled() {
		return physicsModInstalled;
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
			shaderpacksDirectoryManager = new ShaderpackDirectoryManager(shaderpacksDirectory);
		}

		return shaderpacksDirectoryManager;
	}
}
