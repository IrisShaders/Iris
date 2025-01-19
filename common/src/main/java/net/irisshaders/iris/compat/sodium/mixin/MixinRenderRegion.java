package net.irisshaders.iris.compat.sodium.mixin;

import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.irisshaders.iris.compat.sodium.mixinterface.ShadowRenderRegion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderRegion.class)
public class MixinRenderRegion implements ShadowRenderRegion {
	@Final
	@Shadow
	@Mutable
	private ChunkRenderList renderList;

	@Unique
	ChunkRenderList regularRenderList;

	@Unique
	ChunkRenderList shadowRenderList;

	@Unique
	@Override
	public void swapToShadowRenderList() {
		this.regularRenderList = this.renderList;
		this.renderList = this.shadowRenderList;
		this.ensureRenderList();
	}

	@Unique
	@Override
	public void swapToRegularRenderList() {
		this.shadowRenderList = this.renderList;
		this.renderList = this.regularRenderList;
		this.ensureRenderList();
	}

	@Unique
	private void ensureRenderList() {
		if (this.renderList == null) {
			this.renderList = new ChunkRenderList((RenderRegion) (Object) this);
		}
	}
}
