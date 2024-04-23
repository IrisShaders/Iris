package net.irisshaders.iris.shaderpack.loading;

public enum ProgramArrayId {
	Setup(ProgramGroup.Setup, 100),
	Begin(ProgramGroup.Begin, 100),
	ShadowComposite(ProgramGroup.ShadowComposite, 100),
	Prepare(ProgramGroup.Prepare, 100),
	Deferred(ProgramGroup.Deferred, 100),
	Composite(ProgramGroup.Composite, 100),
	;

	private final ProgramGroup group;
	private final int numPrograms;

	ProgramArrayId(ProgramGroup group, int numPrograms) {
		this.group = group;
		this.numPrograms = numPrograms;
	}

	public ProgramGroup getGroup() {
		return group;
	}

	public String getSourcePrefix() {
		return group.getBaseName();
	}

	public int getNumPrograms() {
		return numPrograms;
	}
}
