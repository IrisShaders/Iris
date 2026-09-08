package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.Uniform;
import net.irisshaders.iris.mixinterface.GlProgramBindings;
import net.irisshaders.iris.pipeline.programs.IrisBindings;
import net.irisshaders.iris.pipeline.programs.IrisProgram;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mixin(GlProgram.class)
public class MixinGlProgram implements GlProgramBindings {
	@Shadow
	@Final
	private List<Uniform> uniforms;
	@Shadow
	private Uniform.Ubo pushConstant;

	@Unique
	private final Map<String, Uniform.Utb> iris$textureBuffers = new HashMap<>();

	@Inject(method = "setupBindGroupLayouts", at = @At("HEAD"), cancellable = true)
	private void iris$setupInitialBindings(List<BindGroupLayout.UniformDescription> descriptions, CallbackInfo ci) {
		if ((Object) this instanceof IrisProgram) {
			iris$setupBindings(descriptions, 0);
			ci.cancel();
		}
	}

	@Override
	public void iris$setupBindings(List<BindGroupLayout.UniformDescription> descriptions, int pushConstantsSize) {
		this.pushConstant = pushConstantsSize > 0 ? new Uniform.Ubo(IrisBindings.PUSH_CONSTANTS) : null;

		while (this.uniforms.size() < Math.max(descriptions.size(), IrisBindings.RESOURCE_COUNT)) {
			this.uniforms.add(null);
		}

		for (int i = 0; i < this.uniforms.size(); i++) {
			this.uniforms.set(i, null);
		}

		for (int i = 0; i < descriptions.size(); i++) {
			this.uniforms.set(i, iris$createBinding(descriptions.get(i)));
		}
	}

	@Inject(method = "close", at = @At("HEAD"))
	private void iris$deleteTextureBuffers(CallbackInfo ci) {
		if (this.iris$textureBuffers.isEmpty()) {
			return;
		}

		for (int i = 0; i < this.uniforms.size(); i++) {
			if (this.uniforms.get(i) instanceof Uniform.Utb) {
				this.uniforms.set(i, null);
			}
		}

		this.iris$textureBuffers.values().forEach(Uniform.Utb::close);
		this.iris$textureBuffers.clear();
	}

	@Unique
	private Uniform iris$createBinding(BindGroupLayout.UniformDescription description) {
		return switch (description.type()) {
			case UNIFORM_BUFFER -> {
				int binding = iris$uniformBufferBinding(description.name());
				yield binding < 0 ? null : new Uniform.Ubo(binding);
			}
			case COMBINED_IMAGE_SAMPLER -> {
				int binding = iris$samplerBinding(description.name());
				yield binding < 0 ? null : new Uniform.Sampler(binding);
			}
			case TEXEL_BUFFER -> {
				int binding = iris$samplerBinding(description.name());
				yield binding < 0 ? null : this.iris$textureBuffers.computeIfAbsent(description.name(), name -> new Uniform.Utb(binding, Objects.requireNonNull(description.gpuFormat())));
			}
		};
	}

	@Unique
	private static int iris$uniformBufferBinding(String name) {
		return switch (name) {
			case "DynamicTransforms", "TerrainUniform" -> IrisBindings.DYNAMIC_TRANSFORMS;
			case "CloudInfo" -> IrisBindings.CLOUD_INFO;
			case "Projection" -> IrisBindings.PROJECTION;
			case "Globals" -> IrisBindings.GLOBALS;
			case "Fog" -> IrisBindings.FOG;
			case "Lighting" -> IrisBindings.LIGHTING;
			case "u_Globals" -> IrisBindings.SODIUM_GLOBALS;
			default -> -1;
		};
	}

	@Unique
	private static int iris$samplerBinding(String name) {
		return switch (name) {
			case "Sampler0", "u_BlockTex" -> IrisBindings.ALBEDO_TEXTURE;
			case "Sampler1" -> IrisBindings.OVERLAY_TEXTURE;
			case "Sampler2", "u_LightTex" -> IrisBindings.LIGHTMAP_TEXTURE;
			case "CloudFaces", "u_SectionTimeInfo" -> IrisBindings.AUX_TEXTURE;
			default -> -1;
		};
	}
}
