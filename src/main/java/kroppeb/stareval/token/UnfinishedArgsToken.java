package kroppeb.stareval.token;

import java.util.ArrayList;
import java.util.List;

public class UnfinishedArgsToken extends Token {
	public final List<ExpressionToken> tokens = new ArrayList<>();

	@Override
	public String toString() {
		return "UnfinishedArgs{" + this.tokens + "}";
	}
}
