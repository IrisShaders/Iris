package net.irisshaders.iris;

import com.google.common.base.Throwables;
import com.mojang.blaze3d.opengl.GlDebug;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializerRegistry;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.config.IrisConfig;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.gl.shader.StandardMacros;
import net.irisshaders.iris.gui.debug.DebugLoadFailedGridScreen;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.irisshaders.iris.helpers.OptionalBoolean;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.VanillaRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.irisshaders.iris.shaderpack.DimensionId;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.discovery.ShaderpackDirectoryManager;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.option.OptionSet;
import net.irisshaders.iris.shaderpack.option.Profile;
import net.irisshaders.iris.shaderpack.option.values.MutableOptionValues;
import net.irisshaders.iris.shaderpack.option.values.OptionValues;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.sodium.EntityToTerrainVertexSerializer;
import net.irisshaders.iris.vertices.sodium.GlyphExtVertexSerializer;
import net.irisshaders.iris.vertices.sodium.IrisEntityToTerrainVertexSerializer;
import net.irisshaders.iris.vertices.sodium.ModelToEntityVertexSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.ARBParallelShaderCompile;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.KHRParallelShaderCompile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;
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
	public static final boolean IS_FOOL;
	private static final Map<String, String> shaderPackOptionQueue = new HashMap<>();
	// Change this for snapshots!
	private static final String backupVersionNumber = "1.21.9";
	public static NamespacedId lastDimension = null;
	public static boolean testing = false;
	private static Path shaderpacksDirectory;
	private static ShaderpackDirectoryManager shaderpacksDirectoryManager;
	private static ShaderPack currentPack;
	private static String currentPackName;
	private static Optional<Exception> storedError = Optional.empty();
	private static boolean initialized;
	private static PipelineManager pipelineManager;
	private static IrisConfig irisConfig;
	private static FileSystem zipFileSystem;
	private static KeyMapping reloadKeybind;
	private static final KeyMapping.Category irisKeybindCategory = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("iris", "keybinds"));
	private static KeyMapping toggleShadersKeybind;
	private static KeyMapping shaderpackScreenKeybind;
	private static KeyMapping wireframeKeybind;
	// Flag variable used when reloading
	// Used in favor of queueDefaultShaderPackOptionValues() for resetting as the
	// behavior is more concrete and therefore is more likely to repair a user's issues
	private static boolean resetShaderPackOptions = false;
	private static String IRIS_VERSION;
	private static UpdateChecker updateChecker;
	private static boolean fallback;
	private static boolean loadShaderPackWhenPossible;

	static {
		if (!BuildConfig.ACTIVATE_RENDERDOC && IrisPlatformHelpers.getInstance().isDevelopmentEnvironment() && System.getProperty("user.name").contains("ims") && Util.getPlatform() == Util.OS.LINUX) {
		}

		Calendar c = Calendar.getInstance();
		IS_FOOL = c.get(Calendar.MONTH) == Calendar.APRIL && c.get(Calendar.DAY_OF_MONTH) == 1;
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

		if (GL.getCapabilities().GL_KHR_parallel_shader_compile) {
			KHRParallelShaderCompile.glMaxShaderCompilerThreadsKHR(10);
		} else if (GL.getCapabilities().GL_ARB_parallel_shader_compile) {
			ARBParallelShaderCompile.glMaxShaderCompilerThreadsARB(10);
		}

		PBRTextureManager.INSTANCE.init();

		VertexSerializerRegistry.instance().registerSerializer(DefaultVertexFormat.NEW_ENTITY, IrisVertexFormats.TERRAIN, new EntityToTerrainVertexSerializer());
		VertexSerializerRegistry.instance().registerSerializer(IrisVertexFormats.ENTITY, IrisVertexFormats.TERRAIN, new IrisEntityToTerrainVertexSerializer());
		VertexSerializerRegistry.instance().registerSerializer(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, IrisVertexFormats.GLYPH, new GlyphExtVertexSerializer());
		VertexSerializerRegistry.instance().registerSerializer(DefaultVertexFormat.NEW_ENTITY, IrisVertexFormats.ENTITY, new ModelToEntityVertexSerializer());

		// Only load the shader pack when we can access OpenGL
		if (!IrisPlatformHelpers.getInstance().isModLoaded("distanthorizons")) {
			loadShaderpack();
		}
	}

	public static void duringRenderSystemInit() {
		setDebug(irisConfig.areDebugOptionsEnabled());
	}

	/**
	 * Called when the title screen is initialized for the first time.
	 */
	public static void onLoadingComplete() {
		if (!initialized) {
			Iris.logger.warn("Iris::onLoadingComplete was called, but Iris::onEarlyInitialize was not called." +
				" Trying to avoid a crash but this is an odd state.");
			return;
		}

		// Initialize the pipeline now so that we don't increase world loading time. Just going to guess that
		// the player is in the overworld.
		// See: https://github.com/IrisShaders/Iris/issues/323
		lastDimension = DimensionId.OVERWORLD;
		Iris.getPipelineManager().preparePipeline(DimensionId.OVERWORLD);
	}

	public static void handleKeybinds(Minecraft minecraft) {
		if (loadShaderPackWhenPossible) {
			loadShaderPackWhenPossible = false;
			Iris.loadShaderpack();
		}

		if (reloadKeybind.consumeClick()) {
			try {
				reload();

				if (minecraft.player != null) {
					minecraft.player.displayClientMessage(Component.translatable("iris.shaders.reloaded"), false);
				}

			} catch (Exception e) {
				logger.error("Error while reloading Shaders for Iris!", e);

				if (minecraft.player != null) {
					minecraft.player.displayClientMessage(Component.translatable("iris.shaders.reloaded.failure", Throwables.getRootCause(e).getMessage()).withStyle(ChatFormatting.RED), false);
				}
			}
		} else if (toggleShadersKeybind.consumeClick()) {
			try {
				toggleShaders(minecraft, !irisConfig.areShadersEnabled());
			} catch (Exception e) {
				logger.error("Error while toggling shaders!", e);

				if (minecraft.player != null) {
					minecraft.player.displayClientMessage(Component.translatable("iris.shaders.toggled.failure", Throwables.getRootCause(e).getMessage()).withStyle(ChatFormatting.RED), false);
				}
				setShadersDisabled();
				fallback = true;
			}
		} else if (shaderpackScreenKeybind.consumeClick()) {
			minecraft.setScreen(new ShaderPackScreen(null));
		} else if (wireframeKeybind.consumeClick()) {
			if (irisConfig.areDebugOptionsEnabled() && minecraft.player != null && !Minecraft.getInstance().isLocalServer()) {
				minecraft.player.displayClientMessage(Component.literal("No cheating; wireframe only in singleplayer!"), false);
			}
		}
	}

	public static boolean shouldActivateWireframe() {
		return irisConfig.areDebugOptionsEnabled() && wireframeKeybind.isDown();
	}

	public static void toggleShaders(Minecraft minecraft, boolean enabled) throws IOException {
		irisConfig.setShadersEnabled(enabled);
		irisConfig.save();

		reload();
		if (minecraft.player != null) {
			minecraft.player.displayClientMessage(enabled ? Component.translatable("iris.shaders.toggled", currentPackName) : Component.translatable("iris.shaders.disabled"), false);
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

		if (externalName.isEmpty()) {
			logger.info("Shaders are disabled because no valid shaderpack is selected");

			setShadersDisabled();

			return;
		}

		if (!loadExternalShaderpack(externalName.get())) {
			logger.warn("Falling back to normal rendering without shaders because the shaderpack could not be loaded");
			setShadersDisabled();
			fallback = true;
		}
	}

	@SuppressWarnings("unchecked")
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

		if (!isValidShaderpack(shaderPackRoot)) {
			logger.error("Pack \"{}\" is not valid! Can't load it.", name);
			return false;
		}

		Path shaderPackPath;
		boolean isZip = false;

		if (!Files.isDirectory(shaderPackRoot) && shaderPackRoot.toString().endsWith(".zip")) {
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
			isZip = true;
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
			.map(properties -> (Map<String, String>) (Object) properties)
			.orElse(new HashMap<>());

		changedConfigs.putAll(shaderPackOptionQueue);
		clearShaderPackOptionQueue();

		if (resetShaderPackOptions) {
			changedConfigs.clear();
		}
		resetShaderPackOptions = false;

		try {
			currentPack = new ShaderPack(shaderPackPath, changedConfigs, StandardMacros.createStandardEnvironmentDefines(), isZip);

			MutableOptionValues changedConfigsValues = currentPack.getShaderPackOptions().getOptionValues().mutableCopy();

			// Store changed values from those currently in use by the shader pack
			Properties configsToSave = new Properties();
			changedConfigsValues.getBooleanValues().forEach((k, v) -> configsToSave.setProperty(k, Boolean.toString(v)));
			changedConfigsValues.getStringValues().forEach(configsToSave::setProperty);

			tryUpdateConfigPropertiesFile(shaderPackConfigTxt, configsToSave);
		} catch (Exception e) {
			logger.error("Failed to load the shaderpack \"{}\"!", name);
			logger.error("", e);
			handleException(e);

			return false;
		}

		fallback = false;
		currentPackName = name;

		logger.info("Using shaderpack: " + name);

		return true;
	}

	private static void handleException(Exception e) {
		if (irisConfig.areDebugOptionsEnabled()) {
			Minecraft.getInstance().setScreen(new DebugLoadFailedGridScreen(Minecraft.getInstance().screen, Component.literal(e instanceof ShaderCompileException ? "Failed to compile shaders" : "Exception"), e));
		} else {
			if (Minecraft.getInstance().player != null) {
				Minecraft.getInstance().player.displayClientMessage(Component.translatable(e instanceof ShaderCompileException ? "iris.load.failure.shader" : "iris.load.failure.generic").append(Component.literal("Copy Info").withStyle(arg -> arg.withUnderlined(true).withColor(
					ChatFormatting.BLUE).withClickEvent(new ClickEvent.CopyToClipboard(e.getMessage())).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.copy.click"))))), false);
			} else {
				storedError = Optional.of(e);
			}
		}
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
		try (Stream<Path> stream = Files.walk(root)) {
			return stream
				.filter(Files::isDirectory)
				.filter(path -> path.endsWith("shaders"))
				.findFirst();
		}
	}

	private static void setShadersDisabled() {
		currentPack = null;
		fallback = false;
		currentPackName = "(off)";
	}

	public static void setDebug(boolean enable) {
		try {
			irisConfig.setDebugEnabled(enable);
			irisConfig.save();
		} catch (IOException e) {
			Iris.logger.fatal("Failed to save config!", e);
		}

		int success;
		if (enable) {
			success = GLDebug.setupDebugMessageCallback();
		} else {
			GLDebug.reloadDebugState();
			GlDebug.enableDebugCallback(Minecraft.getInstance().options.glDebugVerbosity, false, new HashSet<>(((GlDevice) RenderSystem.getDevice()).getEnabledExtensions()));
			success = 1;
		}

		logger.info("Debug functionality is " + (enable ? "enabled, logging will be more verbose!" : "disabled."));
		if (Minecraft.getInstance().player != null) {
			if (IrisPlatformHelpers.getInstance().useELS()) {
				Minecraft.getInstance().player.displayClientMessage(Component.translatable("iris.shaders.debug.restartNoDebug"), false);
			} else {
				Minecraft.getInstance().player.displayClientMessage(Component.translatable(success != 0 ? (enable ? "iris.shaders.debug.enabled" : "iris.shaders.debug.disabled") : "iris.shaders.debug.failure"), false);
			}

			if (success == 2 && !IrisPlatformHelpers.getInstance().useELS()) {
				Minecraft.getInstance().player.displayClientMessage(Component.translatable("iris.shaders.debug.restart"), false);
			}
		}
	}

	private static Optional<Properties> tryReadConfigProperties(Path path) {
		Properties properties = new Properties();

		if (Files.exists(path)) {
			try (InputStream is = Files.newInputStream(path)) {
				// NB: config properties are specified to be encoded with ISO-8859-1 by OptiFine,
				//     so we don't need to do the UTF-8 workaround here.
				properties.load(is);
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

	public static boolean isValidToShowPack(Path pack) {
		return Files.isDirectory(pack) || pack.toString().endsWith(".zip");
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
			return pack.resolve("shaders").toFile().exists();
		}

		if (pack.toString().endsWith(".zip")) {
			try (FileSystem zipSystem = FileSystems.newFileSystem(pack, Iris.class.getClassLoader())) {
				Path root = zipSystem.getRootDirectories().iterator().next();
				try (Stream<Path> stream = Files.walk(root)) {
					return stream
						.filter(Files::isDirectory)
						.anyMatch(path -> path.endsWith("shaders"));
				}
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

	public static boolean shouldResetShaderPackOptionsOnNextReload() {
		return resetShaderPackOptions;
	}

	public static void reload() throws IOException {
		// allows shaderpacks to be changed at runtime
		irisConfig.initialize();

		// Destroy all allocated resources
		destroyEverything();

		// Load the new shaderpack
		loadShaderpack();

		// Very important - we need to re-create the pipeline straight away.
		// https://github.com/IrisShaders/Iris/issues/1330
		if (Minecraft.getInstance().level != null) {
			Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());
		}
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

	public static NamespacedId getCurrentDimension() {
		ClientLevel level = Minecraft.getInstance().level;

		if (level != null) {
			NamespacedId dimensionId = new NamespacedId(level.dimension().location().getNamespace(), level.dimension().location().getPath());

			ShaderPack pack = getCurrentPack().orElse(null);

			// If there is an exact match in dimension.properties, don't override using dimension type effects
			if (pack != null && pack.getDimensionMap().containsKey(dimensionId)) {
				return dimensionId;
			}

			// Check if the dimension type of the current level has custom effects set (end sky or nether).
			// This is minecraft:overworld by default, but can also be minecraft:the_nether or minecraft:the_end.
			// The appropriate shader for the dimension should be used by default in order to prevent buggy results.
			// More information at https://minecraft.wiki/w/Dimension_type
			// https://github.com/IrisShaders/Iris/issues/2200
			ResourceLocation effects = level.dimensionType().effectsLocation();

			if (Level.END.location().equals(effects)) {
				return DimensionId.END;
			}

			if (Level.NETHER.location().equals(effects)) {
				return DimensionId.NETHER;
			}

			return dimensionId;
		} else {
			// This prevents us from reloading the shaderpack unless we need to. Otherwise, if the player is in the
			// nether and quits the game, we might end up reloading the shaders on exit and on entry to the level
			// because the code thinks that the dimension changed.
			return lastDimension;
		}
	}

	private static WorldRenderingPipeline createPipeline(NamespacedId dimensionId) {
		if (currentPack == null) {
			// Completely disables shader-based rendering
			return new VanillaRenderingPipeline();
		}

		ProgramSet programs = currentPack.getProgramSet(dimensionId);

		// We use DeferredWorldRenderingPipeline on 1.16, and NewWorldRendering pipeline on 1.17 when rendering shaders.
		try {
			return new IrisRenderingPipeline(programs);
		} catch (Exception e) {
			handleException(e);

			ShaderStorageBufferHolder.forceDeleteBuffers();
			logger.error("Failed to create shader rendering pipeline, disabling shaders!", e);
			// TODO: This should be reverted if a dimension change causes shaders to compile again
			fallback = true;

			return new VanillaRenderingPipeline();
		}
	}

	@NotNull
	public static PipelineManager getPipelineManager() {
		if (pipelineManager == null) {
			pipelineManager = new PipelineManager(Iris::createPipeline);
		}

		return pipelineManager;
	}

	public static Optional<Exception> getStoredError() {
		Optional<Exception> stored = Iris.storedError;
		storedError = Optional.empty();
		return stored;
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

	public static UpdateChecker getUpdateChecker() {
		return updateChecker;
	}

	public static boolean isFallback() {
		return fallback;
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

		if (IrisPlatformHelpers.getInstance().isDevelopmentEnvironment()) {
			color = ChatFormatting.GOLD;
			version = version + " (Development Environment)";
		} else if (version.endsWith("-dirty") || version.contains("unknown") || version.endsWith("-nogit")) {
			color = ChatFormatting.RED;
		} else if (version.contains("+rev.")) {
			color = ChatFormatting.LIGHT_PURPLE;
		} else {
			color = ChatFormatting.GREEN;
		}

		return color + version;
	}

	/**
	 * Gets the current release target. Since 1.19.3, Mojang no longer stores this information, so we must manually provide it for snapshots.
	 *
	 * @return Release target
	 */
	public static String getReleaseTarget() {
		// If this is a snapshot, you must change backupVersionNumber!
		SharedConstants.tryDetectVersion();
		return SharedConstants.getCurrentVersion().stable() ? SharedConstants.getCurrentVersion().name() : backupVersionNumber;
	}

	public static String getBackupVersionNumber() {
		return backupVersionNumber;
	}

	public static Path getShaderpacksDirectory() {
		if (shaderpacksDirectory == null) {
			shaderpacksDirectory = IrisPlatformHelpers.getInstance().getGameDir().resolve("shaderpacks");
		}

		return shaderpacksDirectory;
	}

	public static ShaderpackDirectoryManager getShaderpacksDirectoryManager() {
		if (shaderpacksDirectoryManager == null) {
			shaderpacksDirectoryManager = new ShaderpackDirectoryManager(getShaderpacksDirectory());
		}

		return shaderpacksDirectoryManager;
	}

	public static boolean loadedIncompatiblePack() {
		return DHCompat.lastPackIncompatible();
	}

	public static boolean isPackInUseQuick() {
		return getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline;
	}

	public static void loadShaderpackWhenPossible() {
		loadShaderPackWhenPossible = true;
	}

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
		IRIS_VERSION = IrisPlatformHelpers.getInstance().getVersion();

		updateChecker = new UpdateChecker(IRIS_VERSION);

		reloadKeybind = IrisPlatformHelpers.getInstance().registerKeyBinding(new KeyMapping("iris.keybind.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, irisKeybindCategory));
		toggleShadersKeybind = IrisPlatformHelpers.getInstance().registerKeyBinding(new KeyMapping("iris.keybind.toggleShaders", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, irisKeybindCategory));
		shaderpackScreenKeybind = IrisPlatformHelpers.getInstance().registerKeyBinding(new KeyMapping("iris.keybind.shaderPackSelection", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, irisKeybindCategory));
		wireframeKeybind = IrisPlatformHelpers.getInstance().registerKeyBinding(new KeyMapping("iris.keybind.wireframe", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), irisKeybindCategory));

		DHCompat.run();

		try {
			if (!Files.exists(getShaderpacksDirectory())) {
				Files.createDirectories(getShaderpacksDirectory());
			}
		} catch (IOException e) {
			logger.warn("Failed to create the shaderpacks directory!");
			logger.warn("", e);
		}

		irisConfig = new IrisConfig(IrisPlatformHelpers.getInstance().getConfigDir().resolve("iris.properties"), IrisPlatformHelpers.getInstance().getConfigDir().resolve("iris-excluded.json"));

		try {
			irisConfig.initialize();
		} catch (IOException e) {
			logger.error("Failed to initialize Iris configuration, default values will be used instead");
			logger.error("", e);
		}

		updateChecker.checkForUpdates(irisConfig);

		initialized = true;
	}
}
