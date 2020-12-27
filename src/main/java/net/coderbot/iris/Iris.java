
package net.coderbot.iris;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.google.common.base.Throwables;
import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.pipeline.ShaderPipeline;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.shaderpack.ShaderPack;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

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

	private static final Path shaderpacksDirectory = FabricLoader.getInstance().getGameDir().resolve("shaderpacks");

	private static ShaderPack currentPack;
	private static ShaderPipeline pipeline;
	private static CompositeRenderer compositeRenderer;
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
		ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
			while (reloadKeybind.wasPressed()){

				try {
					reload();
					minecraftClient.worldRenderer.reload();

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
	}

	private void loadExternalShaderpack(String name) {
		Path shaderPackRoot = shaderpacksDirectory.resolve(name);
		Path shaderPackPath = shaderPackRoot.resolve("shaders");

		if (!Files.exists(shaderPackPath)) {
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

	public static void reload() throws IOException {
		//currently this first line can be used to reload to a diff shaderpack, but it should be removed
		//when there is a gui or a better system for changing packs at runtime
		//or could be kept for the gui to use
		irisConfig.initialize();
		Path shaderPackRoot;
		if (!irisConfig.isInternal()) {
			shaderPackRoot = shaderpacksDirectory.resolve(irisConfig.getShaderPackName());
		} else {
			shaderPackRoot = FabricLoader.getInstance().getModContainer("iris").orElseThrow(() -> new RuntimeException("Failed to get the mod container for Iris!"))
					.getRootPath()
					.resolve("shaders");
		}
		ShaderPack pack = new ShaderPack(shaderPackRoot.resolve("shaders"));
		pipeline = new ShaderPipeline(pack);
		currentPack = pack;
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

	public static CompositeRenderer getCompositeRenderer() {
		if (compositeRenderer == null) {
			compositeRenderer = new CompositeRenderer(Objects.requireNonNull(currentPack));
		}

		return compositeRenderer;
	}

	public static IrisConfig getIrisConfig() {
		return irisConfig;
	}
}
