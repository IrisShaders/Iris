package kroppeb.stareval.parser;

import kroppeb.stareval.token.*;

import java.util.ArrayList;
import java.util.List;

class TokenStack {
	final List<Token> stack = new ArrayList<>();

	Token peek() {
		if (stack.size() > 0) {
			return stack.get(stack.size() - 1);
		}
		return null;
	}

	private Token peek(int offset) {
		if (stack.size() > offset) {
			return stack.get(stack.size() - 1 - offset);
		}
		return null;
	}

	private void pop(int count) throws Exception {
		for (int i = 0; i < count; i++) {
			pop();
		}
	}

	private Token pop() throws Exception {
		if (stack.isEmpty()) {
			throw new Exception();
		}
		return stack.remove(stack.size() - 1);
	}

	/**
	 * Executes following reduce steps:
	 * <ul>
	 *     <li>{@link IdToken} | {@link ArgsToken} => {@link CallToken}</li>
	 *     <li>{@link ExpressionToken} | {@link BinaryOperatorToken} => {@link PartialBinaryExpressionToken}</li>
	 *     <li>{@link UnaryOperatorToken}, {@link ExpressionToken} | {@link BinaryOperatorToken} => {@link UnaryExpressionToken} | {@link BinaryOperatorToken}</li>
	 *     <li>
	 *         {@link PartialBinaryExpressionToken}, {@link ExpressionToken} | {@link BinaryOperatorToken} <br/>
	 *         where the operator on the stack has a higher or equal priority to the one being added, the 3 items on the
	 *         stack get popped, merged to a {@link BinaryExpressionToken} and placed on the stack.
	 *         The new token is then pushed again.
	 *     </li>
	 * </ul>
	 */
	void push(Token token) throws Exception {
		// in Kotlin I'd mark this tailrecursive.
		Token top = this.peek();
		if (token instanceof ArgsToken && top instanceof IdToken) {
			this.pop();
			push(new CallToken(((IdToken) top).id, ((ArgsToken) token).tokens));
		} else if (token instanceof BinaryOperatorToken) {
			BinaryOperatorToken binOpToken = (BinaryOperatorToken) token;

			// bin ops need to follow an expression
			// should have been guaranteed by the parser.
			assert top instanceof ExpressionToken;
			ExpressionToken b = (ExpressionToken) top;
			this.pop();


			Token other = this.peek(); // can be null
			if (other instanceof PriorityOperatorToken &&
					((PriorityOperatorToken) other).getPriority() <= binOpToken.op.priority) {
				// stack[ {'a', '*' }, 'b'], token = '+'
				// stack[ {'a', '+' }, 'b'], token = '+'
				// stack[ {'-'}, 'b'], token = '+'

				// does not match
				// stack[ {'a', '+' }, 'b'], token = '*'

				// pop `other`
				this.pop();
				PriorityOperatorToken otherOp = (PriorityOperatorToken) other;

				// merge op
				this.stack.add(otherOp.resolveWith(b));

				// retry pushing this token
				this.push(token);
			} else {
				this.stack.add(new PartialBinaryExpressionToken(b, binOpToken.op));
			}
		} else {
			this.stack.add(token);
		}
	}

	/**
	 * Pops an expression after trying to reduce the stack.
	 * Executes following reduce steps:
	 * <ul>
	 *     <li>{@link UnaryOperatorToken}, {@link ExpressionToken} => {@link BinaryExpressionToken}</li>
	 *     <li>{@link ExpressionToken}, {@link BinaryOperatorToken}, {@link ExpressionToken} => {@link BinaryExpressionToken}</li>
	 * </ul>
	 */
	ExpressionToken expressionReducePop() throws Exception {
		ExpressionToken token = (ExpressionToken) pop();
		while (stack.size() >= 1) {
			Token x = peek(0);

			if (x instanceof PriorityOperatorToken) {
				pop(1);
				token = ((PriorityOperatorToken) x).resolveWith(token);
			} else {
				break;
			}
		}
		return token;
	}

	/**
	 * Executes following reduce step:
	 * <ul>
	 *     <li>{@link UnfinishedArgsToken}, {@link #expressionReducePop} => {@link UnfinishedArgsToken}</li>
	 * </ul>
	 */
	void commaReduce() throws Exception {
		// ( expr,
		// UnfinishedArgs Expression (commaReduce)
		// => UnfinishedArgs

		ExpressionToken expr = expressionReducePop();
		UnfinishedArgsToken args = (UnfinishedArgsToken) this.peek();
		if (args == null) {
			throw new Exception();
		}
		args.tokens.add(expr);
	}

	/**
	 * Allows for trailing comma.
	 * Executes following reduce steps:
	 * <ul>
	 *     <li>{@link UnfinishedArgsToken}, {@link #expressionReducePop} => {@link ArgsToken}</li>
	 *     <li>{@link UnfinishedArgsToken} => {@link ArgsToken}</li>
	 * </ul>
	 */
	void bracketReduce() throws Exception {
		//
		// ( ... )
		// UnfinishedArgsToken Expression? (callReduce)

		if (this.peek() instanceof ExpressionToken) {
			this.commaReduce();
		}

		UnfinishedArgsToken args = (UnfinishedArgsToken) this.pop();

		this.push(new ArgsToken(args.tokens));
	}
}
