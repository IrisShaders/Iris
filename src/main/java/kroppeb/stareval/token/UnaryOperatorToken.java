package kroppeb.stareval.token;

import kroppeb.stareval.parser.UnaryOp;

public class UnaryOperatorToken extends Token {
	public final UnaryOp op;
	
	public UnaryOperatorToken(UnaryOp op) {
		this.op = op;
	}
	
	@Override
	public String toString() {
		return "UnaryOp{" + op + "}";
	}
}
