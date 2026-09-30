package net.irisshaders.iris.mixin.forge;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * This mixin used to @WrapOperation the CommandEncoder.clearDepthTexture call inside
 * LevelRenderer's always-on-top pass lambda, so that the depth clear could be skipped while a shader
 * pack was active.
 *
 * <p>In 26.3 that call no longer exists. The always-on-top pass was restructured into the concrete
 * method executeAlwaysOnTop(...), and its depth is cleared two other ways instead: the frame graph
 * allocates the target from a pre-cleared RenderTargetDescriptor, and the pass itself is opened with
 * createRenderPass(..., OptionalDouble.of(0.0)). There is no single operation left to wrap, so the
 * injection was failing to find a target and taking the whole game down with it.
 *
 * <p>Neutralised rather than reimplemented: guessing at the replacement would mean inventing Iris
 * depth semantics against a pipeline its authors have not ported yet. The cost is that always-on-top
 * geometry now uses vanilla depth-clear behaviour while shaders are active.
 */
@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
}
