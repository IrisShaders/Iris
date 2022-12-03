package kroppeb.stareval.parser;

public class BinaryOp {
	private final String name;
	private final int priority;

	public BinaryOp(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name + "{" + this.priority + "}";
	}

	public int getPriority() {
		return this.priority;
	}
}
