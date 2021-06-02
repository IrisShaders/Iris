package kroppeb.stareval.parser;

import kroppeb.stareval.exception.MissingTokenException;
import kroppeb.stareval.exception.ParseException;
import kroppeb.stareval.exception.UnexpectedTokenException;
import kroppeb.stareval.token.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
	private final List<Token> stack = new ArrayList<>();

	Parser() {
	}

	private Token peek() {
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

	private void push(Token token) {
		this.stack.add(token);
	}

	/**
	 * @throws ClassCastException if the top is not an expression
	 * @see #expressionReducePop(int)
	 */
	private ExpressionToken expressionReducePop() {
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
	 * @throws ClassCastException if the top is not an expression
	 */
	private void commaReduce(int index) throws ParseException {
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

	// visitor methods

	void visitId(String id) {
		this.push(new IdToken(id));
	}

	boolean canReadAccess() {
		return this.peek() instanceof AccessableToken;
	}

	/**
	 * Assumes `canReadAccess` has returned true
	 */
	void visitAccess(String access) {
		AccessableToken pop = (AccessableToken) this.pop();
		this.push(new AccessToken(pop, access));
	}

	void visitNumber(String numberString) {
		this.push(new NumberToken(numberString));
	}

	void visitOpeningParenthesis() {
		this.push(new UnfinishedArgsToken());
	}

	void visitComma(int index) throws ParseException {
		if (this.peek() instanceof ExpressionToken) {
			this.commaReduce(index);
		} else {
			throw new UnexpectedTokenException("Expected an expression before a comma ','", index);
		}
	}

	/**
	 * Allows for trailing comma.
	 * Executes following reduce steps:
	 * <ul>
	 *     <li>{@link IdToken} {@link UnfinishedArgsToken}, {@link #expressionReducePop} => {@link CallToken}</li>
	 *     <li>{@link IdToken} {@link UnfinishedArgsToken} => {@link CallToken}</li>
	 *     <li>{@link UnfinishedArgsToken}, {@link #expressionReducePop} => {@link ExpressionToken}</li>
	 * </ul>
	 *
	 * @param index the current reader index, for exception throwing.
	 */
	void visitClosingParenthesis(int index) throws ParseException {
		//
		// ( ... )
		// UnfinishedArgsToken Expression? (callReduce)

		boolean expressionOnTop = this.peek() instanceof ExpressionToken;
		if (expressionOnTop) {
			this.commaReduce(index);
		}

		UnfinishedArgsToken args;
		{
			if (this.stack.isEmpty()) {
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
			if (args.tokens.isEmpty()) {
				throw new MissingTokenException("Encountered empty brackets that aren't a call", index);
			} else if (args.tokens.size() > 1) {
				throw new UnexpectedTokenException("Encountered too many expressions in brackets that aren't a call", index);
			} else if (!expressionOnTop) {
				throw new UnexpectedTokenException("Encountered a trailing comma in brackets that aren't a call", index);
			} else {
				this.push(args.tokens.get(0));
			}
		}
	}

	boolean canReadBinaryOp() {
		return this.peek() instanceof ExpressionToken;
	}

	/**
	 * Executes following reduce steps:
	 * <ul>
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
	void visitBinaryOperator(BinaryOp binaryOp) {
		// reduce the expressions to the needed priority level
		ExpressionToken left = this.expressionReducePop(binaryOp.getPriority());
		// stack[ {'a', '*'}, 'b'], token = '+' -> stack[], left = {'a', '*', 'b'}
		//                                      -> stack[{{'a', '*', 'b'}, '+'}]
		// stack[ {'a', '+'}, 'b'], token = '+' -> stack[], left = {'a', '+', 'b'}
		//                                      -> stack[{{'a', '+', 'b'}, '+'}]
		// stack[ {     '-'}, 'b'], token = '+' -> stack[], left = {'-', 'b'}
		//                                      -> stack[{{     '-', 'b'}, '+'}]

		// stack[ {'a', '+'}, 'b'], token = '*' -> stack[{'a', '+'}], left = {'b'}
		//                                      -> stack[{'a', '+'}, {'b', '*'}]

		this.stack.add(new PartialBinaryExpressionToken(left, binaryOp));
	}

	void visitUnaryOperator(UnaryOp unaryOp) {
		this.push(new UnaryOperatorToken(unaryOp));
	}

	ExpressionToken getFinal(int endIndex) throws ParseException {
		if (!this.stack.isEmpty()) {
			if (this.peek() instanceof ExpressionToken) {
				ExpressionToken result = this.expressionReducePop();

				if (this.stack.isEmpty()) {
					return result;
				}

				if (this.peek() instanceof UnfinishedArgsToken) {
					throw new MissingTokenException("Expected a closing bracket", endIndex);
				} else {
					throw new UnexpectedTokenException(
							"The stack of tokens isn't empty at the end of the expression: " + this.stack +
									" top: " + result, endIndex);
				}
			} else {
				Token top = this.peek();
				if (top instanceof UnfinishedArgsToken) {
					throw new MissingTokenException("Expected a closing bracket", endIndex);
				} else if (top instanceof PriorityOperatorToken) {
					throw new MissingTokenException(
							"Expected a identifier, constant or subexpression on the right side of the operator",
							endIndex);
				} else {
					throw new UnexpectedTokenException(
							"The stack of tokens contains an unexpected token at the top: " + this.stack,
							endIndex);
				}
			}
		} else {
			throw new MissingTokenException("The input seems to be empty", endIndex);
		}
	}

	public static ExpressionToken parse(String input, ParserOptions options) throws ParseException {
		return Tokenizer.parse(input, options);
	}
}
