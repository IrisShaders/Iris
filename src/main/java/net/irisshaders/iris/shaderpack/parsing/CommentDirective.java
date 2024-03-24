package net.irisshaders.iris.shaderpack.parsing;

public class CommentDirective {
	private final Type type;
	private final String directive;
	private final int location;

	CommentDirective(Type type, String directive, int location) {
		this.type = type;
		this.directive = directive;
		this.location = location;
	}

	public Type getType() {
		return type;
	}

	/**
	 * @return The directive without {@literal /}* or *{@literal /}
	 */
	public String getDirective() {
		return directive;
	}

	/**
	 * @return The starting position of the directive in a multi-line string. <br>
	 * This is necessary to check if either the drawbuffer or the rendertarget directive should be applied
	 * when there are multiple in the same shader file, based on which one is defined last.
	 */
	public int getLocation() {
		return location;
	}

	public enum Type {
		DRAWBUFFERS,
		RENDERTARGETS
	}
}
