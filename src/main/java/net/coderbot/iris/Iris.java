package net.coderbot.iris;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.google.common.base.Throwables;
import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.pipeline.ShaderPipeline;
import net.coderbot.iris.postprocess.CompositeRenderer;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ShaderPack;
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

	private static final Path shaderpacksDirectory = FabricLoader.getInstance().getGameDir().resolve("shaderpacks");

	private static ShaderPack currentPack;
	private static ShaderPipeline pipeline;
	private static RenderTargets renderTargets;
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


		loadShaderpack();
		reloadKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("iris.keybind.reload", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "iris.keybinds"));

		ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
			if (reloadKeybind.wasPressed()){

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

	public static void loadShaderpack() {
		// Attempt to load an external shaderpack if it is available
		if (!irisConfig.isInternal()) {
			if (!loadExternalShaderpack(irisConfig.getShaderPackName())) {
				loadInternalShaderpack();
			}
		} else {
			loadInternalShaderpack();
		}
	}

	private static boolean loadExternalShaderpack(String name) {
		Path shaderPackRoot = shaderpacksDirectory.resolve(name);
		Path shaderPackPath = shaderPackRoot.resolve("shaders");

		if (!Files.exists(shaderPackPath)) {
			logger.warn("The shaderpack " + name + " does not have a shaders directory, falling back to internal shaders");
			return false;
		}

		try {
			currentPack = new ShaderPack(shaderPackPath);
		} catch (IOException e) {
			logger.error(String.format("Failed to load shaderpack \"%s\"! Falling back to internal shaders", irisConfig.getShaderPackName()));
			logger.catching(Level.ERROR, e);

			return false;
		}

		logger.info("Using shaderpack: " + name);
		return true;
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
	}

	public static void reload() throws IOException {
		irisConfig.initialize();//allows shaderpacks to be changed at runtime
		loadShaderpack();
		pipeline = null;//set to null so getPipeline catches and resets it
	}

	public static RenderTargets getRenderTargets() {
		if (renderTargets == null) {
			renderTargets = new RenderTargets(MinecraftClient.getInstance().getFramebuffer(), Objects.requireNonNull(currentPack));
		}

		return renderTargets;
	}

	public static ShaderPipeline getPipeline() {
		if (pipeline == null) {
			pipeline = new ShaderPipeline(Objects.requireNonNull(currentPack), getRenderTargets());
		}

		return pipeline;
	}

	public static ShaderPack getCurrentPack() {
		return currentPack;
	}

	public static CompositeRenderer getCompositeRenderer() {
		if (compositeRenderer == null) {
			compositeRenderer = new CompositeRenderer(Objects.requireNonNull(currentPack), getRenderTargets());
		}

		return compositeRenderer;
	}

	public static IrisConfig getIrisConfig() {
		return irisConfig;
	}
}
