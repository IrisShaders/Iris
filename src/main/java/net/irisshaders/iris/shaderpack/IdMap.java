package net.irisshaders.iris.shaderpack;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.irisshaders.iris.shaderpack.materialmap.BlockEntry;
import net.irisshaders.iris.shaderpack.materialmap.BlockRenderType;
import net.irisshaders.iris.shaderpack.materialmap.Entry;
import net.irisshaders.iris.shaderpack.materialmap.LegacyIdMap;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.TagEntry;
import net.irisshaders.iris.shaderpack.option.OrderBackedProperties;
import net.irisshaders.iris.shaderpack.option.ShaderPackOptions;
import net.irisshaders.iris.shaderpack.preprocessor.PropertiesPreprocessor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * A utility class for parsing entries in item.properties, block.properties, and entities.properties files in shaderpacks
 */
public class IdMap {
	/**
	 * Maps a given item ID to an integer ID
	 */
	private final Object2IntMap<NamespacedId> itemIdMap;

	/**
	 * Maps a given entity ID to an integer ID
	 */
	private final Object2IntMap<NamespacedId> entityIdMap;

	/**
	 * Maps block states to block ids defined in block.properties
	 */
	private Int2ObjectLinkedOpenHashMap<List<BlockEntry>> blockPropertiesMap;

	/**
	 * Maps tags to block ids defined in block.properties
	 */
	private Int2ObjectLinkedOpenHashMap<List<TagEntry>> blockTagMap;

	/**
	 * A set of render type overrides for specific blocks. Allows shader packs to move blocks to different render types.
	 */
	private Map<NamespacedId, BlockRenderType> blockRenderTypeMap;

	IdMap(Path shaderPath, ShaderPackOptions shaderPackOptions, Iterable<StringPair> environmentDefines) {
		itemIdMap = loadProperties(shaderPath, "item.properties", shaderPackOptions, environmentDefines)
			.map(IdMap::parseItemIdMap).orElse(Object2IntMaps.emptyMap());

		entityIdMap = loadProperties(shaderPath, "entity.properties", shaderPackOptions, environmentDefines)
			.map(IdMap::parseEntityIdMap).orElse(Object2IntMaps.emptyMap());
		blockTagMap = new Int2ObjectLinkedOpenHashMap<>();

		loadProperties(shaderPath, "block.properties", shaderPackOptions, environmentDefines).ifPresent(blockProperties -> {
			blockPropertiesMap = parseBlockMap(blockProperties, "block.", "block.properties", blockTagMap);
			blockRenderTypeMap = parseRenderTypeMap(blockProperties, "layer.", "block.properties");
		});

		// TODO: Properly override block render layers

		if (blockPropertiesMap == null) {
			// Fill in with default values...
			blockPropertiesMap = new Int2ObjectLinkedOpenHashMap<>();
			LegacyIdMap.addLegacyValues(blockPropertiesMap);
		}

		if (blockRenderTypeMap == null) {
			blockRenderTypeMap = Collections.emptyMap();
		}
	}

