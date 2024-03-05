package net.irisshaders.iris.shaderpack;

public interface ProgramSetInterface {
	class Empty implements ProgramSetInterface {

		public static final ProgramSetInterface INSTANCE = new Empty();
	}
}
