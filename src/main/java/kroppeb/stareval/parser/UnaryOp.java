package kroppeb.stareval.parser;

public class UnaryOp extends Op {
	public final String name;
	
	public UnaryOp(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
