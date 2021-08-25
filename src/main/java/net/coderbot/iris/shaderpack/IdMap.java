package net.coderbot.iris.shaderpack;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.coderbot.iris.Iris;
import net.minecraft.block.Blocks;
import net.minecraft.state.StateManager;
import net.minecraft.tag.BlockTags;
import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

/**
 * A utility class for parsing entries in item.properties, block.properties, and entities.properties files in shaderpacks
 */
public class IdMap {
	/**
	 * Maps a given item ID to an integer ID
	 */
	private final Object2IntMap<Identifier> itemIdMap;

	/**
	 * Maps a given entity ID to an integer ID
	 */
	private final Object2IntMap<Identifier> entityIdMap;

	/**
	 * Maps block states to block ids defined in block.properties
	 */
	private Object2IntMap<BlockState> blockPropertiesMap;

	/**
	 * A map that contains render layers for blocks in block.properties
	 */
	private Map<Identifier, RenderLayer> blockRenderLayerMap;

	IdMap(Path shaderPath) {
		itemIdMap = loadProperties(shaderPath, "item.properties")
			.map(IdMap::parseItemIdMap).orElse(Object2IntMaps.emptyMap());

		entityIdMap = loadProperties(shaderPath, "entity.properties")
			.map(IdMap::parseEntityIdMap).orElse(Object2IntMaps.emptyMap());

		loadProperties(shaderPath, "block.properties").ifPresent(blockProperties -> {
			// TODO: This won't parse block states in block.properties properly
			blockPropertiesMap = parseBlockMap(blockProperties, "block.", "block.properties");
			blockRenderLayerMap = parseRenderLayerMap(blockProperties, "layer.", "block.properties");
		});

		// TODO: Properly override block render layers

		if (blockPropertiesMap == null) {
			// Fill in with default values...
			blockPropertiesMap = new Object2IntOpenHashMap<>();
			LegacyIdMap.addLegacyValues(blockPropertiesMap);
		}

		if (blockRenderLayerMap == null) {
			blockRenderLayerMap = Collections.emptyMap();
		}
	}

	/**
	 * Loads properties from a properties file in a shaderpack path
	 */
	private static Optional<Properties> loadProperties(Path shaderPath, String name) {
		String fileContents = readProperties(shaderPath, name);
		if (fileContents == null) {
			return Optional.empty();
		}

		String processed = PropertiesPreprocessor.process(name, fileContents);

		StringReader propertiesReader = new StringReader(processed);
		Properties properties = new Properties();
		try {
			properties.load(propertiesReader);
		} catch (IOException e) {
			Iris.logger.error("Error loading " + name + " at " + shaderPath);
			Iris.logger.catching(Level.ERROR, e);

			return Optional.empty();
		}

		return Optional.of(properties);
	}

	private static String readProperties(Path shaderPath, String name) {
		try {
			return new String(Files.readAllBytes(shaderPath.resolve(name)), StandardCharsets.UTF_8);
		} catch (NoSuchFileException e) {
			Iris.logger.debug("An " + name + " file was not found in the current shaderpack");

			return null;
		} catch (IOException e) {
			Iris.logger.error("An IOException occurred reading " + name + " from the current shaderpack");
			Iris.logger.catching(Level.ERROR, e);

			return null;
		}
	}

	private static Object2IntMap<Identifier> parseItemIdMap(Properties properties) {
		return parseIdMap(properties, "item.", "item.properties");
	}

	private static Object2IntMap<Identifier> parseEntityIdMap(Properties properties) {
		return parseIdMap(properties, "entity.", "entity.properties");
	}

