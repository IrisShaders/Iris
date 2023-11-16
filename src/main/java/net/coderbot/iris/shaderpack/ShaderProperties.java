package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.AlphaTestFunction;
import net.coderbot.iris.gl.blending.BlendMode;
import net.coderbot.iris.gl.blending.BlendModeFunction;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.buffer.ShaderStorageInfo;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import net.coderbot.iris.gl.texture.TextureDefinition;
import net.coderbot.iris.gl.texture.TextureScaleOverride;
import net.coderbot.iris.gl.blending.BufferBlendInformation;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;
import net.coderbot.iris.shaderpack.preprocessor.PropertiesPreprocessor;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.coderbot.iris.uniforms.custom.CustomUniforms;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The parsed representation of the shaders.properties file. This class is not meant to be stored permanently, rather
 * it merely exists as an intermediate step until we build up PackDirectives and ProgramDirectives objects from the
 * values in here & the values parsed from shader source code.
 */
public class ShaderProperties {
	private int customTexAmount;
	private CloudSetting cloudSetting = CloudSetting.DEFAULT;
	private OptionalBoolean oldHandLight = OptionalBoolean.DEFAULT;
	private OptionalBoolean dynamicHandLight = OptionalBoolean.DEFAULT;
	private OptionalBoolean supportsColorCorrection = OptionalBoolean.DEFAULT;
	private OptionalBoolean oldLighting = OptionalBoolean.DEFAULT;
	private OptionalBoolean shadowTerrain = OptionalBoolean.DEFAULT;
	private OptionalBoolean shadowTranslucent = OptionalBoolean.DEFAULT;
	private OptionalBoolean shadowEntities = OptionalBoolean.DEFAULT;
	private OptionalBoolean shadowPlayer = OptionalBoolean.DEFAULT;
	private OptionalBoolean shadowBlockEntities = OptionalBoolean.DEFAULT;
	private OptionalBoolean underwaterOverlay = OptionalBoolean.DEFAULT;
	private OptionalBoolean sun = OptionalBoolean.DEFAULT;
	private OptionalBoolean moon = OptionalBoolean.DEFAULT;
	private OptionalBoolean vignette = OptionalBoolean.DEFAULT;
	private OptionalBoolean backFaceSolid = OptionalBoolean.DEFAULT;
	private OptionalBoolean backFaceCutout = OptionalBoolean.DEFAULT;
	private OptionalBoolean backFaceCutoutMipped = OptionalBoolean.DEFAULT;
	private OptionalBoolean backFaceTranslucent = OptionalBoolean.DEFAULT;
	private OptionalBoolean rainDepth = OptionalBoolean.DEFAULT;
	private OptionalBoolean concurrentCompute = OptionalBoolean.DEFAULT;
	private OptionalBoolean beaconBeamDepth = OptionalBoolean.DEFAULT;
	private OptionalBoolean separateAo = OptionalBoolean.DEFAULT;
	private OptionalBoolean voxelizeLightBlocks = OptionalBoolean.DEFAULT;
	private OptionalBoolean separateEntityDraws = OptionalBoolean.DEFAULT;
	private OptionalBoolean frustumCulling = OptionalBoolean.DEFAULT;
	private ShadowCullState shadowCulling = ShadowCullState.DEFAULT;
	private OptionalBoolean shadowEnabled = OptionalBoolean.DEFAULT;
	private Optional<ParticleRenderingSettings> particleRenderingSettings = Optional.empty();
	private OptionalBoolean prepareBeforeShadow = OptionalBoolean.DEFAULT;
	private List<String> sliderOptions = new ArrayList<>();
	private final Map<String, List<String>> profiles = new LinkedHashMap<>();
	private List<String> mainScreenOptions = null;
	private final Map<String, List<String>> subScreenOptions = new HashMap<>();
	private Integer mainScreenColumnCount = null;
	private final Map<String, Integer> subScreenColumnCount = new HashMap<>();
	// TODO: private Map<String, String> optifineVersionRequirements;
	// TODO: Parse custom uniforms / variables
	private final Object2ObjectMap<String, AlphaTest> alphaTestOverrides = new Object2ObjectOpenHashMap<>();
	private final Object2FloatMap<String> viewportScaleOverrides = new Object2FloatOpenHashMap<>();
	private final Object2ObjectMap<String, TextureScaleOverride> textureScaleOverrides = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectMap<String, BlendModeOverride> blendModeOverrides = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectMap<String, ArrayList<BufferBlendInformation>> bufferBlendOverrides = new Object2ObjectOpenHashMap<>();
	private final EnumMap<TextureStage, Object2ObjectMap<String, TextureDefinition>> customTextures = new EnumMap<>(TextureStage.class);
	private final Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> customTexturePatching = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectMap<String, TextureDefinition> irisCustomTextures = new Object2ObjectOpenHashMap<>();
	private final List<ImageInformation> irisCustomImages = new ArrayList<>();
	private final Int2ObjectArrayMap<ShaderStorageInfo> bufferObjects = new Int2ObjectArrayMap<>();
	private final Object2ObjectMap<String, Object2BooleanMap<String>> explicitFlips = new Object2ObjectOpenHashMap<>();
	private String noiseTexturePath = null;
	CustomUniforms.Builder customUniforms = new CustomUniforms.Builder();
	private Object2ObjectMap<String, String> conditionallyEnabledPrograms = new Object2ObjectOpenHashMap<>();
	private List<String> requiredFeatureFlags = new ArrayList<>();
	private List<String> optionalFeatureFlags = new ArrayList<>();

