package kroppeb.stareval.parser;


import kroppeb.stareval.exception.ParseException;
import kroppeb.stareval.exception.UnexpectedCharacterException;
import kroppeb.stareval.exception.UnexpectedEndingException;
import kroppeb.stareval.token.AccessToken;
import kroppeb.stareval.token.ExpressionToken;
import net.minecraft.client.util.CharPredicate;


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
				final String id = readWhile(input, this.parserParts::isIdPart);
				stack.visitId(id);
			} else if(c == '.' && stack.canReadAccess()){
				if (input.canRead()) {
					char start = input.read();
					if (!this.parserParts.isAccessStart(start)) {
						throw new UnexpectedCharacterException("a valid accessor", start, input.getCurrentIndex());
					}

					final String access = readWhile(input, this.parserParts::isAccessPart);
					stack.visitAccess(access);
				} else {
					throw new UnexpectedEndingException("An expression can't end with '.'");
				}
			} else if (this.parserParts.isNumberStart(c)) {
				// start parsing a number
				final String numberString = readWhile(input, this.parserParts::isNumberPart);
				stack.visitNumber(numberString);
			} else if (c == '(') {
				stack.visitOpeningParenthesis();
			} else if (c == ',') {
				stack.visitComma(input.getCurrentIndex());
			} else if (c == ')') {
				stack.visitClosingParenthesis(input.getCurrentIndex());
			} else {
				if (stack.canReadBinaryOp()) {
					// maybe binary operator
					OpResolver<? extends BinaryOp> resolver = this.options.getBinaryOpResolver(c);

					if (resolver != null) {
						stack.visitBinaryOperator(resolver.resolve(input));
						continue;
					}
				} else {
					// maybe unary operator
					OpResolver<? extends UnaryOp> resolver = this.options.getUnaryOpResolver(c);

					if (resolver != null) {
						stack.visitUnaryOperator(resolver.resolve(input));
						continue;
					}
				}

				throw new UnexpectedCharacterException(c, input.getCurrentIndex());
			}
		}

		return stack.getFinal(input.getCurrentIndex());
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


