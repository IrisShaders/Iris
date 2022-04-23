package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.materialmap.BlockEntry;
import net.coderbot.iris.shaderpack.materialmap.BlockRenderType;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;
import net.coderbot.iris.shaderpack.preprocessor.PropertiesPreprocessor;

import java.io.IOException;
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
	private Int2ObjectMap<List<BlockEntry>> blockPropertiesMap;

	/**
	 * A set of render type overrides for specific blocks. Allows shader packs to move blocks to different render types.
	 */
	private Map<NamespacedId, BlockRenderType> blockRenderTypeMap;

	IdMap(Path shaderPath, ShaderPackOptions shaderPackOptions) {
		itemIdMap = loadProperties(shaderPath, "item.properties", shaderPackOptions)
			.map(IdMap::parseItemIdMap).orElse(Object2IntMaps.emptyMap());

		entityIdMap = loadProperties(shaderPath, "entity.properties", shaderPackOptions)
			.map(IdMap::parseEntityIdMap).orElse(Object2IntMaps.emptyMap());

		loadProperties(shaderPath, "block.properties", shaderPackOptions).ifPresent(blockProperties -> {
			blockPropertiesMap = parseBlockMap(blockProperties, "block.", "block.properties");
			blockRenderTypeMap = parseRenderTypeMap(blockProperties, "layer.", "block.properties");
		});

		// TODO: Properly override block render layers

		if (blockPropertiesMap == null) {
			// Fill in with default values...
			blockPropertiesMap = new Int2ObjectOpenHashMap<>();
			LegacyIdMap.addLegacyValues(blockPropertiesMap);
		}

		if (blockRenderTypeMap == null) {
			blockRenderTypeMap = Collections.emptyMap();
		}
	}

	/**
	 * Loads properties from a properties file in a shaderpack path
	 */
	private static Optional<Properties> loadProperties(Path shaderPath, String name, ShaderPackOptions shaderPackOptions) {
		String fileContents = readProperties(shaderPath, name);
		if (fileContents == null) {
			return Optional.empty();
		}

		String processed = PropertiesPreprocessor.preprocessSource(fileContents, shaderPackOptions);

		StringReader propertiesReader = new StringReader(processed);

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

		return Optional.of(properties);
	}

	private static String readProperties(Path shaderPath, String name) {
		try {
			// ID maps should be encoded in ISO_8859_1.
			return new String(Files.readAllBytes(shaderPath.resolve(name)), StandardCharsets.ISO_8859_1);
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

	private static Int2ObjectMap<List<BlockEntry>> parseBlockMap(Properties properties, String keyPrefix, String fileName) {
		Int2ObjectMap<List<BlockEntry>> entriesById = new Int2ObjectOpenHashMap<>();

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

			List<BlockEntry> entries = new ArrayList<>();

			// Split on whitespace groups, not just single spaces
			for (String part : value.split("\\s+")) {
				if (part.isEmpty()) {
					continue;
				}

				try {
					entries.add(BlockEntry.parse(part));
				} catch (Exception e) {
					Iris.logger.warn("Unexpected error while parsing an entry from " + fileName + " for the key " + key + ":", e);
				}
			}

			entriesById.put(intId, Collections.unmodifiableList(entries));
		});

		return Int2ObjectMaps.unmodifiable(entriesById);
	}

	/**
	 * Parses a render layer map.
	 *
	 * This feature is used by Chocapic v9 and Wisdom Shaders. Otherwise, it is a rarely-used feature.
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
				// Note: NamespacedId performs no validation on the content. That will need to be done by whatever is
				//       converting these things to ResourceLocations.
				overrides.put(new NamespacedId(part), renderType);
			}
		});

		return overrides;
	}

	public Int2ObjectMap<List<BlockEntry>> getBlockProperties() {
		return blockPropertiesMap;
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
				&& Objects.equals(blockRenderTypeMap, idMap.blockRenderTypeMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemIdMap, entityIdMap, blockPropertiesMap, blockRenderTypeMap);
	}
}
