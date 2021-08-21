package kroppeb.stareval.parser;

import kroppeb.stareval.element.AccessibleExpression;
import kroppeb.stareval.element.Element;
import kroppeb.stareval.element.Expression;
import kroppeb.stareval.element.PriorityOperatorElement;
import kroppeb.stareval.element.tree.*;
import kroppeb.stareval.element.tree.partial.PartialBinaryExpressionToken;
import kroppeb.stareval.element.tree.partial.UnfinishedArgsExpression;
import kroppeb.stareval.exception.MissingTokenException;
import kroppeb.stareval.exception.ParseException;
import kroppeb.stareval.exception.UnexpectedTokenException;
import kroppeb.stareval.element.token.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
	private final List<Element> stack = new ArrayList<>();

	Parser() {
	}

	private Element peek() {
		if (!this.stack.isEmpty()) {
			return this.stack.get(this.stack.size() - 1);
		}

		return null;
	}

	private Element pop() {
		if (this.stack.isEmpty()) {
			throw new IllegalStateException("Internal token stack is empty");
		}

		return this.stack.remove(this.stack.size() - 1);
	}

	private void push(Element element) {
		this.stack.add(element);
	}

	/**
	 * @throws ClassCastException if the top is not an expression
	 * @see #expressionReducePop(int)
	 */
	private Expression expressionReducePop() {
		return this.expressionReducePop(Integer.MAX_VALUE);
	}

	/**
	 * Pops an expression after trying to reduce the stack.
	 * Executes following reduce steps:
	 * <ul>
	 *     <li>{@link PriorityOperatorElement}, {@link Expression} => {@link Expression}
	 *     as long as the {@code priority} of the {@link PriorityOperatorElement} is stricter than the given priority</li>
	 * </ul>
	 *
	 * @throws ClassCastException if the top is not an expression
	 */
	private Expression expressionReducePop(int priority) {
		Expression token = (Expression) this.pop();

		while (!this.stack.isEmpty()) {
			Element x = this.peek();

			if (x instanceof PriorityOperatorElement && ((PriorityOperatorElement) x).getPriority() <= priority) {
				this.pop();
				token = ((PriorityOperatorElement) x).resolveWith(token);
			} else {
				break;
			}
		}

		return token;
	}

	/**
	 * Executes following reduce step:
	 * <ul>
	 *     <li>{@link UnfinishedArgsExpression}, {@link #expressionReducePop} => {@link UnfinishedArgsExpression}</li>
	 * </ul>
	 *
	 * @param index the current reader index, for exception throwing.
	 * @throws ClassCastException if the top is not an expression
	 */
	private void commaReduce(int index) throws ParseException {
		// ( expr,
		// UnfinishedArgs Expression (commaReduce)
		// => UnfinishedArgs

		Expression expr = this.expressionReducePop();
		Element args = this.peek();

		if (args == null) {
			throw new MissingTokenException(
					"Expected an opening bracket '(' before seeing a comma ',' or closing bracket ')'",
					index
			);
		}

		if (args instanceof UnfinishedArgsExpression) {
			((UnfinishedArgsExpression) args).tokens.add(expr);
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
		return this.peek() instanceof AccessibleExpression;
	}

	/**
	 * Assumes `canReadAccess` has returned true
	 */
	void visitAccess(String access) {
		AccessibleExpression pop = (AccessibleExpression) this.pop();
		this.push(new AccessExpression(pop, access));
	}

	void visitNumber(String numberString) {
		this.push(new NumberToken(numberString));
	}

	void visitOpeningParenthesis() {
		this.push(new UnfinishedArgsExpression());
	}

	void visitComma(int index) throws ParseException {
		if (this.peek() instanceof Expression) {
			this.commaReduce(index);
		} else {
			throw new UnexpectedTokenException("Expected an expression before a comma ','", index);
		}
	}

	/**
	 * Allows for trailing comma.
	 * Executes following reduce steps:
	 * <ul>
	 *     <li>{@link IdToken} {@link UnfinishedArgsExpression}, {@link #expressionReducePop} => {@link FunctionCall}</li>
	 *     <li>{@link IdToken} {@link UnfinishedArgsExpression} => {@link FunctionCall}</li>
	 *     <li>{@link UnfinishedArgsExpression}, {@link #expressionReducePop} => {@link Expression}</li>
	 * </ul>
	 *
	 * @param index the current reader index, for exception throwing.
	 */
	void visitClosingParenthesis(int index) throws ParseException {
		//
		// ( ... )
		// UnfinishedArgsExpression Expression? (callReduce)

		boolean expressionOnTop = this.peek() instanceof Expression;
		if (expressionOnTop) {
			this.commaReduce(index);
		}

		UnfinishedArgsExpression args;
		{
			if (this.stack.isEmpty()) {
				throw new MissingTokenException("A closing bracket ')' can't be the first character of an expression", index);
			}

			Element pop = this.pop();

			if (!(pop instanceof UnfinishedArgsExpression)) {
				throw new UnexpectedTokenException(
						"Expected to see an opening bracket '(' or a comma ',' right before an expression followed by a " +
								"closing bracket ')' or a comma ','", index);
			}
			args = (UnfinishedArgsExpression) pop;
		}

		Element top = this.peek();

		if (top instanceof IdToken) {
			this.pop();
			this.push(new FunctionCall(((IdToken) top).id, args.tokens));
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
		return this.peek() instanceof Expression;
	}

	/**
	 * Executes following reduce steps:
	 * <ul>
	 *     <li>{@link Expression} | {@link BinaryOperatorToken} => {@link PartialBinaryExpressionToken}</li>
	 *     <li>{@link UnaryOperatorToken}, {@link Expression} | {@link BinaryOperatorToken} => {@link UnaryExpression} | {@link BinaryOperatorToken}</li>
	 *     <li>
	 *         {@link PartialBinaryExpressionToken}, {@link Expression} | {@link BinaryOperatorToken} <br/>
	 *         where the operator on the stack has a higher or equal priority to the one being added, the 3 items on the
	 *         stack get popped, merged to a {@link BinaryExpression} and placed on the stack.
	 *         The new token is then pushed again.
	 *     </li>
	 * </ul>
	 */
	void visitBinaryOperator(BinaryOp binaryOp) {
		// reduce the expressions to the needed priority level
		Expression left = this.expressionReducePop(binaryOp.getPriority());
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

	Expression getFinal(int endIndex) throws ParseException {
		if (!this.stack.isEmpty()) {
			if (this.peek() instanceof Expression) {
				Expression result = this.expressionReducePop();

				if (this.stack.isEmpty()) {
					return result;
				}

				if (this.peek() instanceof UnfinishedArgsExpression) {
					throw new MissingTokenException("Expected a closing bracket", endIndex);
				} else {
					throw new UnexpectedTokenException(
							"The stack of tokens isn't empty at the end of the expression: " + this.stack +
									" top: " + result, endIndex);
				}
			} else {
				Element top = this.peek();
				if (top instanceof UnfinishedArgsExpression) {
					throw new MissingTokenException("Expected a closing bracket", endIndex);
				} else if (top instanceof PriorityOperatorElement) {
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

	public static Expression parse(String input, ParserOptions options) throws ParseException {
		return Tokenizer.parse(input, options);
	}
}
