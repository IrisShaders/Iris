package net.coderbot.iris.mixin.fantastic;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.fantastic.IrisParticleTextureSheets;
import net.coderbot.iris.fantastic.ParticleRenderingPhase;
import net.coderbot.iris.fantastic.PhasedParticleManager;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends the ParticleManager class to allow multiple phases of particle rendering.
 *
 * This is used to enable the rendering of known-opaque particles much earlier than other particles, most notably before
 * translucent content. Normally, particles behind translucent blocks are not visible on Fancy graphics, and a user must
 * enable the much more intensive Fabulous graphics option. This is not ideal because Fabulous graphics is fundamentally
 * incompatible with most shaderpacks.
 *
 * So what causes this? Essentially, on Fancy graphics, all particles are rendered after translucent terrain. Aside from
 * causing problems with particles being invisible, this also causes particles to write to the translucent depth buffer,
 * even when they are not translucent. This notably causes problems with particles on Sildur's Enhanced Default when
 * underwater.
 *
 * So, what these mixins do is try to render known-opaque particles right before entities are rendered and right after
 * opaque terrain has been rendered. This seems to be an acceptable injection point, and has worked in my testing. It
 * fixes issues with particles when underwater, fixes a vanilla bug, and doesn't have any significant performance hit.
 * A win-win!
 *
 * Unfortunately, there are limitations. Some particles rendering in texture sheets where translucency is supported. So,
 * even if an individual particle from that sheet is not translucent, it will still be treated as translucent, and thus
 * will not be affected by this patch. Without making more invasive and sweeping changes, there isn't a great way to get
 * around this.
 *
 * As the saying goes, "Work smarter, not harder."
 */
@Mixin(ParticleManager.class)
public class MixinParticleManager implements PhasedParticleManager {
	@Unique
	private ParticleRenderingPhase phase = ParticleRenderingPhase.EVERYTHING;

	@Shadow
	@Final
	private static List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS;

	private static final List<ParticleTextureSheet> OPAQUE_PARTICLE_TEXTURE_SHEETS;

	static {
		OPAQUE_PARTICLE_TEXTURE_SHEETS = ImmutableList.of(
			IrisParticleTextureSheets.OPAQUE_TERRAIN_SHEET,
			ParticleTextureSheet.PARTICLE_SHEET_OPAQUE,
			ParticleTextureSheet.PARTICLE_SHEET_LIT,
			ParticleTextureSheet.CUSTOM,
			ParticleTextureSheet.NO_RENDER
		);
	}

	@Redirect(method = "renderParticles", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticleManager;PARTICLE_TEXTURE_SHEETS:Ljava/util/List;"))
	private List<ParticleTextureSheet> iris$selectParticlesToRender() {
		if (phase == ParticleRenderingPhase.TRANSLUCENT) {
			// Create a copy of the list
			//
			// We re-copy the list every time in case someone has added new particle texture sheets behind our back.
			List<ParticleTextureSheet> toRender = new ArrayList<>(PARTICLE_TEXTURE_SHEETS);

			// Remove all known opaque particle texture sheets.
			toRender.removeAll(OPAQUE_PARTICLE_TEXTURE_SHEETS);

			return toRender;
		} else if (phase == ParticleRenderingPhase.OPAQUE) {
			// Render only opaque particle sheets
			return OPAQUE_PARTICLE_TEXTURE_SHEETS;
		} else {
			// Don't override particle rendering
			return PARTICLE_TEXTURE_SHEETS;
		}
	}

	@Override
	public void setParticleRenderingPhase(ParticleRenderingPhase phase) {
		this.phase = phase;
	}
}
