package kroppeb.stareval.parser;


import kroppeb.stareval.token.*;
import net.minecraft.client.util.CharPredicate;
import org.jetbrains.annotations.NotNull;


public class Parser {
	private final ParserOptions options;
	private final ParserOptions.ParserParts parserParts;

	public Parser(ParserOptions options) {
		this.options = options;
		this.parserParts = options.getParserParts();
	}

	public ExpressionToken parse(String input) throws Exception {
		return this.parseInternal(new StringReader(input));
	}

	private ExpressionToken parseInternal(StringReader input) throws Exception {
		// parser stack
		final TokenStack stack = new TokenStack();


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
				stack.commaReduce();
			} else if (c == ')') {
				stack.bracketReduce();
			} else {
				if (stack.peek() instanceof ExpressionToken) {
					// maybe binary operator
					OpResolver<BinaryOp> resolver = this.options.getBinaryOpResolver(c);
					if (resolver != null) {
						stack.push(new BinaryOperatorToken(resolver.check(input)));
						continue;
					}
				} else {
					// maybe unary operator
					OpResolver<UnaryOp> resolver = this.options.getUnaryOpResolver(c);
					if (resolver != null) {
						stack.push(new UnaryOperatorToken(resolver.check(input)));
						continue;
					}
				}

				throw new Exception("unknown char: '" + c + "'");
			}
		}

		ExpressionToken result = stack.expressionReducePop();
		if (!stack.stack.isEmpty()) {
			throw new Exception("stack isn't empty: " + stack.stack + " top: " + result);
		}

		return result;
	}

	@NotNull
	private Token parseIdGroup(StringReader input) throws Exception {
		final String id = readWhile(input, this.parserParts::isIdPart);
		AccessableToken token = new IdToken(id);

		if (!input.canRead()) {
			return token;
		}

		char c = input.peek();

		if (c == '.') {
			do {
				input.skip();
				if (input.canRead()) {
					if (!this.parserParts.isAccessStart(input.read())) {
						throw new Exception("expected a valid access");
					}

					token = new AccessToken(token, readWhile(input, this.parserParts::isAccessPart));
				} else {
					throw new Error("can't end with '.'");
				}
			} while (input.canRead() && input.peek() == '.');
		}
		return token;
	}

	/**
	 * The returned value add the last value btw;
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


