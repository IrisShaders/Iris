package net.coderbot.batchedentityrendering.mixin;

import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPhase.class)
public interface RenderPhaseAccessor {
	@Accessor("NO_TRANSPARENCY")
	static RenderPhase.Transparency getNO_TRANSPARENCY() {
		throw new AssertionError();
	}

	@Accessor("GLINT_TRANSPARENCY")
	static RenderPhase.Transparency getGLINT_TRANSPARENCY() {
		throw new AssertionError();
	}

	@Accessor("CRUMBLING_TRANSPARENCY")
	static RenderPhase.Transparency getCRUMBLING_TRANSPARENCY() {
		throw new AssertionError();
	}
}
