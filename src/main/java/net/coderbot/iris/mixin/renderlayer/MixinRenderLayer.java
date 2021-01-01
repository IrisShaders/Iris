package net.coderbot.iris.mixin.renderlayer;

import java.util.Optional;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.IrisRenderLayerWrapper;
import net.coderbot.iris.layer.ProgramRenderLayer;
import net.coderbot.iris.layer.UseProgramRenderPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.RenderLayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(RenderLayer.class)
public class MixinRenderLayer implements ProgramRenderLayer {
	@Shadow
	@Final
	@Mutable
	private static RenderLayer SOLID;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer CUTOUT_MIPPED;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer CUTOUT;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer TRANSLUCENT;

	@Shadow
	@Final
	@Mutable
	private static RenderLayer TRIPWIRE;

	@Unique
	private static RenderLayer iris$LINES;

	@Override
	public Optional<GbufferProgram> getProgram() {
		// By default, don't use shaders to render content
		return Optional.empty();
	}

	static {
		SOLID = wrap("iris:terrain_solid", SOLID, GbufferProgram.TERRAIN);
		CUTOUT_MIPPED = wrap("iris:terrain_cutout_mipped", CUTOUT_MIPPED, GbufferProgram.TERRAIN);
		CUTOUT = wrap("iris:terrain_cutout", CUTOUT, GbufferProgram.TERRAIN);
		TRANSLUCENT = wrap("iris:translucent", TRANSLUCENT, GbufferProgram.TRANSLUCENT_TERRAIN);
		TRIPWIRE = wrap("iris:tripwire", TRIPWIRE, GbufferProgram.TRANSLUCENT_TERRAIN);
		// TODO: figure out how to assign to RenderLayer.LINES
		// We cannot use @Shadow easily because the type of the field is a package-private class
		iris$LINES = wrap("iris:lines", RenderLayer.LINES, GbufferProgram.BASIC);
	}

	/**
	 * @author coderbot
	 * @reason Use the wrapped render layer instead.
	 */
	@Overwrite
	public static RenderLayer getLines() {
		return iris$LINES;
	}

	private static RenderLayer wrap(String name, RenderLayer wrapped, GbufferProgram program) {
		return new IrisRenderLayerWrapper(name, wrapped, new UseProgramRenderPhase(program));
	}
}