	private ShaderProperties() {
		// empty
	}

	// TODO: Is there a better solution than having ShaderPack pass a root path to ShaderProperties to be able to read textures?
	public ShaderProperties(String contents, ShaderPackOptions shaderPackOptions, Iterable<StringPair> environmentDefines, Iterable<StringPair> replacements) {
		for (StringPair pair : replacements) {
			contents = contents.replace(pair.getKey(), pair.getValue());
		}

		String preprocessedContents = PropertiesPreprocessor.preprocessSource(contents, shaderPackOptions, environmentDefines);

		Properties preprocessed = new OrderBackedProperties();
		Properties original = new OrderBackedProperties();
		try {
			preprocessed.load(new StringReader(preprocessedContents));
			original.load(new StringReader(contents));
		} catch (IOException e) {
			Iris.logger.error("Error loading shaders.properties!", e);
		}

		preprocessed.forEach((keyObject, valueObject) -> {
			String key = (String) keyObject;
			String value = (String) valueObject;

			if ("texture.noise".equals(key)) {
				noiseTexturePath = value;
				return;
			}

			if ("clouds".equals(key)) {
				if ("off".equals(value)) {
					cloudSetting = CloudSetting.OFF;
				} else if ("fast".equals(value)) {
					cloudSetting = CloudSetting.FAST;
				} else if ("fancy".equals(value)) {
					cloudSetting = CloudSetting.FANCY;
				} else {
					Iris.logger.error("Unrecognized clouds setting: " + value);
				}
			}

			if ("shadow.culling".equals(key)) {
				if ("false".equals(value)) {
					shadowCulling = ShadowCullState.DISTANCE;
				} else if ("true".equals(value)) {
					shadowCulling = ShadowCullState.ADVANCED;
				} else if ("reversed".equals(value)) {
					shadowCulling = ShadowCullState.REVERSED;
				} else {
					Iris.logger.error("Unrecognized shadow culling setting: " + value);
				}
			}

			handleBooleanDirective(key, value, "oldHandLight", bool -> oldHandLight = bool);
			handleBooleanDirective(key, value, "dynamicHandLight", bool -> dynamicHandLight = bool);
			handleBooleanDirective(key, value, "oldLighting", bool -> oldLighting = bool);
			handleBooleanDirective(key, value, "shadowTerrain", bool -> shadowTerrain = bool);
			handleBooleanDirective(key, value, "shadowTranslucent", bool -> shadowTranslucent = bool);
			handleBooleanDirective(key, value, "shadowEntities", bool -> shadowEntities = bool);
			handleBooleanDirective(key, value, "shadowPlayer", bool -> shadowPlayer = bool);
			handleBooleanDirective(key, value, "shadowBlockEntities", bool -> shadowBlockEntities = bool);
			handleBooleanDirective(key, value, "underwaterOverlay", bool -> underwaterOverlay = bool);
			handleBooleanDirective(key, value, "sun", bool -> sun = bool);
			handleBooleanDirective(key, value, "moon", bool -> moon = bool);
			handleBooleanDirective(key, value, "vignette", bool -> vignette = bool);
			handleBooleanDirective(key, value, "backFace.solid", bool -> backFaceSolid = bool);
			handleBooleanDirective(key, value, "backFace.cutout", bool -> backFaceCutout = bool);
			handleBooleanDirective(key, value, "backFace.cutoutMipped", bool -> backFaceCutoutMipped = bool);
			handleBooleanDirective(key, value, "backFace.translucent", bool -> backFaceTranslucent = bool);
			handleBooleanDirective(key, value, "rain.depth", bool -> rainDepth = bool);
			handleBooleanDirective(key, value, "allowConcurrentCompute", bool -> concurrentCompute = bool);
			handleBooleanDirective(key, value, "beacon.beam.depth", bool -> beaconBeamDepth = bool);
			handleBooleanDirective(key, value, "separateAo", bool -> separateAo = bool);
			handleBooleanDirective(key, value, "voxelizeLightBlocks", bool -> voxelizeLightBlocks = bool);
			handleBooleanDirective(key, value, "separateEntityDraws", bool -> separateEntityDraws = bool);
			handleBooleanDirective(key, value, "frustum.culling", bool -> frustumCulling = bool);
			handleBooleanDirective(key, value, "shadow.enabled", bool -> shadowEnabled = bool);
			handleBooleanDirective(key, value, "particles.before.deferred", bool -> {
				if (bool.orElse(false) && particleRenderingSettings.isEmpty()) {
					particleRenderingSettings = Optional.of(ParticleRenderingSettings.BEFORE);
				}
			});
			handleBooleanDirective(key, value, "prepareBeforeShadow", bool -> prepareBeforeShadow = bool);
			handleBooleanDirective(key, value, "supportsColorCorrection", bool -> supportsColorCorrection = bool);

			if (key.startsWith("particles.ordering")) {
				Optional<ParticleRenderingSettings> settings = ParticleRenderingSettings.fromString(value.trim().toUpperCase(Locale.US));
				if (settings.isPresent()) {
					particleRenderingSettings = settings;
				} else {
					throw new RuntimeException("Failed to parse particle rendering order! " + value);
				}
			}

			// TODO: Min optifine versions, shader options layout / appearance / profiles
			// TODO: Custom uniforms

			handlePassDirective("scale.", key, value, pass -> {
				float scale;

				try {
					scale = Float.parseFloat(value);
				} catch (NumberFormatException e) {
					Iris.logger.error("Unable to parse scale directive for " + pass + ": " + value, e);
					return;
				}

				viewportScaleOverrides.put(pass, scale);
			});

			handlePassDirective("size.buffer.", key, value, pass -> {
				String[] parts = value.split(" ");

				if (parts.length != 2) {
					Iris.logger.error("Unable to parse size.buffer directive for " + pass + ": " + value);
					return;
				}

				textureScaleOverrides.put(pass, new TextureScaleOverride(parts[0], parts[1]));
			});

			handlePassDirective("alphaTest.", key, value, pass -> {
				if ("off".equals(value) || "false".equals(value)) {
					alphaTestOverrides.put(pass, AlphaTest.ALWAYS);
					return;
				}

				String[] parts = value.split(" ");

				if (parts.length > 2) {
					Iris.logger.warn("Weird alpha test directive for " + pass + " contains more parts than we expected: " + value);
				} else if (parts.length < 2) {
					Iris.logger.error("Invalid alpha test directive for " + pass + ": " + value);
					return;
				}

				Optional<AlphaTestFunction> function = AlphaTestFunction.fromString(parts[0]);

				if (!function.isPresent()) {
					Iris.logger.error("Unable to parse alpha test directive for " + pass + ", unknown alpha test function " + parts[0] + ": " + value);
					return;
				}

				float reference;

				try {
					reference = Float.parseFloat(parts[1]);
				} catch (NumberFormatException e) {
					Iris.logger.error("Unable to parse alpha test directive for " + pass + ": " + value, e);
					return;
				}

				alphaTestOverrides.put(pass, new AlphaTest(function.get(), reference));
			});

			handlePassDirective("blend.", key, value, pass -> {
				if (pass.contains(".")) {

					if (!IrisRenderSystem.supportsBufferBlending()) {
						throw new RuntimeException("Buffer blending is not supported on this platform, however it was attempted to be used!");
					}

					String[] parts = pass.split("\\.");
					int index = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.indexOf(parts[1]);

					if (index == -1 && parts[1].startsWith("colortex")) {
						String id = parts[1].substring("colortex".length());

						try {
							index = Integer.parseInt(id);
						} catch (NumberFormatException e) {
							throw new RuntimeException("Failed to parse buffer blend!", e);
						}
					}

					if (index == -1) {
						throw new RuntimeException("Failed to parse buffer blend! index = " + index);
					}

					if ("off".equals(value)) {
						bufferBlendOverrides.computeIfAbsent(parts[0], list -> new ArrayList<>()).add(new BufferBlendInformation(index, null));
						return;
					}

					String[] modeArray = value.split(" ");
					int[] modes = new int[modeArray.length];

					int i = 0;
					for (String modeName : modeArray) {
						modes[i] = BlendModeFunction.fromString(modeName).get().getGlId();
						i++;
					}

					bufferBlendOverrides.computeIfAbsent(parts[0], list -> new ArrayList<>()).add(new BufferBlendInformation(index, new BlendMode(modes[0], modes[1], modes[2], modes[3])));

					return;
				}

				if ("off".equals(value)) {
					blendModeOverrides.put(pass, BlendModeOverride.OFF);
					return;
				}

				String[] modeArray = value.split(" ");
				int[] modes = new int[modeArray.length];

				int i = 0;
				for (String modeName : modeArray) {
					modes[i] = BlendModeFunction.fromString(modeName).get().getGlId();
					i++;
				}

				blendModeOverrides.put(pass, new BlendModeOverride(new BlendMode(modes[0], modes[1], modes[2], modes[3])));
			});

			handleProgramEnabledDirective("program.", key, value, program -> {
				conditionallyEnabledPrograms.put(program, value);
			});

			handlePassDirective("bufferObject.", key, value, index -> {
				int trueIndex;
				int trueSize;
				boolean isRelative;
				float scaleX, scaleY;
				String[] parts = value.split(" ");
				if (parts.length == 1) {
					try {
						trueIndex = Integer.parseInt(index);
						trueSize = Integer.parseInt(value);
					} catch (NumberFormatException e) {
						Iris.logger.error("Number format exception parsing SSBO index/size!", e);
						return;
					}

					if (trueIndex > 8) {
						Iris.logger.fatal("SSBO's cannot use buffer numbers higher than 8, they're reserved!");
						return;
					}

					bufferObjects.put(trueIndex, new ShaderStorageInfo(trueSize, false, 0, 0));
				} else {
					// Assume it's a long one
					try {
						trueIndex = Integer.parseInt(index);
						trueSize = Integer.parseInt(parts[0]);
						isRelative = Boolean.parseBoolean(parts[1]);
						scaleX = Float.parseFloat(parts[2]);
						scaleY = Float.parseFloat(parts[3]);
					} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
						Iris.logger.error("Number format exception parsing SSBO index/size, or not correct format!", e);
						return;
					}

					if (trueIndex > 8) {
						Iris.logger.fatal("SSBO's cannot use buffer numbers higher than 8, they're reserved!");
						return;
					}

					bufferObjects.put(trueIndex, new ShaderStorageInfo(trueSize, isRelative, scaleX, scaleY));
				}
			});

			handleTwoArgDirective("texture.", key, value, (stageName, samplerName) -> {
				String[] parts = value.split(" ");
				// TODO: Is there a better way to achieve this?
				samplerName = samplerName.split("\\.")[0];

				Optional<TextureStage> optionalTextureStage = TextureStage.parse(stageName);

				if (!optionalTextureStage.isPresent()) {
					Iris.logger.warn("Unknown texture stage " + "\"" + stageName + "\"," + " ignoring custom texture directive for " + key);
					return;
				}

				TextureStage stage = optionalTextureStage.get();

				if (parts.length > 1) {
					String newSamplerName = "customtex" + customTexAmount;
					customTexAmount++;
					TextureType type = null;
					// Raw texture handling
					if (parts.length == 6) {
						// 1D texture handling
						type = TextureType.TEXTURE_1D;
						irisCustomTextures.put(newSamplerName, new TextureDefinition.RawDefinition(parts[0], TextureType.valueOf(parts[1].toUpperCase(Locale.ROOT)), InternalTextureFormat.fromString(parts[2]).orElseThrow(IllegalArgumentException::new), Integer.parseInt(parts[3]), 0, 0, PixelFormat.fromString(parts[4]).orElseThrow(IllegalArgumentException::new), PixelType.fromString(parts[5]).orElseThrow(IllegalArgumentException::new)));
					} else if (parts.length == 7) {
						// 2D texture handling
						type = TextureType.valueOf(parts[1].toUpperCase(Locale.ROOT));
						irisCustomTextures.put(newSamplerName, new TextureDefinition.RawDefinition(parts[0], TextureType.valueOf(parts[1].toUpperCase(Locale.ROOT)), InternalTextureFormat.fromString(parts[2]).orElseThrow(IllegalArgumentException::new), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), 0, PixelFormat.fromString(parts[5]).orElseThrow(IllegalArgumentException::new), PixelType.fromString(parts[6]).orElseThrow(IllegalArgumentException::new)));
					} else if (parts.length == 8) {
						// 3D texture handling
						type = TextureType.TEXTURE_3D;
						irisCustomTextures.put(newSamplerName, new TextureDefinition.RawDefinition(parts[0], TextureType.valueOf(parts[1].toUpperCase(Locale.ROOT)), InternalTextureFormat.fromString(parts[2]).orElseThrow(IllegalArgumentException::new), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), PixelFormat.fromString(parts[6]).orElseThrow(IllegalArgumentException::new), PixelType.fromString(parts[7]).orElseThrow(IllegalArgumentException::new)));
					} else {
						Iris.logger.warn("Unknown texture directive for " + key + ": " + value);
					}

					customTexturePatching.put(new Tri<>(samplerName, type, stage), newSamplerName);

					return;
				}

				customTextures.computeIfAbsent(stage, _stage -> new Object2ObjectOpenHashMap<>())
						.put(samplerName, new TextureDefinition.PNGDefinition(value));
			});

