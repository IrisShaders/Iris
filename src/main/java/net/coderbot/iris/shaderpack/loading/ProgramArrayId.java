package net.coderbot.iris.shaderpack.loading;

public enum ProgramArrayId {
	ShadowComposite(ProgramGroup.ShadowComposite, 16),
	Prepare(ProgramGroup.Prepare, 16),
	Deferred(ProgramGroup.Deferred, 16),
	Composite(ProgramGroup.Composite, 16),
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
