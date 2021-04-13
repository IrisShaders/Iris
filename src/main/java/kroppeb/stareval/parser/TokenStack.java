package kroppeb.stareval.parser;

import java.util.ArrayList;
import java.util.List;

import kroppeb.stareval.token.ArgsToken;
import kroppeb.stareval.token.BinaryExpressionToken;
import kroppeb.stareval.token.BinaryOperatorToken;
import kroppeb.stareval.token.CallToken;
import kroppeb.stareval.token.ExpressionToken;
import kroppeb.stareval.token.IdToken;
import kroppeb.stareval.token.Token;
import kroppeb.stareval.token.UnaryExpressionToken;
import kroppeb.stareval.token.UnaryOperatorToken;
import kroppeb.stareval.token.UnfinishedArgsToken;

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
	 *     <li>{@link UnaryOperatorToken} | {@link ExpressionToken} => {@link UnaryExpressionToken}</li>
	 *     <li>
	 *         {@link ExpressionToken}, {@link BinaryOperatorToken}, {@link ExpressionToken} | {@link BinaryOperatorToken}
	 *         where the BinOp on the stack has a higher or equal priority to the one being added, the 3 items on the
	 *         stack get popped, merged to a {@link BinaryExpressionToken} and placed on the stack.
	 *         The new token is then pushed again.
	 *     </li>
	 * </ul>
	 */
	void push(Token token) throws Exception {
		Token top = this.peek();
		if (token instanceof ExpressionToken) {
			if (token instanceof ArgsToken && top instanceof IdToken) {
				this.pop();
				push(new CallToken(((IdToken) top).id, ((ArgsToken) token).tokens));
				return;
			}
			else if (top instanceof UnaryOperatorToken) {
				this.pop();
				push(new UnaryExpressionToken(((UnaryOperatorToken) top).op, (ExpressionToken) token));
				return;
			}
		}
		else if (token instanceof BinaryOperatorToken) {
			// bin ops need to follow an expression
			assert top instanceof ExpressionToken;


			if (stack.size() >= 3) {
				Token other = this.peek(1);
				if (other instanceof BinaryOperatorToken &&
					((BinaryOperatorToken) other).op.priority <= ((BinaryOperatorToken) token).op.priority) {
					// stack[ 'a', '*', 'b'], token = '+'
					ExpressionToken b = (ExpressionToken) this.pop();
					this.pop();
					ExpressionToken a = (ExpressionToken) this.pop();

					// merge op
					this.stack.add(new BinaryExpressionToken(((BinaryOperatorToken) other).op, a, b));
					// retry pushing this token
					this.push(token);
				}
			}
		}

		this.stack.add(token);
	}

	/**
	 * Pops an expression after trying to reduce the stack.
	 * Executes following reduce step:
	 * <ul>
	 *     <li>{@link ExpressionToken}, {@link BinaryOperatorToken}, {@link ExpressionToken} => {@link BinaryExpressionToken}</li>
	 * </ul>
	 */
	ExpressionToken expressionReducePop() throws Exception {
		ExpressionToken token = (ExpressionToken)pop();
		while (stack.size() >= 2) {
			Token x = peek(0);
			Token a = peek(1);


			if (x instanceof BinaryOperatorToken) {
				// a should have been asserted when x got added
				assert a instanceof ExpressionToken;

				pop(2);
				token = new BinaryExpressionToken(((BinaryOperatorToken) x).op, (ExpressionToken) a, token);
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
