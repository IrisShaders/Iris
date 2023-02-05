package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.render.vertex.serializers.MemoryTransfer;
import me.jellysquid.mods.sodium.client.render.vertex.serializers.VertexSerializerCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = VertexSerializerCache.class, remap = false)
public abstract class MixinVertexSerializerCache {
	@Shadow
	private static List<MemoryTransfer> mergeAdjacentMemoryTransfers(ArrayList<MemoryTransfer> src) {
		return null;
	}

	/**
	 * @author IMS
	 * @reason We need to allow "invalid" combinations.
	 */
	// TODO: This is not good. We need this because the "block" vertex format contains elements not in the "entity" format, and that trips up rendering the crumbling texture on beds.
	@Overwrite
	private static List<MemoryTransfer> createMemoryTransferList(VertexFormatDescription srcVertexFormat, VertexFormatDescription dstVertexFormat) {
		var ops = new ArrayList<MemoryTransfer>();

		var srcElements = srcVertexFormat.getElements();
		var srcOffsets = srcVertexFormat.getOffsets();

		var dstElements = dstVertexFormat.getElements();
		var dstOffsets = dstVertexFormat.getOffsets();

		for (int dstIndex = 0; dstIndex < dstElements.size(); dstIndex++) {
			var dstElement = dstElements.get(dstIndex);
			var srcIndex = srcElements.indexOf(dstElement);

			if (srcIndex == -1) {
				continue;
			}

			var srcOffset = srcOffsets.getInt(srcIndex);
			var dstOffset = dstOffsets.getInt(dstIndex);

			ops.add(new MemoryTransfer(srcOffset, dstOffset, dstElement.getByteSize()));
		}

		return mergeAdjacentMemoryTransfers(ops);
	}
}
