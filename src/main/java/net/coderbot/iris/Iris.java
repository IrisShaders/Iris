package net.coderbot.iris;

import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.pipeline.ShaderPipeline;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	public static final String MODID = "iris";
	public static final Logger logger = LogManager.getLogger();//so we get "Iris" instead of "iris"

	private static final Path shaderpacksDirectory = FabricLoader.getInstance().getGameDir().resolve("shaderpacks");

	private static ShaderPack currentPack;
	private static ShaderPipeline pipeline;
	private static IrisConfig irisConfig;
	public static KeyBinding reloadKeybind;

	@Override
	public void onInitializeClient() {
		try {
			Files.createDirectories(shaderpacksDirectory);
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

		// Attempt to load an external shaderpack if it is available
		if (!irisConfig.isInternal()) {
			loadExternalShaderpack(irisConfig.getShaderPackName());
		}
		// If there is no external shaderpack or it failed to load for some reason, load the internal shaders
		if (currentPack == null) {
			loadInternalShaderpack();
		}
		reloadKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("iris.keybind.reload", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "iris.keybinds"));
	}

	private void loadExternalShaderpack(String name) {
		Path shaderPackRoot = shaderpacksDirectory.resolve(name);
		Path shaderPackPath = shaderPackRoot.resolve("shaders");
		if (shaderPackRoot.toString().endsWith(".zip")) {
			Optional<Path> shaderDir = loadExternalZipShaderpack(name);
			if (shaderDir.isPresent()) {
				shaderPackPath = shaderDir.get();
			}
		} if (!Files.exists(shaderPackPath)) {
			logger.warn("The shaderpack " + name + " does not have a shaders directory, falling back to internal shaders");
			return;
		}

		try {
			currentPack = new ShaderPack(shaderPackPath);
		} catch (IOException e) {
			logger.error(String.format("Failed to load shaderpack \"%s\"! Falling back to internal shaders", irisConfig.getShaderPackName()));
			logger.catching(Level.ERROR, e);

			return;
		}

		logger.info("Using shaderpack: " + name);
	}

	private Optional<Path> loadExternalZipShaderpack(String fileName) {
		Path path = shaderpacksDirectory.resolve(fileName);
		try {
			FileSystem fileSystem = FileSystems.newFileSystem(path, this.getClass().getClassLoader());
			return Files.walk(fileSystem.getRootDirectories().iterator().next()).filter(path1 -> path1.endsWith("shaders")).findFirst();
		} catch (IOException e) {
			logger.error("Error while creating file system for zip directory {}", path);
			logger.catching(Level.ERROR, e);
		}
		return Optional.empty();
	}

	private void loadInternalShaderpack() {
		Path root = FabricLoader.getInstance().getModContainer("iris")
				.orElseThrow(() -> new RuntimeException("Failed to get the mod container for Iris!")).getRootPath();

		try {
			currentPack = new ShaderPack(root.resolve("shaders"));
		} catch (IOException e) {
			logger.error("Failed to load internal shaderpack!");
			throw new RuntimeException("Failed to load internal shaderpack!", e);
		}

		logger.info("Using internal shaders");
	}

	public static ShaderPipeline getPipeline() {
		if (pipeline == null) {
			pipeline = new ShaderPipeline(Objects.requireNonNull(currentPack));
		}

		return pipeline;
	}

	public static ShaderPack getCurrentPack() {
		return currentPack;
	}

	public static IrisConfig getIrisConfig() {
		return irisConfig;
	}
}