	/**
	 * Parses an identifier map in OptiFine format
	 */
	private static Object2IntMap<Identifier> parseIdMap(Properties properties, String keyPrefix, String fileName) {
		Object2IntMap<Identifier> idMap = new Object2IntOpenHashMap<>();

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

			for (String part : value.split(" ")) {
				if (part.contains("=")) {
					// Avoid tons of logspam for now
					Iris.logger.warn("Failed to parse an identifier in " + fileName + " for the key " + key + ": state properties are currently not supported: " + part);
					continue;
				}

				try {
					Identifier identifier = new Identifier(part);

					idMap.put(identifier, intId);
				} catch (InvalidIdentifierException e) {
					Iris.logger.warn("Failed to parse an identifier in " + fileName + " for the key " + key + ":");
					Iris.logger.catching(Level.WARN, e);
				}
			}
		});

		return Object2IntMaps.unmodifiable(idMap);
	}

	private static Object2IntMap<BlockState> parseBlockMap(Properties properties, String keyPrefix, String fileName) {
		Object2IntMap<BlockState> idMap = new Object2IntOpenHashMap<>();

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

			for (String part : value.split(" ")) {
				try {
					addBlockStates(part, idMap, intId);
				} catch (InvalidIdentifierException e) {
					Iris.logger.warn("Failed to parse an identifier in " + fileName + " for the key " + key + ":");
					Iris.logger.catching(Level.WARN, e);
				}
			}
		});

		return Object2IntMaps.unmodifiable(idMap);
	}

	private static void addBlockStates(String entry, Object2IntMap<BlockState> idMap, int intId) throws InvalidIdentifierException {
		String[] splitStates = entry.split(":");

		if (splitStates.length == 0) {
			// An empty string?
			return;
		}

		// Simple cases: no states involved
		//
		// The first term MUST be a valid identifier component without an equals sign
		// The second term, if it does not contain an equals sign, must be a valid identifier component.
		if (splitStates.length == 1 || splitStates.length == 2 && !splitStates[1].contains("=")) {
			// We parse this as a normal identifier here.
			Identifier identifier = new Identifier(entry);

			Block block = Registry.BLOCK.get(identifier);

			// If the block doesn't exist, by default the registry will return AIR. That probably isn't what we want.
			// TODO: Assuming that Registry.BLOCK.getDefaultId() == "minecraft:air" here
			if (block == Blocks.AIR) {
				return;
			}

			for (BlockState state : block.getStateManager().getStates()) {
				idMap.put(state, intId);
			}

			return;
		}

		// Complex case: One or more states involved...
		int statesStart;
		Identifier identifier;

		if (splitStates[1].contains("=")) {
			// We have an entry of the form "tall_grass:half=upper"
			statesStart = 1;
			identifier = new Identifier(splitStates[0]);
		} else {
			// We have an entry of the form "minecraft:tall_grass:half=upper"
			statesStart = 2;
			identifier = new Identifier(splitStates[0], splitStates[1]);
		}

		// Let's look up the block and make sure that it exists.
		Block block = Registry.BLOCK.get(identifier);

		// If the block doesn't exist, by default the registry will return AIR. That probably isn't what we want.
		// TODO: Assuming that Registry.BLOCK.getDefaultId() == "minecraft:air" here
		if (block == Blocks.AIR && !entry.contains("air")) {
			Iris.logger.warn("Failed to parse the block ID map entry \"" + entry + "\":");
			Iris.logger.warn("- There is no block with the name " + identifier + "!");

			return;
		}

		// Once we've determined that the block exists, we must parse each property key=value pair from the state entry.
		//
		// These pairs act as a filter on the block states. Thus, the shaderpack does not have to specify all of the
		// individual block properties itself; rather, it only specifies the parts of the block state that it wishes
		// to filter in/out.
		//
		// For example, some shaderpacks may make it so that hanging lantern blocks wave. They will put something of
		// the form "lantern:hanging=false" in the ID map as a result. Note, however, that there are also waterlogged
		// hanging lanterns, which would have "lantern:hanging=false:waterlogged=true". We must make sure that when the
		// shaderpack author writes "lantern:hanging=false", that we do not just match that individual state, but that
		// we also match the waterlogged variant too.
		//
		// As a result, we first parse each key=value pair in order to determine what properties we need to filter on.
		Map<Property<?>, String> properties = new HashMap<>();
		StateManager<Block, BlockState> stateManager = block.getStateManager();

		for (int index = statesStart; index < splitStates.length; index++) {
			// Split "key=value" into the key and value
			String[] propertyParts = splitStates[index].split("=");

			if (propertyParts.length != 2) {
				Iris.logger.warn("Failed to parse the block ID map entry \"" + entry + "\":");
				Iris.logger.warn("- Block state property filters must be of the form \"key=value\", but " + splitStates[index] + " is not of that form!");

				// TODO: Should we just "continue" here and ignore the invalid property entry?
				return;
			}

			String key = propertyParts[0];
			String value = propertyParts[1];

			Property<?> property = stateManager.getProperty(key);

			if (property == null) {
				Iris.logger.warn("Error while parsing the block ID map entry \"" + entry + "\":");
				Iris.logger.warn("- The block " + identifier + " has no property with the name " + key + ", ignoring!");

				continue;
			}

			properties.put(property, value);
		}

		// Once we have a list of properties and their expected values, we iterate over every possible state of this
		// block and check for ones that match the filters. This isn't particularly efficient, but it works!
		for (BlockState state : stateManager.getStates()) {
			boolean matches = true;

			for (Map.Entry<Property<?>, String> condition : properties.entrySet()) {
				// TODO: Do something about these raw types...
				Property property = condition.getKey();
				String expectedValue = condition.getValue();

				String actualValue = property.name((Comparable) state.get(property));

				if (!expectedValue.equals(actualValue)) {
					matches = false;
					break;
				}
			}

			if (matches) {
				idMap.put(state, intId);
			}
		}
	}

	/**
	 * Parses a render layer map
	 */
	private static Map<Identifier, RenderLayer> parseRenderLayerMap(Properties properties, String keyPrefix, String fileName) {
		// TODO: Most of this is copied from parseIdMap, it would be nice to reduce duplication.
		Map<Identifier, RenderLayer> layerMap = new HashMap<>();

		properties.forEach((keyObject, valueObject) -> {
			String key = (String) keyObject;
			String value = (String) valueObject;

			if (!key.startsWith(keyPrefix)) {
				// Not a valid line, ignore it
				return;
			}

			RenderLayer layer;

			// See: https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt#L556-L576
			switch (key) {
				case "solid":
					layer = RenderLayer.getSolid();
					break;
				case "cutout":
					layer = RenderLayer.getCutout();
					break;
				case "cutout_mipped":
					layer = RenderLayer.getCutoutMipped();
					break;
				case "translucent":
					layer = RenderLayer.getTranslucent();
					break;
				default:
					Iris.logger.warn("Failed to parse line in " + fileName + ": invalid render layer type: " + key);
					return;
			}

			for (String part : value.split(" ")) {
				try {
					Identifier identifier = new Identifier(part);

					layerMap.put(identifier, layer);
				} catch (InvalidIdentifierException e) {
					Iris.logger.warn("Failed to parse an identifier in " + fileName + " for the key " + key + ":");
					Iris.logger.catching(Level.WARN, e);
				}
			}
		});

		return layerMap;
	}

	public Map<BlockState, Integer> getBlockProperties() {
		return blockPropertiesMap;
	}

	public Map<Identifier, Integer> getItemIdMap() {
		return itemIdMap;
	}

	public Map<Identifier, Integer> getEntityIdMap() {
		return entityIdMap;
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
				&& Objects.equals(blockRenderLayerMap, idMap.blockRenderLayerMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemIdMap, entityIdMap, blockPropertiesMap, blockRenderLayerMap);
	}
}