	/**
	 * Loads properties from a properties file in a shaderpack path
	 */
	private static Optional<Properties> loadProperties(Path shaderPath, String name, ShaderPackOptions shaderPackOptions,
													   Iterable<StringPair> environmentDefines) {
		String fileContents = readProperties(shaderPath, name);
		if (fileContents == null) {
			return Optional.empty();
		}

		// TODO: This is the worst code I have ever made. Do not do this.
		String processed = PropertiesPreprocessor.preprocessSource(fileContents, shaderPackOptions, environmentDefines).replaceAll("\\\\\\n\\s*\\n", " ").replaceAll("\\S\s*block\\.", "\nblock.");
		StringReader propertiesReader = new StringReader(processed);
		warnMissingBackslashInPropertiesFile(processed, name);

		// Note: ordering of properties is significant
		// See https://github.com/IrisShaders/Iris/issues/1327 and the relevant putIfAbsent calls in
		// BlockMaterialMapping
		Properties properties = new OrderBackedProperties();
		try {
			properties.load(propertiesReader);
		} catch (IOException e) {
			Iris.logger.error("Error loading " + name + " at " + shaderPath, e);

			return Optional.empty();
		}

		if (Iris.getIrisConfig().areDebugOptionsEnabled()) {
			ShaderPrinter.deleteIfClearing();
			try (OutputStream os = Files.newOutputStream(FabricLoader.getInstance().getGameDir().resolve("patched_shaders").resolve(name))) {
				properties.store(new OutputStreamWriter(os, StandardCharsets.UTF_8), "Patched version of properties");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return Optional.of(properties);
	}

	private static String readProperties(Path shaderPath, String name) {
		try {
			// ID maps should be encoded in ISO_8859_1.
			return Files.readString(shaderPath.resolve(name), StandardCharsets.ISO_8859_1);
		} catch (NoSuchFileException e) {
			Iris.logger.debug("An " + name + " file was not found in the current shaderpack");

			return null;
		} catch (IOException e) {
			Iris.logger.error("An IOException occurred reading " + name + " from the current shaderpack", e);

			return null;
		}
	}

	private static Object2IntMap<NamespacedId> parseItemIdMap(Properties properties) {
		return parseIdMap(properties, "item.", "item.properties");
	}

	private static Object2IntMap<NamespacedId> parseEntityIdMap(Properties properties) {
		return parseIdMap(properties, "entity.", "entity.properties");
	}

	/**
	 * Parses a NamespacedId map in OptiFine format
	 */
	private static Object2IntMap<NamespacedId> parseIdMap(Properties properties, String keyPrefix, String fileName) {
		Object2IntMap<NamespacedId> idMap = new Object2IntOpenHashMap<>();
		idMap.defaultReturnValue(-1);

		properties.forEach((keyObject, valueObject) -> {
			String key = (String) keyObject;
			String value = (String) valueObject;

			if (!key.startsWith(keyPrefix)) {
				// Not a valid line, ignore it
				return;
			}

			int intId;

			try {
				intId = Integer.parseInt(key.substring(keyPrefix.length()));
			} catch (NumberFormatException e) {
				// Not a valid property line
				Iris.logger.warn("Failed to parse line in " + fileName + ": invalid key " + key);
				return;
			}

			// Split on any whitespace
			for (String part : value.split("\\s+")) {
				if (part.contains("=")) {
					// Avoid tons of logspam for now
					Iris.logger.warn("Failed to parse an ResourceLocation in " + fileName + " for the key " + key + ": state properties are currently not supported: " + part);
					continue;
				}

				// Note: NamespacedId performs no validation on the content. That will need to be done by whatever is
				//       converting these things to ResourceLocations.
				idMap.put(new NamespacedId(part), intId);
			}
		});

		return Object2IntMaps.unmodifiable(idMap);
	}

	private static Int2ObjectLinkedOpenHashMap<List<BlockEntry>> parseBlockMap(Properties properties, String keyPrefix, String fileName, Int2ObjectLinkedOpenHashMap<List<TagEntry>> blockTagMap) {
		Int2ObjectLinkedOpenHashMap<List<BlockEntry>> blockEntriesById = new Int2ObjectLinkedOpenHashMap<>();
		Int2ObjectLinkedOpenHashMap<List<TagEntry>> tagEntriesById = new Int2ObjectLinkedOpenHashMap<>();

		properties.forEach((keyObject, valueObject) -> {
			String key = (String) keyObject;
			String value = (String) valueObject;

			if (!key.startsWith(keyPrefix)) {
				// Not a valid line, ignore it
				return;
			}

			int intId;

			try {
				intId = Integer.parseInt(key.substring(keyPrefix.length()));
			} catch (NumberFormatException e) {
				// Not a valid property line
				Iris.logger.warn("Failed to parse line in " + fileName + ": invalid key " + key);
				return;
			}

			List<BlockEntry> blockEntries = new ArrayList<>();
			List<TagEntry> tagEntries = new ArrayList<>();

			// Split on whitespace groups, not just single spaces
			for (String part : value.split("\\s+")) {
				if (part.isEmpty()) {
					continue;
				}

				try {
					Entry entry = BlockEntry.parse(part);
					if (entry instanceof BlockEntry be) {
						blockEntries.add(be);
					} else if (entry instanceof TagEntry te) {
						tagEntries.add(te);
					}
				} catch (Exception e) {
					Iris.logger.warn("Unexpected error while parsing an entry from " + fileName + " for the key " + key + ":", e);
				}
			}

			blockEntriesById.put(intId, Collections.unmodifiableList(blockEntries));
			tagEntriesById.put(intId, Collections.unmodifiableList(tagEntries));
		});

		blockTagMap.putAll(tagEntriesById);

		return blockEntriesById;
	}

	/**
	 * Parses a render layer map.
	 * <p>
	 */
	private static Map<NamespacedId, BlockRenderType> parseRenderTypeMap(Properties properties, String keyPrefix, String fileName) {
		Map<NamespacedId, BlockRenderType> overrides = new HashMap<>();

		properties.forEach((keyObject, valueObject) -> {
			String key = (String) keyObject;
			String value = (String) valueObject;

			if (!key.startsWith(keyPrefix)) {
				// Not a valid line, ignore it
				return;
			}

			// Note: We have to remove the prefix "layer." because fromString expects "cutout", not "layer.cutout".
			String keyWithoutPrefix = key.substring(keyPrefix.length());

			BlockRenderType renderType = BlockRenderType.fromString(keyWithoutPrefix).orElse(null);

			if (renderType == null) {
				Iris.logger.warn("Failed to parse line in " + fileName + ": invalid block render type: " + key);
				return;
			}

			for (String part : value.split("\\s+")) {
				if (part.startsWith("%")) {
					Iris.logger.fatal("Cannot use a tag in the render type map: " + key + " = " + value);
					continue;
				}
				// Note: NamespacedId performs no validation on the content. That will need to be done by whatever is
				//       converting these things to ResourceLocations.
				overrides.put(new NamespacedId(part), renderType);
			}
		});

		return overrides;
	}


	private static Map<NamespacedId, String> parseDimensionMap(Properties properties, String keyPrefix, String fileName) {
		Map<NamespacedId, String> overrides = new Object2ObjectArrayMap<>();

		properties.forEach((keyObject, valueObject) -> {
			String key = (String) keyObject;
			String value = (String) valueObject;

			if (!key.startsWith(keyPrefix)) {
				// Not a valid line, ignore it
				return;
			}

			key = key.substring(keyPrefix.length());

			for (String part : value.split("\\s+")) {
				overrides.put(new NamespacedId(part), key);
			}
		});

		return overrides;
	}

	private static void warnMissingBackslashInPropertiesFile(String processedSource, String propertiesFileName) {
		if (propertiesFileName.equals("shaders.properties")) {
			return;
		}
		String[] fileNameSections = propertiesFileName.split("\\.");
		String entryName = "entry";
		if (fileNameSections.length >= 2) {
			entryName = fileNameSections[0] + " entry";
		}
		Matcher matcher = PropertiesPreprocessor.BACKSLASH_MATCHER.matcher(processedSource);
		while (matcher.find()) {
			Iris.logger.warn("Found missing \"\\\" in file \"{}\" in {}: \"{}\"", propertiesFileName, entryName, matcher.group(0));
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String match = matcher.group(i);
				if (match == null) {
					continue;
				}
				Iris.logger.warn("At ID: \"{}\"", match);
			}
		}
	}

	public Int2ObjectLinkedOpenHashMap<List<BlockEntry>> getBlockProperties() {
		return blockPropertiesMap;
	}

	public Int2ObjectLinkedOpenHashMap<List<TagEntry>> getTagEntries() {
		return blockTagMap;
	}

	public Object2IntFunction<NamespacedId> getItemIdMap() {
		return itemIdMap;
	}

	public Object2IntFunction<NamespacedId> getEntityIdMap() {
		return entityIdMap;
	}

	public Map<NamespacedId, BlockRenderType> getBlockRenderTypeMap() {
		return blockRenderTypeMap;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		IdMap idMap = (IdMap) o;

		return Objects.equals(itemIdMap, idMap.itemIdMap)
			&& Objects.equals(entityIdMap, idMap.entityIdMap)
			&& Objects.equals(blockPropertiesMap, idMap.blockPropertiesMap)
			&& Objects.equals(blockTagMap, idMap.blockTagMap)
			&& Objects.equals(blockRenderTypeMap, idMap.blockRenderTypeMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemIdMap, entityIdMap, blockPropertiesMap, blockTagMap, blockRenderTypeMap);
	}
}
