package net.irisshaders.iris.shaderpack.materialmap;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.irisshaders.iris.Iris;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlockMaterialMapping {
	public static Object2IntMap<BlockState> createBlockStateIdMap(Int2ObjectLinkedOpenHashMap<List<BlockEntry>> blockPropertiesMap, Int2ObjectLinkedOpenHashMap<List<TagEntry>> tagPropertiesMap) {
		Object2IntMap<BlockState> blockStateIds = new Object2IntLinkedOpenHashMap<>();

		blockPropertiesMap.forEach((intId, entries) -> {
			for (BlockEntry entry : entries) {
				addBlockStates(entry, blockStateIds, intId);
			}
		});

		tagPropertiesMap.forEach((intId, entries) -> {
			for (TagEntry entry : entries) {
				addTag(entry, blockStateIds, intId);
			}
		});

		return blockStateIds;
	}

	private static void addTag(TagEntry tagEntry, Object2IntMap<BlockState> idMap, int intId) {
		List<TagKey<Block>> compatibleTags = BuiltInRegistries.BLOCK.getTagNames().filter(t -> t.location().getNamespace().equalsIgnoreCase(tagEntry.id().getNamespace()) &&
			t.location().getPath().equalsIgnoreCase(tagEntry.id().getName())).toList();

		if (compatibleTags.isEmpty()) {
			Iris.logger.warn("Failed to find the tag " + tagEntry.id());
		} else if (compatibleTags.size() > 1) {
			Iris.logger.fatal("You've broke the system; congrats. More than one tag matched " + tagEntry.id());
		} else {
			BuiltInRegistries.BLOCK.getTag(compatibleTags.get(0)).get().forEach((block) -> {
					Map<String, String> propertyPredicates = tagEntry.propertyPredicates();

					if (propertyPredicates.isEmpty()) {
						// Just add all the states if there aren't any predicates
						for (BlockState state : block.value().getStateDefinition().getPossibleStates()) {
							// NB: Using putIfAbsent means that the first successful mapping takes precedence
							//     Needed for OptiFine parity:
							//     https://github.com/IrisShaders/Iris/issues/1327
							idMap.putIfAbsent(state, intId);
						}

						return;
					}

					// As a result, we first collect each key=value pair in order to determine what properties we need to filter on.
					// We already get this from BlockEntry, but we convert the keys to `Property`s to ensure they exist and to avoid
					// string comparisons later.
					Map<Property<?>, String> properties = new LinkedHashMap<>();
					StateDefinition<Block, BlockState> stateManager = block.value().getStateDefinition();

					propertyPredicates.forEach((key, value) -> {
						Property<?> property = stateManager.getProperty(key);

						if (property == null) {
							Iris.logger.warn("Error while parsing the block ID map entry for tag \"" + "block." + intId + "\":");
							Iris.logger.warn("- The block " + block.unwrapKey().get().location() + " has no property with the name " + key + ", ignoring!");

							return;
						}

						properties.put(property, value);
					});

					// Once we have a list of properties and their expected values, we iterate over every possible state of this
					// block and check for ones that match the filters. This isn't particularly efficient, but it works!
					for (BlockState state : stateManager.getPossibleStates()) {
						if (checkState(state, properties)) {
							// NB: Using putIfAbsent means that the first successful mapping takes precedence
							//     Needed for OptiFine parity:
							//     https://github.com/IrisShaders/Iris/issues/1327
							idMap.putIfAbsent(state, intId);
						}
					}
			}
			);
		}
	}

	public static Map<Block, RenderType> createBlockTypeMap(Map<NamespacedId, BlockRenderType> blockPropertiesMap) {
		Map<Block, RenderType> blockTypeIds = new Reference2ReferenceLinkedOpenHashMap<>();

		blockPropertiesMap.forEach((id, blockType) -> {
			ResourceLocation resourceLocation = new ResourceLocation(id.getNamespace(), id.getName());

			Block block = BuiltInRegistries.BLOCK.get(resourceLocation);

			blockTypeIds.put(block, convertBlockToRenderType(blockType));
		});

		return blockTypeIds;
	}

	private static RenderType convertBlockToRenderType(BlockRenderType type) {
		if (type == null) {
			return null;
		}

		return switch (type) {
			case SOLID -> RenderType.solid();
			case CUTOUT -> RenderType.cutout();
			case CUTOUT_MIPPED -> RenderType.cutoutMipped();
			case TRANSLUCENT -> RenderType.translucent();
		};
	}

	private static void addBlockStates(BlockEntry entry, Object2IntMap<BlockState> idMap, int intId) {
		NamespacedId id = entry.id();
		ResourceLocation resourceLocation;
		try {
			resourceLocation = new ResourceLocation(id.getNamespace(), id.getName());
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to get entry for " + intId, exception);
		}

		Block block = BuiltInRegistries.BLOCK.get(resourceLocation);

		// If the block doesn't exist, by default the registry will return AIR. That probably isn't what we want.
		// TODO: Assuming that Registry.BLOCK.getDefaultId() == "minecraft:air" here
		if (block == Blocks.AIR) {
			return;
		}

		Map<String, String> propertyPredicates = entry.propertyPredicates();

		if (propertyPredicates.isEmpty()) {
			// Just add all the states if there aren't any predicates
			for (BlockState state : block.getStateDefinition().getPossibleStates()) {
				// NB: Using putIfAbsent means that the first successful mapping takes precedence
				//     Needed for OptiFine parity:
				//     https://github.com/IrisShaders/Iris/issues/1327
				idMap.putIfAbsent(state, intId);
			}

			return;
		}

		// As a result, we first collect each key=value pair in order to determine what properties we need to filter on.
		// We already get this from BlockEntry, but we convert the keys to `Property`s to ensure they exist and to avoid
		// string comparisons later.
		Map<Property<?>, String> properties = new LinkedHashMap<>();
		StateDefinition<Block, BlockState> stateManager = block.getStateDefinition();

		propertyPredicates.forEach((key, value) -> {
			Property<?> property = stateManager.getProperty(key);

			if (property == null) {
				Iris.logger.warn("Error while parsing the block ID map entry for \"" + "block." + intId + "\":");
				Iris.logger.warn("- The block " + resourceLocation + " has no property with the name " + key + ", ignoring!");

				return;
			}

			properties.put(property, value);
		});

		// Once we have a list of properties and their expected values, we iterate over every possible state of this
		// block and check for ones that match the filters. This isn't particularly efficient, but it works!
		for (BlockState state : stateManager.getPossibleStates()) {
			if (checkState(state, properties)) {
				// NB: Using putIfAbsent means that the first successful mapping takes precedence
				//     Needed for OptiFine parity:
				//     https://github.com/IrisShaders/Iris/issues/1327
				idMap.putIfAbsent(state, intId);
			}
		}
	}

	// We ignore generics here, the actual types don't matter because we just convert
	// them to strings anyways, and the compiler checks just get in the way.
	//
	// If you're able to rewrite this function without SuppressWarnings, feel free.
	// But otherwise it works fine.
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static boolean checkState(BlockState state, Map<Property<?>, String> expectedValues) {
		for (Map.Entry<Property<?>, String> condition : expectedValues.entrySet()) {
			Property property = condition.getKey();
			String expectedValue = condition.getValue();

			String actualValue = property.getName(state.getValue(property));

			if (!expectedValue.equals(actualValue)) {
				return false;
			}
		}

		return true;
	}
}
