package kroppeb.stareval.token;

import java.util.List;

public class CallToken extends ExpressionToken {
	public final String id;
	public final List<ExpressionToken> args;
	
	public CallToken(String id, List<ExpressionToken> args) {
		this.id = id;
		this.args = args;
	}
	
	@Override
	public String toString() {
		return "CallToken{" + id + " {" + args + "} }";
	}
	
	@Override
	public ExpressionToken simplify() {
		for (int i = 0; i < args.size(); i++) {
			args.set(i, args.get(i).simplify());
		}
		return this;
	}
}