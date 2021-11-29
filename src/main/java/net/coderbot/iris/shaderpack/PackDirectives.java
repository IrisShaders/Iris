package net.coderbot.iris.shaderpack;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.coderbot.iris.Iris;

import java.util.Set;

public class PackDirectives {
	private int noiseTextureResolution;
	private float sunPathRotation;
	private float ambientOcclusionLevel;
	private boolean areCloudsEnabled;
	private boolean separateAo;
	private boolean oldLighting;
	private Object2ObjectMap<String, Object2BooleanMap<String>> explicitFlips = new Object2ObjectOpenHashMap<>();

	private final PackRenderTargetDirectives renderTargetDirectives;
	private final PackShadowDirectives shadowDirectives;

	private PackDirectives(Set<Integer> supportedRenderTargets) {
		noiseTextureResolution = 256;
		sunPathRotation = 0.0F;
		ambientOcclusionLevel = 1.0F;
		renderTargetDirectives = new PackRenderTargetDirectives(supportedRenderTargets);
		shadowDirectives = new PackShadowDirectives();
	}

	PackDirectives(Set<Integer> supportedRenderTargets, ShaderProperties properties) {
		this(supportedRenderTargets);
		areCloudsEnabled = properties.areCloudsEnabled();
		separateAo = properties.getSeparateAo().orElse(false);
		oldLighting = properties.getOldLighting().orElse(false);
		explicitFlips = properties.getExplicitFlips();
	}

	PackDirectives(Set<Integer> supportedRenderTargets, PackDirectives directives) {
		this(supportedRenderTargets);
		areCloudsEnabled = directives.areCloudsEnabled();
		separateAo = directives.separateAo;
		oldLighting = directives.oldLighting;
		explicitFlips = directives.explicitFlips;
	}

	public int getNoiseTextureResolution() {
		return noiseTextureResolution;
	}

	public float getSunPathRotation() {
		return sunPathRotation;
	}

	public float getAmbientOcclusionLevel() {
		return ambientOcclusionLevel;
	}

	public boolean areCloudsEnabled() {
		return areCloudsEnabled;
	}

	public boolean shouldUseSeparateAo() {
		return separateAo;
	}

	public boolean isOldLighting() {
		return oldLighting;
	}

	public PackRenderTargetDirectives getRenderTargetDirectives() {
		return renderTargetDirectives;
	}

	public PackShadowDirectives getShadowDirectives() {
		return shadowDirectives;
	}

	public void acceptDirectivesFrom(DirectiveHolder directives) {
		renderTargetDirectives.acceptDirectives(directives);
		shadowDirectives.acceptDirectives(directives);

		directives.acceptConstIntDirective("noiseTextureResolution",
				noiseTextureResolution -> this.noiseTextureResolution = noiseTextureResolution);

		directives.acceptConstFloatDirective("sunPathRotation",
				sunPathRotation -> this.sunPathRotation = sunPathRotation);

		directives.acceptConstFloatDirective("ambientOcclusionLevel",
				ambientOcclusionLevel -> this.ambientOcclusionLevel = ambientOcclusionLevel);

	}

	public ImmutableMap<Integer, Boolean> getExplicitFlips(String pass) {
		ImmutableMap.Builder<Integer, Boolean> explicitFlips = ImmutableMap.builder();

		Object2BooleanMap<String> explicitFlipsStr = this.explicitFlips.get(pass);

		if (explicitFlipsStr == null) {
			explicitFlipsStr = Object2BooleanMaps.emptyMap();
		}

		explicitFlipsStr.forEach((buffer, shouldFlip) -> {
			int index = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.indexOf(buffer);

			if (index == -1 && buffer.startsWith("colortex")) {
				String id = buffer.substring("colortex".length());

				try {
					index = Integer.parseInt(id);
				} catch (NumberFormatException e) {
					// fall through to index == null check for unknown buffer.
				}
			}

			if (index != -1) {
				explicitFlips.put(index, shouldFlip);
			} else {
				Iris.logger.warn("Unknown buffer with ID " + buffer + " specified in flip directive for pass "
						+ pass);
			}
		});

		return explicitFlips.build();
	}
}
