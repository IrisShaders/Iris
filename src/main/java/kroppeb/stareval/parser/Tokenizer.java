package kroppeb.stareval.parser;


import kroppeb.stareval.exception.ParseException;
import kroppeb.stareval.exception.UnexpectedCharacterException;
import kroppeb.stareval.exception.UnexpectedEndingException;
import kroppeb.stareval.element.Expression;
import net.minecraft.client.util.CharPredicate;


class Tokenizer {
	private Tokenizer() {
	}

	static Expression parse(String input, ParserOptions options) throws ParseException {
		return parseInternal(new StringReader(input), options);
	}

	static Expression parseInternal(StringReader input, ParserOptions options) throws ParseException {
		// parser stack
		final Parser stack = new Parser();
		ParserOptions.ParserParts parserParts = options.getParserParts();

		while (input.canRead()) {
			char c = input.read();

			if (parserParts.isIdStart(c)) {
				final String id = readWhile(input, parserParts::isIdPart);
				stack.visitId(id);
			} else if (c == '.' && stack.canReadAccess()) {
				if (input.canRead()) {
					char start = input.read();

					if (!parserParts.isAccessStart(start)) {
						throw new UnexpectedCharacterException("a valid accessor", start, input.getCurrentIndex());
					}

					final String access = readWhile(input, parserParts::isAccessPart);
					stack.visitAccess(access);
				} else {
					throw new UnexpectedEndingException("An expression can't end with '.'");
				}
			} else if (parserParts.isNumberStart(c)) {
				// start parsing a number
				final String numberString = readWhile(input, parserParts::isNumberPart);
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
					OpResolver<? extends BinaryOp> resolver = options.getBinaryOpResolver(c);

					if (resolver != null) {
						stack.visitBinaryOperator(resolver.resolve(input));
						continue;
					}
				} else {
					// maybe unary operator
					OpResolver<? extends UnaryOp> resolver = options.getUnaryOpResolver(c);

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


