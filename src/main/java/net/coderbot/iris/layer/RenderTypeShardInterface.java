package net.coderbot.iris.layer;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public interface RenderTypeShardInterface {
	RenderType addTemporaryShard(RenderStateShard shard);
}
