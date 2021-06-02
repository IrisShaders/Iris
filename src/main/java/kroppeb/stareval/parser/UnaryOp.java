package kroppeb.stareval.parser;

public class UnaryOp {
	private final String name;

	public UnaryOp(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