			handlePassDirective("customTexture.", key, value, (samplerName) -> {
				String[] parts = value.split(" ");

				// TODO: Support raw textures
				if (parts.length > 1) {
					// Raw texture handling
					if (parts.length == 6) {
						// 1D texture handling
						irisCustomTextures.put(samplerName, new TextureDefinition.RawDefinition(parts[0], TextureType.valueOf(parts[1].toUpperCase(Locale.ROOT)), InternalTextureFormat.fromString(parts[2]).orElseThrow(IllegalArgumentException::new), Integer.parseInt(parts[3]), 0, 0, PixelFormat.fromString(parts[4]).orElseThrow(IllegalArgumentException::new), PixelType.fromString(parts[5]).orElseThrow(IllegalArgumentException::new)));
					} else if (parts.length == 7) {
						// 2D texture handling
						irisCustomTextures.put(samplerName, new TextureDefinition.RawDefinition(parts[0], TextureType.valueOf(parts[1].toUpperCase(Locale.ROOT)), InternalTextureFormat.fromString(parts[2]).orElseThrow(IllegalArgumentException::new), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), 0, PixelFormat.fromString(parts[5]).orElseThrow(IllegalArgumentException::new), PixelType.fromString(parts[6]).orElseThrow(IllegalArgumentException::new)));
					} else if (parts.length == 8) {
						// 3D texture handling
						irisCustomTextures.put(samplerName, new TextureDefinition.RawDefinition(parts[0], TextureType.valueOf(parts[1].toUpperCase(Locale.ROOT)), InternalTextureFormat.fromString(parts[2]).orElseThrow(IllegalArgumentException::new), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), PixelFormat.fromString(parts[6]).orElseThrow(IllegalArgumentException::new), PixelType.fromString(parts[7]).orElseThrow(IllegalArgumentException::new)));
					} else {
						Iris.logger.warn("Unknown texture directive for " + key + ": " + value);
					}

					return;
				}

