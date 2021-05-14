package net.coderbot.iris;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.zip.ZipException;

import com.google.common.base.Throwables;
import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.pipeline.*;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	public static final String MODID = "iris";
	public static final Logger logger = LogManager.getLogger(MODID);

	public static final Path SHADERPACKS_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("shaderpacks");

	private static ShaderPack currentPack;
	private static String currentPackName;
	private static boolean internal;

	private static PipelineManager pipelineManager;
	private static IrisConfig irisConfig;
	private static FileSystem zipFileSystem;
	private static KeyBinding reloadKeybind;

	@Override
	public void onInitializeClient() {
		FabricLoader.getInstance().getModContainer("sodium").ifPresent(
			modContainer -> {
				String versionString = modContainer.getMetadata().getVersion().getFriendlyString();

				// A lot of people are reporting visual bugs with Iris + Sodium. This makes it so that if we don't have
				// the right fork of Sodium, it will just crash.
				if (!versionString.startsWith("IRIS-SNAPSHOT")) {
					throw new IllegalStateException("You do not have a compatible version of Sodium installed! You have " + versionString + " but IRIS-SNAPSHOT is expected");
				}
			}
		);

		try {
			Files.createDirectories(SHADERPACKS_DIRECTORY);
		} catch (IOException e) {
			Iris.logger.warn("Failed to create shaderpacks directory!");
			Iris.logger.catching(Level.WARN, e);
		}

		irisConfig = new IrisConfig();

		try {
			irisConfig.initialize();
		} catch (IOException e) {
			logger.error("Failed to initialize Iris configuration, default values will be used instead");
			logger.catching(Level.ERROR, e);
		}


		loadShaderpack();

		reloadKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("iris.keybind.reload", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "iris.keybinds"));

		ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
			if (reloadKeybind.wasPressed()){

				try {
					reload();

					if (minecraftClient.player != null){
						minecraftClient.player.sendMessage(new TranslatableText("iris.shaders.reloaded"), false);
					}

				} catch (Exception e) {
					Iris.logger.error("Error while reloading Shaders for Iris!", e);

					if (minecraftClient.player != null) {
						minecraftClient.player.sendMessage(new TranslatableText("iris.shaders.reloaded.failure", Throwables.getRootCause(e).getMessage()).formatted(Formatting.RED), false);
					}
				}
			}
		});

		pipelineManager = new PipelineManager(Iris::createPipeline);
	}

	public static void loadShaderpack() {
		if (!irisConfig.areShadersEnabled()) {
			logger.info("Shaders are disabled because enableShaders is set to false in iris.properties");

			currentPack = null;
			currentPackName = "(off)";
			internal = false;

			return;
		}

		// Attempt to load an external shaderpack if it is available
		if (!irisConfig.isInternal()) {
			if (!loadExternalShaderpack(irisConfig.getShaderPackName())) {
				logger.warn("Falling back to internal shaders because the external shaderpack could not be loaded");
				loadInternalShaderpack();
				currentPackName = "(internal) [fallback, check your logs for errors]";
			}
		} else {
			loadInternalShaderpack();
		}
	}

	private static boolean loadExternalShaderpack(String name) {
		Path shaderPackRoot = SHADERPACKS_DIRECTORY.resolve(name);
		Path shaderPackPath;

		if (shaderPackRoot.toString().endsWith(".zip")) {
			Optional<Path> optionalPath;

			try {
				optionalPath = loadExternalZipShaderpack(shaderPackRoot);
			} catch (FileSystemNotFoundException e) {
				logger.error("Failed to load the shaderpack \"{}\" because it does not exist!", irisConfig.getShaderPackName());

				return false;
			} catch (ZipException e) {
				logger.error("The shaderpack \"{}\" appears to be corrupted, please try downloading it again!", irisConfig.getShaderPackName());

				return false;
			} catch (IOException e) {
				logger.error("Failed to load the shaderpack \"{}\"!", irisConfig.getShaderPackName());
				logger.catching(Level.ERROR, e);

				return false;
			}

			if (optionalPath.isPresent()) {
				shaderPackPath = optionalPath.get();
			} else {
				logger.error("Could not load the shaderpack \"{}\" because it appears to lack a \"shaders\" directory", irisConfig.getShaderPackName());
				return false;
			}
		} else {
			if (!Files.exists(shaderPackRoot)) {
				logger.error("Failed to load the shaderpack \"{}\" because it does not exist!", irisConfig.getShaderPackName());
				return false;
			}

			// If it's a folder-based shaderpack, just use the shaders subdirectory
			shaderPackPath = shaderPackRoot.resolve("shaders");
		}

		if (!Files.exists(shaderPackPath)) {
			logger.error("Could not load the shaderpack \"{}\" because it appears to lack a \"shaders\" directory", irisConfig.getShaderPackName());
			return false;
		}

		try {
			currentPack = new ShaderPack(shaderPackPath);
		} catch (IOException e) {
			logger.error("Failed to load the shaderpack \"{}\"!", irisConfig.getShaderPackName());
			logger.error(e);

			return false;
		}

		logger.info("Using shaderpack: " + name);
		currentPackName = name;
		internal = false;

		return true;
	}

	private static Optional<Path> loadExternalZipShaderpack(Path shaderpackPath) throws IOException {
		FileSystem zipSystem = FileSystems.newFileSystem(shaderpackPath, Iris.class.getClassLoader());
		zipFileSystem = zipSystem;
		Path root = zipSystem.getRootDirectories().iterator().next();//should only be one root directory for a zip shaderpack

		Path potentialShaderDir = zipSystem.getPath("shaders");
		//if the shaders dir was immediately found return it
		//otherwise, manually search through each directory path until it ends with "shaders"
		if (Files.exists(potentialShaderDir)) {
			return Optional.of(potentialShaderDir);
		}

		//sometimes shaderpacks have their shaders directory within another folder in the shaderpack
		//for example Sildurs-Vibrant-Shaders.zip/shaders
		//while other packs have Trippy-Shaderpack-master.zip/Trippy-Shaderpack-master/shaders
		//this makes it hard to determine what is the actual shaders dir
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

	public static boolean isValidShaderpack(Path pack) {
		if (Files.isDirectory(pack)) {
			try {
				return Files.walk(pack)
						.filter(Files::isDirectory)
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

		pipelineManager.destroyPipeline();

		// Close the zip filesystem that the shaderpack was loaded from
		//
		// This prevents a FileSystemAlreadyExistsException when reloading shaderpacks.
		if (zipFileSystem != null) {
			try {
				zipFileSystem.close();
			} catch (NoSuchFileException e) {
				Iris.logger.warn("Failed to close the shaderpack zip when reloading because it was deleted, proceeding anyways.");
			} catch (IOException e) {
				Iris.logger.error("Failed to close zip file system?", e);
			}
		}
	}

	public static DimensionId lastDimension = DimensionId.OVERWORLD;

	public static DimensionId getCurrentDimension() {
		ClientWorld world = MinecraftClient.getInstance().world;

		if (world != null) {
			RegistryKey<World> worldRegistryKey = world.getRegistryKey();

			if (worldRegistryKey.equals(World.END)) {
				return DimensionId.END;
			} else if (worldRegistryKey.equals(World.NETHER)) {
				return DimensionId.NETHER;
			} else {
				return DimensionId.OVERWORLD;
			}
		} else {
			// This prevents us from reloading the shaderpack unless we need to. Otherwise, if the player is in the
			// nether and quits the game, we might end up reloading the shaders on exit and on entry to the world
			// because the code thinks that the dimension changed.
			return lastDimension;
		}
	}

	private static WorldRenderingPipeline createPipeline(DimensionId dimensionId) {
		if (currentPack == null) {
			// completely disable shader-based rendering
			return new FixedFunctionWorldRenderingPipeline();
		}

		ProgramSet programs = currentPack.getProgramSet(dimensionId);

		if (internal) {
			return new InternalWorldRenderingPipeline(programs);
		} else {
			return new DeferredWorldRenderingPipeline(programs);
		}
	}

	public static PipelineManager getPipelineManager() {
		return pipelineManager;
	}

	public static Optional<ShaderPack> getCurrentPack() {
		return Optional.ofNullable(currentPack);
	}

	public static String getCurrentPackName() {
		return currentPackName;
	}

	public static IrisConfig getIrisConfig() {
		return irisConfig;
	}
}
