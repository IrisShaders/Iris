package net.coderbot.iris.mixin.fantastic;

import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPhase.class)
public interface RenderPhaseAccessor {
	@Accessor("NO_TRANSPARENCY")
	static RenderPhase.Transparency getNO_TRANSPARENCY() {
		throw new AssertionError();
	}
}
