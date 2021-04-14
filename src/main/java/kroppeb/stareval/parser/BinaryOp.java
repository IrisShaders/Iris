package kroppeb.stareval.parser;

public class BinaryOp extends Op {
	public final String name;
	public final int priority;
	
	public BinaryOp(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}
	
	@Override
	public String toString() {
		return name + "{" + priority + "}" ;
	}
}
