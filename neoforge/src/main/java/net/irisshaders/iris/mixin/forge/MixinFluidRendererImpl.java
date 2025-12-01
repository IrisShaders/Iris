package net.irisshaders.iris.mixin.forge;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.neoforge.render.FluidRendererImpl;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FluidRendererImpl.class)
public class MixinFluidRendererImpl implements VertexEncoderInterface {
	@Shadow
	@Final
	private DefaultFluidRenderer defaultRenderer;

	@Override
	public void beginBlock(int blockId, byte isFluid, byte lightEmission, int x, int y, int z) {
		((VertexEncoderInterface) this.defaultRenderer).beginBlock(blockId, isFluid, lightEmission, x, y, z);
	}
}
