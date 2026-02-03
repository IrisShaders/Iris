package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.irisshaders.iris.compat.general.IrisModSupport;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(AbstractBlockRenderContext.class)
public class MixinAbstractBlockRenderContext {
	@Shadow
	protected BlockPos pos;

	@Shadow
	protected BlockAndTintGetter level;

	@Shadow
	protected BlockState state;

	@Inject(method = "bufferDefaultModel", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformModelAccess;getQuads(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/model/BlockModelPart;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;)Ljava/util/List;"))
	private void checkDirectionNeo(BlockModelPart part, Predicate<Direction> cullTest, Consumer<MutableQuadViewImpl> emitter, CallbackInfo ci, @Local Direction cullFace) {
		if ((Object) this instanceof BlockRenderer r && WorldRenderingSettings.INSTANCE.getBlockStateIds() != null && cullFace != null) {
			BlockState override = IrisModSupport.INSTANCE.getModelPartState(part);
			if (override != null) {
				((VertexEncoderInterface) r).overrideBlock(WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(override));
			}
		}
	}

	@Inject(method = "bufferDefaultModel", at = @At(value = "TAIL"))
	private void checkDirectionNeo(BlockModelPart part, Predicate<Direction> cullTest, Consumer<MutableQuadViewImpl> emitter, CallbackInfo ci) {
		if ((Object) this instanceof BlockRenderer r && WorldRenderingSettings.INSTANCE.getBlockStateIds() != null) {
			((VertexEncoderInterface) r).restoreBlock();
		}
	}
}