				irisCustomTextures.put(samplerName, new TextureDefinition.PNGDefinition(value));
			});

			handlePassDirective("image.", key, value, (imageName) -> {
				String[] parts = value.split(" ");
				String key2 = key.substring(6);

				if (irisCustomImages.size() > 15) {
					Iris.logger.error("Only up to 16 images are allowed, but tried to add another image! " + key);
					return;
				}

				ImageInformation image;

				String samplerName = parts[0];
				if (samplerName.equals("none")) {
					samplerName = null;
				}
				PixelFormat format = PixelFormat.fromString(parts[1]).orElse(null);
				InternalTextureFormat internalFormat = InternalTextureFormat.fromString(parts[2]).orElse(null);
				PixelType pixelType = PixelType.fromString(parts[3]).orElse(null);

				if (format == null || internalFormat == null || pixelType == null) {
					Iris.logger.error("Image " + key2 + " is invalid! Format: " + format + " Internal format: " + internalFormat + " Pixel type: " + pixelType);
				}

				boolean clear = Boolean.parseBoolean(parts[4]);

				boolean relative = Boolean.parseBoolean(parts[5]);

				if (relative) { // Is relative?
					float relativeWidth = Float.parseFloat(parts[6]);
					float relativeHeight = Float.parseFloat(parts[7]);
					image = new ImageInformation(key2, samplerName, TextureType.TEXTURE_2D, format, internalFormat, pixelType, 0, 0, 0, clear, true, relativeWidth, relativeHeight);
				} else {
					TextureType type;
					int width, height, depth;
					if (parts.length == 7) {
						type = TextureType.TEXTURE_1D;
						width = Integer.parseInt(parts[6]);
						height = 0;
						depth = 0;
					} else if (parts.length == 8) {
						type = TextureType.TEXTURE_2D;
						width = Integer.parseInt(parts[6]);
						height = Integer.parseInt(parts[7]);
						depth = 0;
					} else if (parts.length == 9) {
						type = TextureType.TEXTURE_3D;
						width = Integer.parseInt(parts[6]);
						height = Integer.parseInt(parts[7]);
						depth = Integer.parseInt(parts[8]);
					} else {
						Iris.logger.error("Unknown image type! " + key2 + " = " + value);
						return;
					}
					image = new ImageInformation(key2, samplerName, type, format, internalFormat, pixelType, width, height, depth, clear, false, 0, 0);
				}

				irisCustomImages.add(image);
			});

			handleTwoArgDirective("flip.", key, value, (pass, buffer) -> {
				handleBooleanValue(key, value, shouldFlip -> {
					explicitFlips.computeIfAbsent(pass, _pass -> new Object2BooleanOpenHashMap<>())
							.put(buffer, shouldFlip);
				});
			});

			handlePassDirective("variable.", key, value, pass -> {
				String[] parts = pass.split("\\.");
				if(parts.length != 2){
					Iris.logger.warn("Custom variables should take the form of `variable.<type>.<name> = <expression>. Ignoring " + key);
					return;
				}

				customUniforms.addVariable(parts[0], parts[1], value, false);
			});

			handlePassDirective("uniform.", key, value, pass -> {
				String[] parts = pass.split("\\.");
				if(parts.length != 2){
					Iris.logger.warn("Custom uniforms should take the form of `uniform.<type>.<name> = <expression>. Ignoring " + key);
					return;
				}

				customUniforms.addVariable(parts[0], parts[1], value, true);
			});


			handleWhitespacedListDirective(key, value, "iris.features.required", options -> requiredFeatureFlags = options);
			handleWhitespacedListDirective(key, value, "iris.features.optional", options -> optionalFeatureFlags = options);

			// TODO: Buffer size directives
			// TODO: Conditional program enabling directives
		});

		// We need to use a non-preprocessed property file here since we don't want any weird preprocessor changes to be applied to the screen/value layout.
		original.forEach((keyObject, valueObject) -> {
			String key = (String) keyObject;
			String value = (String) valueObject;

			// Defining "sliders" multiple times in the properties file will only result in
			// the last definition being used, should be tested if behavior matches OptiFine
			handleWhitespacedListDirective(key, value, "sliders", sliders -> sliderOptions = sliders);
			handlePrefixedWhitespacedListDirective("profile.", key, value, profiles::put);

			if (handleIntDirective(key, value, "screen.columns", columns -> mainScreenColumnCount = columns)) {
				return;
			}

			if (handleAffixedIntDirective("screen.", ".columns", key, value, subScreenColumnCount::put)) {
				return;
			}

			handleWhitespacedListDirective(key, value, "screen", options -> mainScreenOptions = options);
			handlePrefixedWhitespacedListDirective("screen.", key, value, subScreenOptions::put);
		});
	}

	private static void handleBooleanValue(String key, String value, BooleanConsumer handler) {
		if ("true".equals(value)) {
			handler.accept(true);
		} else if ("false".equals(value)) {
			handler.accept(false);
		} else {
			Iris.logger.warn("Unexpected value for boolean key " + key + " in shaders.properties: got " + value + ", but expected either true or false");
		}
	}

	private static void handleBooleanDirective(String key, String value, String expectedKey, Consumer<OptionalBoolean> handler) {
		if (!expectedKey.equals(key)) {
			return;
		}

		if ("true".equals(value)) {
			handler.accept(OptionalBoolean.TRUE);
		} else if ("false".equals(value)) {
			handler.accept(OptionalBoolean.FALSE);
		} else {
			Iris.logger.warn("Unexpected value for boolean key " + key + " in shaders.properties: got " + value + ", but expected either true or false");
		}
	}

	private static boolean handleIntDirective(String key, String value, String expectedKey, Consumer<Integer> handler) {
		if (!expectedKey.equals(key)) {
			return false;
		}

		try {
			int result = Integer.parseInt(value);

			handler.accept(result);
		} catch (NumberFormatException nex) {
			Iris.logger.warn("Unexpected value for integer key " + key + " in shaders.properties: got " + value + ", but expected an integer");
		}

		return true;
	}

	private static boolean handleAffixedIntDirective(String prefix, String suffix, String key, String value, BiConsumer<String, Integer> handler) {
		if (key.startsWith(prefix) && key.endsWith(suffix)) {
			int substrBegin = prefix.length();
			int substrEnd = key.length() - suffix.length();

			if (substrEnd <= substrBegin) {
				return false;
			}

			String affixStrippedKey = key.substring(substrBegin, substrEnd);

			try {
				int result = Integer.parseInt(value);

				handler.accept(affixStrippedKey, result);
			} catch (NumberFormatException nex) {
				Iris.logger.warn("Unexpected value for integer key " + key + " in shaders.properties: got " + value + ", but expected an integer");
			}

			return true;
		}

		return false;
	}

	private static void handlePassDirective(String prefix, String key, String value, Consumer<String> handler) {
		if (key.startsWith(prefix)) {
			String pass = key.substring(prefix.length());

			handler.accept(pass);
		}
	}

	private static void handleProgramEnabledDirective(String prefix, String key, String value, Consumer<String> handler) {
		if (key.startsWith(prefix)) {
			String program = key.substring(prefix.length(), key.indexOf(".", prefix.length()));

			handler.accept(program);
		}
	}

	private static void handleWhitespacedListDirective(String key, String value, String expectedKey, Consumer<List<String>> handler) {
		if (!expectedKey.equals(key)) {
			return;
		}

		String[] elements = value.split(" +");

		handler.accept(Arrays.asList(elements));
	}

	private static void handlePrefixedWhitespacedListDirective(String prefix, String key, String value, BiConsumer<String, List<String>> handler) {
		if (key.startsWith(prefix)) {
			String prefixStrippedKey = key.substring(prefix.length());
			String[] elements = value.split(" +");

			handler.accept(prefixStrippedKey, Arrays.asList(elements));
		}
	}

	private static void handleTwoArgDirective(String prefix, String key, String value, BiConsumer<String, String> handler) {
		if (key.startsWith(prefix)) {
			int endOfPassIndex = key.indexOf(".", prefix.length());
			String stage = key.substring(prefix.length(), endOfPassIndex);
			String sampler = key.substring(endOfPassIndex + 1);

			handler.accept(stage, sampler);
		}
	}

	public static ShaderProperties empty() {
		return new ShaderProperties();
	}

	public CloudSetting getCloudSetting() {
		return cloudSetting;
	}

	public OptionalBoolean getOldHandLight() {
		return oldHandLight;
	}

	public OptionalBoolean getDynamicHandLight() {
		return dynamicHandLight;
	}

	public OptionalBoolean getOldLighting() {
		return oldLighting;
	}

	public OptionalBoolean getShadowTerrain() {
		return shadowTerrain;
	}

	public OptionalBoolean getShadowTranslucent() {
		return shadowTranslucent;
	}

	public OptionalBoolean getShadowEntities() {
		return shadowEntities;
	}

	public OptionalBoolean getShadowPlayer() {
		return shadowPlayer;
	}

	public OptionalBoolean getShadowBlockEntities() {
		return shadowBlockEntities;
	}

	public OptionalBoolean getUnderwaterOverlay() {
		return underwaterOverlay;
	}

	public OptionalBoolean getSun() {
		return sun;
	}

	public OptionalBoolean getMoon() {
		return moon;
	}

	public OptionalBoolean getVignette() {
		return vignette;
	}

	public OptionalBoolean getBackFaceSolid() {
		return backFaceSolid;
	}

	public OptionalBoolean getBackFaceCutout() {
		return backFaceCutout;
	}

	public OptionalBoolean getBackFaceCutoutMipped() {
		return backFaceCutoutMipped;
	}

	public OptionalBoolean getBackFaceTranslucent() {
		return backFaceTranslucent;
	}

	public OptionalBoolean getRainDepth() {
		return rainDepth;
	}

	public OptionalBoolean getBeaconBeamDepth() {
		return beaconBeamDepth;
	}

	public OptionalBoolean getSeparateAo() {
		return separateAo;
	}

	public OptionalBoolean getVoxelizeLightBlocks() {
		return voxelizeLightBlocks;
	}

	public OptionalBoolean getSeparateEntityDraws() {
		return separateEntityDraws;
	}

	public OptionalBoolean getFrustumCulling() {
		return frustumCulling;
	}

	public ShadowCullState getShadowCulling() {
		return shadowCulling;
	}

	public Object2ObjectMap<String, AlphaTest> getAlphaTestOverrides() {
		return alphaTestOverrides;
	}

	public OptionalBoolean getShadowEnabled() {
		return shadowEnabled;
	}

	public Optional<ParticleRenderingSettings> getParticleRenderingSettings() {
		// Before is implied if separateEntityDraws is true.
		if (separateEntityDraws == OptionalBoolean.TRUE) return Optional.of(ParticleRenderingSettings.MIXED);
		return particleRenderingSettings;
	}

	public OptionalBoolean getConcurrentCompute() {
		return concurrentCompute;
	}

	public OptionalBoolean getPrepareBeforeShadow() {
		return prepareBeforeShadow;
	}

	public Object2FloatMap<String> getViewportScaleOverrides() {
		return viewportScaleOverrides;
	}

	public Object2ObjectMap<String, TextureScaleOverride> getTextureScaleOverrides() {
		return textureScaleOverrides;
	}

	public Object2ObjectMap<String, BlendModeOverride> getBlendModeOverrides() {
		return blendModeOverrides;
	}

	public Object2ObjectMap<String, ArrayList<BufferBlendInformation>> getBufferBlendOverrides() {
		return bufferBlendOverrides;
	}

	public Int2ObjectArrayMap<ShaderStorageInfo> getBufferObjects() {
		return bufferObjects;
	}

	public EnumMap<TextureStage, Object2ObjectMap<String, TextureDefinition>> getCustomTextures() {
		return customTextures;
	}

	public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getCustomTexturePatching() {

		return customTexturePatching;
	}

	public Object2ObjectMap<String, TextureDefinition> getIrisCustomTextures() {
		return irisCustomTextures;
	}

	public List<ImageInformation> getIrisCustomImages() {
		return irisCustomImages;
	}

	public Optional<String> getNoiseTexturePath() {
		return Optional.ofNullable(noiseTexturePath);
	}

	public Object2ObjectMap<String, String> getConditionallyEnabledPrograms() {
		return conditionallyEnabledPrograms;
	}

	public List<String> getSliderOptions() {
		return sliderOptions;
	}

	public Map<String, List<String>> getProfiles() {
		return profiles;
	}

	public Optional<List<String>> getMainScreenOptions() {
		return Optional.ofNullable(mainScreenOptions);
	}

	public Map<String, List<String>> getSubScreenOptions() {
		return subScreenOptions;
	}

	public Optional<Integer> getMainScreenColumnCount() {
		return Optional.ofNullable(mainScreenColumnCount);
	}

	public Map<String, Integer> getSubScreenColumnCount() {
		return subScreenColumnCount;
	}

	public Object2ObjectMap<String, Object2BooleanMap<String>> getExplicitFlips() {
		return explicitFlips;
	}

	public List<String> getRequiredFeatureFlags() {
		return requiredFeatureFlags;
	}

	public List<String> getOptionalFeatureFlags() {
		return optionalFeatureFlags;
	}

	public OptionalBoolean supportsColorCorrection() {
		return supportsColorCorrection;
	}
}
