package kroppeb.stareval.parser;

import java.util.ArrayList;
import java.util.List;

import kroppeb.stareval.token.ArgsToken;
import kroppeb.stareval.token.BinaryExpressionToken;
import kroppeb.stareval.token.BinaryOperatorToken;
import kroppeb.stareval.token.CallToken;
import kroppeb.stareval.token.ExpressionToken;
import kroppeb.stareval.token.IdToken;
import kroppeb.stareval.token.PartialBinaryExpressionToken;
import kroppeb.stareval.token.PriorityOperatorToken;
import kroppeb.stareval.token.Token;
import kroppeb.stareval.token.UnaryExpressionToken;
import kroppeb.stareval.token.UnaryOperatorToken;
import kroppeb.stareval.token.UnfinishedArgsToken;

class TokenStack {
	final List<Token> stack = new ArrayList<>();

	Token peek() {
		if (!this.stack.isEmpty()) {
			return this.stack.get(this.stack.size() - 1);
		}
		return null;
	}

	private Token peek(int offset) {
		if (this.stack.size() > offset) {
			return this.stack.get(this.stack.size() - 1 - offset);
		}
		return null;
	}

	private void pop(int count) throws Exception {
		for (int i = 0; i < count; i++) {
			this.pop();
		}
	}

	private Token pop() throws Exception {
		if (this.stack.isEmpty()) {
			throw new Exception();
		}
		return this.stack.remove(this.stack.size() - 1);
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
		final Token top = this.peek();
		if (token instanceof ArgsToken && top instanceof IdToken) {
			this.pop();
			this.push(new CallToken(((IdToken) top).id, ((ArgsToken) token).tokens));
		} else if (token instanceof BinaryOperatorToken) {
			BinaryOperatorToken binOpToken = (BinaryOperatorToken) token;

			// reduce the expressions to the needed priority level
			ExpressionToken left = this.expressionReducePop(binOpToken.op.getPriority());
			// stack[ {'a', '*'}, 'b'], token = '+' -> stack[], left = {'a', '*', 'b'}
			//                                      -> stack[{{'a', '*', 'b'}, '+'}]
			// stack[ {'a', '+'}, 'b'], token = '+' -> stack[], left = {'a', '+', 'b'}
			//                                      -> stack[{{'a', '+', 'b'}, '+'}]
			// stack[ {     '-'}, 'b'], token = '+' -> stack[], left = {'-', 'b'}
			//                                      -> stack[{{     '-', 'b'}, '+'}]

			// stack[ {'a', '+'}, 'b'], token = '*' -> stack[{'a', '+'}], left = {'b'}
			//                                      -> stack[{'a', '+'}, {'b', '*'}]

			this.stack.add(new PartialBinaryExpressionToken(left, binOpToken.op));

		} else {
			this.stack.add(token);
		}
	}

	/**
	 * @see #expressionReducePop(int)
	 */
	ExpressionToken expressionReducePop() throws Exception {
		return this.expressionReducePop(Integer.MAX_VALUE);
	}

	/**
	 * Pops an expression after trying to reduce the stack.
	 * Executes following reduce steps:
	 * <ul>
	 *     <li>{@link PriorityOperatorToken}, {@link ExpressionToken} => {@link ExpressionToken}
	 *     as long as the {@code priority} of the {@link PriorityOperatorToken} is stricter than the given priority</li>
	 * </ul>
	 */
	private ExpressionToken expressionReducePop(int priority) throws Exception {
		ExpressionToken token = (ExpressionToken) this.pop();
		while (this.stack.size() >= 1) {
			Token x = this.peek();

			if (x instanceof PriorityOperatorToken && ((PriorityOperatorToken) x).getPriority() <= priority) {
				this.pop();
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

		ExpressionToken expr = this.expressionReducePop();
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
