package net.irisshaders.iris.shaderpack.loading;

public enum ProgramGroup {
	Setup("setup"),
	Begin("begin"),
	Shadow("shadow"),
	ShadowComposite("shadowcomp"),
	Prepare("prepare"),
	Gbuffers("gbuffers"),
	Deferred("deferred"),
	Composite("composite"),
	Final("final"),
	Dh("dh");

	private final String baseName;

	ProgramGroup(String baseName) {
		this.baseName = baseName;
	}

	public String getBaseName() {
		return baseName;
	}
}
