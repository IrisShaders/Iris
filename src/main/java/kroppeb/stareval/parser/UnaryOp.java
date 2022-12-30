package kroppeb.stareval.parser;

public class UnaryOp {
	private final String name;

	public UnaryOp(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
