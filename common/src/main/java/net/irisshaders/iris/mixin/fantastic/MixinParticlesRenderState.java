package net.irisshaders.iris.mixin.fantastic;

import net.irisshaders.iris.mixinterface.ParticleRenderStateExtension;
import net.minecraft.client.particle.ItemPickupParticleGroup;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;
import net.minecraft.client.renderer.state.ParticlesRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ParticlesRenderState.class)
public class MixinParticlesRenderState implements ParticleRenderStateExtension {
	@Shadow
	@Final
	public List<ParticleGroupRenderState> particles;

	@Override
	public void submitWithoutItems(SubmitNodeStorage submitNodeStorage, CameraRenderState cameraRenderState) {
		for (ParticleGroupRenderState particleGroupRenderState : this.particles) {
			if (!(particleGroupRenderState instanceof ItemPickupParticleGroup.State)) {
				particleGroupRenderState.submit(submitNodeStorage, cameraRenderState);
			}
		}
	}
}
