package net.irisshaders.iris.pipeline.programs;

public interface IrisProgram {
	void iris$setupState();

	void iris$clearState();

	int iris$getBlockIndex(int program, CharSequence uniformBlockName);

	boolean iris$isSetUp();
}
