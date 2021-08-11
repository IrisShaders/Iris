package net.coderbot.iris.shaderpack;

import java.util.Set;

public class PackDirectives {
	private final PackRenderTargetDirectives renderTargetDirectives;
	private final PackShadowDirectives shadowDirectives;
	private int noiseTextureResolution;
	private float sunPathRotation;
	private boolean areCloudsEnabled;
	private boolean separateAo;

	private PackDirectives(Set<Integer> supportedRenderTargets) {
		noiseTextureResolution = 256;
		sunPathRotation = 0.0F;
		renderTargetDirectives = new PackRenderTargetDirectives(supportedRenderTargets);
		shadowDirectives = new PackShadowDirectives();
	}

	PackDirectives(Set<Integer> supportedRenderTargets, ShaderProperties properties) {
		this(supportedRenderTargets);
		areCloudsEnabled = properties.areCloudsEnabled();
		separateAo = properties.getSeparateAo().orElse(false);
	}

	PackDirectives(Set<Integer> supportedRenderTargets, PackDirectives directives) {
		this(supportedRenderTargets);
		areCloudsEnabled = directives.areCloudsEnabled();
		separateAo = directives.separateAo;
	}

	public int getNoiseTextureResolution() {
		return noiseTextureResolution;
	}

	public float getSunPathRotation() {
		return sunPathRotation;
	}

	public boolean areCloudsEnabled() {
		return areCloudsEnabled;
	}

	public boolean shouldUseSeparateAo() {
		return separateAo;
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
	}
}
