package net.irisshaders.iris.mixin.fantastic;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.fantastic.ParticleRenderingPhase;
import net.irisshaders.iris.fantastic.PhasedParticleEngine;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Extends the ParticleEngine class to allow multiple phases of particle rendering.
 * <p>
 * This is used to enable the rendering of known-opaque particles much earlier than other particles, most notably before
 * translucent content. Normally, particles behind translucent blocks are not visible on Fancy graphics, and a user must
 * enable the much more intensive Fabulous graphics option. This is not ideal because Fabulous graphics is fundamentally
 * incompatible with most shader packs.
 * <p>
 * So what causes this? Essentially, on Fancy graphics, all particles are rendered after translucent terrain. Aside from
 * causing problems with particles being invisible, this also causes particles to write to the translucent depth buffer,
 * even when they are not translucent. This notably causes problems with particles on Sildur's Enhanced Default when
 * underwater.
 * <p>
 * So, what these mixins do is try to render known-opaque particles right before entities are rendered and right after
 * opaque terrain has been rendered. This seems to be an acceptable injection point, and has worked in my testing. It
 * fixes issues with particles when underwater, fixes a vanilla bug, and doesn't have any significant performance hit.
 * A win-win!
 * <p>
 * Unfortunately, there are limitations. Some particles rendering in texture sheets where translucency is supported. So,
 * even if an individual particle from that sheet is not translucent, it will still be treated as translucent, and thus
 * will not be affected by this patch. Without making more invasive and sweeping changes, there isn't a great way to get
 * around this.
 * <p>
 * As the saying goes, "Work smarter, not harder."
 */
@Mixin(ParticleEngine.class)
public class MixinParticleEngine implements PhasedParticleEngine {
	private static final List<ParticleRenderType> OPAQUE_PARTICLE_RENDER_TYPES;
	@Shadow
	@Final
	private static List<ParticleRenderType> RENDER_ORDER;

	static {
		OPAQUE_PARTICLE_RENDER_TYPES = ImmutableList.of(
			ParticleRenderType.PARTICLE_SHEET_OPAQUE,
			ParticleRenderType.PARTICLE_SHEET_LIT,
			ParticleRenderType.CUSTOM,
			ParticleRenderType.NO_RENDER
		);
	}

	@Unique
	private ParticleRenderingPhase phase = ParticleRenderingPhase.EVERYTHING;

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V"))
	private void iris$changeParticleShader(Supplier<ShaderInstance> pSupplier0) {
		RenderSystem.setShader(phase == ParticleRenderingPhase.TRANSLUCENT ? ShaderAccess::getParticleTranslucentShader : pSupplier0);
	}

	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticleEngine;RENDER_ORDER:Ljava/util/List;"))
	private List<ParticleRenderType> iris$selectParticlesToRender() {
		if (phase == ParticleRenderingPhase.TRANSLUCENT) {
			// Create a copy of the list
			//
			// We re-copy the list every time in case someone has added new particle texture sheets behind our back.
			List<ParticleRenderType> toRender = new ArrayList<>(RENDER_ORDER);

			// Remove all known opaque particle texture sheets.
			toRender.removeAll(OPAQUE_PARTICLE_RENDER_TYPES);

			return toRender;
		} else if (phase == ParticleRenderingPhase.OPAQUE) {
			// Render only opaque particle sheets
			return OPAQUE_PARTICLE_RENDER_TYPES;
		} else {
			// Don't override particle rendering
			return RENDER_ORDER;
		}
	}

	@Override
	public void setParticleRenderingPhase(ParticleRenderingPhase phase) {
		this.phase = phase;
	}
}
