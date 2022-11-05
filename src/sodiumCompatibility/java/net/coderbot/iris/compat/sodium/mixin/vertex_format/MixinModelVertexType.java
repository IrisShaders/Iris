package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.chunk.format.sfp.ModelVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * A ridiculous solution to an impossible problem
 *
 * <p>It is possible for a texture coordinate to equal 1.0 or be very close to 1.0 when
 *    a sprite is located on the very edge of the texture atlas. However, when presented
 *    with a value very close to 1.0, Sodium's code for encoding texture coordinates
 *    returns a value of 0.0 instead of 1.0 due to integer overflow. This code ensures
 *    that the incoming texture coordinate is clamped such that it will not get close
 *    enough to 1.0 to trigger the overflow.</p>
 *
 * <p>This flaw was extremely difficult to come across, because it requires a very
 *    specific set of circumstances to occur. The only two ways this bug can occur
 *    is either a fluid sprite being placed on the bottom or right of a texture atlas,
 *    which is a rare occurrence, or a small sprite being placed on to the edge
 *    of a relatively large atlas, and then that sprite actually showing up in terrain
 *    rendering. Both of these cases require specific combinations of resource packs or
 *    mods, often leading to situations where removing a given innocent mod appears to
 *    fix the issue.</p>
 *
 * <p>The release of Iris 1.2.5 introduced atlas stitching optimizations by
 *    PepperCode1, but also resulted in a peculiar error: some blocks in the Modfest
 *    modpack would display stripes/corruption on the side. As it turned out, the
 *    optimized stitching meant that textures were packed far tighter into the atlas,
 *    substantially increasing the likelihood of textures ending up on the problematic
 *    edges of the atlas.</p>
 *
 * <p>Initially, the blame was placed on the Indium mod, since the issue was only observed
 *    through some mods using the Fabric Rendering API. However, after investigation
 *    into Indium's code, nothing came up. The issue sat for months, until a report
 *    of the issue occurring in an environment containing only Iris and Sodium came in.
 *    This required a specific combination of resource packs to reproduce. Then, the
 *    blame fell on the stitching optimizations. This seemed like the clear culprit,
 *    but we were unable to find a correctness issue in the stitching code.</p>
 *
 * <p>Finally, after reaching out to the reporter of that issue,
 *    it turned out that they had already isolated a single resource pack that would
 *    trigger the bug. After a few hours in the debugger, the actual issue was uncovered.
 *    </p>
 *
 * <p>So, what's the end goal of all this rambling? Basically, resist the urge to place
 *    blame on to a mod. Instead, focus on getting a stable set of reproduction steps,
 *    which might take a while. Then, once those steps are available, use the debugger
 *    to its fullest extent.</p>
 */
@Mixin(ModelVertexType.class)
public class MixinModelVertexType {
	@ModifyVariable(method = "encodeBlockTexture", at = @At("HEAD"), remap = false)
	private static float iris$clampUV(float uv) {
		return Math.min(0.99999997F, uv);
	}
}
