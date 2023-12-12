package net.coderbot.iris.compat.sodium.mixin.directional_shading;

import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ModelQuadEncoder;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockRenderer.class)
public class MixinBlockRenderer {
	@Redirect(method = "writeGeometry", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/world/WorldSlice;getShade(Lnet/minecraft/core/Direction;Z)F"))
	private float iris$disableDirectionalShading(WorldSlice instance, Direction direction, boolean shaded) {
		// TODO Sodium
		if (BlockRenderingSettings.INSTANCE.shouldDisableDirectionalShading()) {
			return 1.0f;
		}
		return instance.getShade(direction, shaded);
	}

}
