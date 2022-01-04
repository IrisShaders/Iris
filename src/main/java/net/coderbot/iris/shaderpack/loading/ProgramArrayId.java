package net.coderbot.iris.shaderpack.loading;

public enum ProgramArrayId {
	ShadowComposite(ProgramGroup.ShadowComposite, 99),
	Prepare(ProgramGroup.Prepare, 99),
	Deferred(ProgramGroup.Deferred, 99),
	Composite(ProgramGroup.Composite, 99),
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
