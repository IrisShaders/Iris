package kroppeb.stareval.parser;

import kroppeb.stareval.exception.MissingTokenException;
import kroppeb.stareval.exception.ParseException;
import kroppeb.stareval.exception.UnexpectedTokenException;
import kroppeb.stareval.token.*;

import java.util.ArrayList;
import java.util.List;

class TokenStack {
	final List<Token> stack = new ArrayList<>();

	Token peek() {
		if (!this.stack.isEmpty()) {
			return this.stack.get(this.stack.size() - 1);
		}

		return null;
	}

	private Token pop() {
		if (this.stack.isEmpty()) {
			throw new IllegalStateException("Internal token stack is empty");
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
	void push(Token token) {
		final Token top = this.peek();

		if (token instanceof BinaryOperatorToken) {
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
	 * @throws ClassCastException if the top is not an expression
	 * @see #expressionReducePop(int)
	 */
	ExpressionToken expressionReducePop() {
		return this.expressionReducePop(Integer.MAX_VALUE);
	}

	/**
	 * Pops an expression after trying to reduce the stack.
	 * Executes following reduce steps:
	 * <ul>
	 *     <li>{@link PriorityOperatorToken}, {@link ExpressionToken} => {@link ExpressionToken}
	 *     as long as the {@code priority} of the {@link PriorityOperatorToken} is stricter than the given priority</li>
	 * </ul>
	 *
	 * @throws ClassCastException if the top is not an expression
	 */
	private ExpressionToken expressionReducePop(int priority) {
		ExpressionToken token = (ExpressionToken) this.pop();

		while (!this.stack.isEmpty()) {
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
	 *
	 * @param index the current reader index, for exception throwing.
	 *
	 * @throws ClassCastException if the top is not an expression
	 */
	void commaReduce(int index) throws ParseException {
		// ( expr,
		// UnfinishedArgs Expression (commaReduce)
		// => UnfinishedArgs

		ExpressionToken expr = this.expressionReducePop();
		Token args = this.peek();

		if (args == null) {
			throw new MissingTokenException(
					"Expected an opening bracket '(' before seeing a comma ',' or closing bracket ')'",
					index
			);
		}

		if (args instanceof UnfinishedArgsToken) {
			((UnfinishedArgsToken) args).tokens.add(expr);
		} else {
			throw new UnexpectedTokenException(
					"Expected to see an opening bracket '(' or a comma ',' right before an expression followed by a " +
							"closing bracket ')' or a comma ','", index);
		}
	}

	/**
	 * Allows for trailing comma.
	 * Executes following reduce steps:
	 * <ul>
	 *     <li>{@link UnfinishedArgsToken}, {@link #expressionReducePop} => {@link ArgsToken}</li>
	 *     <li>{@link UnfinishedArgsToken} => {@link ArgsToken}</li>
	 * </ul>
	 *
	 * @param index the current reader index, for exception throwing.
	 */
	void bracketReduce(int index) throws ParseException {
		//
		// ( ... )
		// UnfinishedArgsToken Expression? (callReduce)

		boolean expressionOnTop = this.peek() instanceof ExpressionToken;
		if (expressionOnTop) {
			this.commaReduce(index);
		}

		UnfinishedArgsToken args;
		{
			if(this.stack.isEmpty()){
				throw new MissingTokenException("A closing bracket ')' can't be the first character of an expression", index);
			}

			Token pop = this.pop();

			if (!(pop instanceof UnfinishedArgsToken)) {
				throw new UnexpectedTokenException(
						"Expected to see an opening bracket '(' or a comma ',' right before an expression followed by a " +
								"closing bracket ')' or a comma ','", index);
			}
			args = (UnfinishedArgsToken) pop;
		}

		Token top = this.peek();

		if (top instanceof IdToken) {
			this.pop();
			this.push(new CallToken(((IdToken) top).id, args.tokens));
		} else {
			if(args.tokens.isEmpty()){
				throw new MissingTokenException("Encountered empty brackets that aren't a call", index);
			} else if(args.tokens.size() > 1){
				throw new UnexpectedTokenException("Encountered too many expressions in brackets that aren't a call", index);
			} else if(!expressionOnTop){
				throw new UnexpectedTokenException("Encountered a trailing comma in brackets that aren't a call", index);
			} else {
				this.push(args.tokens.get(0));
			}
		}
	}
}
