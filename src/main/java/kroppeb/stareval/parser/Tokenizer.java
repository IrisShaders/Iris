package kroppeb.stareval.parser;


import kroppeb.stareval.exception.*;
import kroppeb.stareval.token.*;
import net.minecraft.client.util.CharPredicate;
import org.jetbrains.annotations.NotNull;


public class Tokenizer {
	private final ParserOptions options;
	private final ParserOptions.ParserParts parserParts;

	public Tokenizer(ParserOptions options) {
		this.options = options;
		this.parserParts = options.getParserParts();
	}

	public ExpressionToken parse(String input) throws ParseException {
		return this.parseInternal(new StringReader(input));
	}

	private ExpressionToken parseInternal(StringReader input) throws ParseException {
		// parser stack
		final Parser stack = new Parser();


		while (input.canRead()) {
			char c = input.read();

			if (this.parserParts.isIdStart(c)) {
				Token token = this.parseIdGroup(input);
				stack.push(token);
			} else if (this.parserParts.isNumberStart(c)) {
				// start parsing a number
				final String numberString = readWhile(input, this.parserParts::isNumberPart);
				stack.push(new NumberToken(numberString));
			} else if (c == '(') {
				stack.push(new UnfinishedArgsToken());
			} else if (c == ',') {
				if (stack.peek() instanceof ExpressionToken) {
					stack.commaReduce(input.getCurrentIndex());
				} else {
					throw new UnexpectedTokenException("Expected an expression before a comma ','", input.getCurrentIndex());
				}
			} else if (c == ')') {
				stack.bracketReduce(input.getCurrentIndex());
			} else {
				if (stack.peek() instanceof ExpressionToken) {
					// maybe binary operator
					OpResolver<? extends BinaryOp> resolver = this.options.getBinaryOpResolver(c);

					if (resolver != null) {
						stack.push(new BinaryOperatorToken(resolver.resolve(input)));
						continue;
					}
				} else {
					// maybe unary operator
					OpResolver<? extends UnaryOp> resolver = this.options.getUnaryOpResolver(c);

					if (resolver != null) {
						stack.push(new UnaryOperatorToken(resolver.resolve(input)));
						continue;
					}
				}

				throw new UnexpectedCharacterException(c, input.getCurrentIndex());
			}
		}

		if (!stack.stack.isEmpty()) {
			if (stack.peek() instanceof ExpressionToken) {
				ExpressionToken result = stack.expressionReducePop();

				if (stack.stack.isEmpty()) {
					return result;
				}

				if (stack.peek() instanceof UnfinishedArgsToken) {
					throw new MissingTokenException("Expected a closing bracket", input.getCurrentIndex());
				} else {
					throw new UnexpectedTokenException(
							"The stack of tokens isn't empty at the end of the expression: " + stack.stack +
									" top: " + result, input.getCurrentIndex());
				}
			} else {
				Token top = stack.peek();
				if (top instanceof UnfinishedArgsToken) {
					throw new MissingTokenException("Expected a closing bracket", input.getCurrentIndex());
				} else if (top instanceof PriorityOperatorToken) {
					throw new MissingTokenException(
							"Expected a identifier, constant or subexpression on the right side of the operator",
							input.getCurrentIndex());
				} else {
					throw new UnexpectedTokenException(
							"The stack of tokens contains an unexpected token at the top: " + stack.stack,
							input.getCurrentIndex());
				}
			}
		} else {
			throw new MissingTokenException("The input seems to be empty", input.getCurrentIndex());
		}
	}

	@NotNull
	private Token parseIdGroup(StringReader input) throws ParseException {
		final String id = readWhile(input, this.parserParts::isIdPart);
		AccessableToken token = new IdToken(id);

		if (!input.canRead()) {
			return token;
		}


		while (input.tryRead('.')) {
			if (input.canRead()) {
				char start = input.read();
				if (!this.parserParts.isAccessStart(start)) {
					throw new UnexpectedCharacterException("a valid accessor", start, input.getCurrentIndex());
				}

				token = new AccessToken(token, readWhile(input, this.parserParts::isAccessPart));
			} else {
				throw new UnexpectedEndingException("An expression can't end with '.'");
			}
		}

		return token;
	}

	/**
	 * The returned value includes the last value btw;
	 */
	private static String readWhile(StringReader input, CharPredicate predicate) {
		input.mark();

		while (input.canRead()) {
			if (!predicate.test(input.peek())) {
				break;
			}

			input.skip();
		}

		return input.substring();
	}
}


