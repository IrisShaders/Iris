package kroppeb.stareval.parser;

import kroppeb.stareval.element.ExpressionElement;
import kroppeb.stareval.exception.ParseException;
import kroppeb.stareval.exception.UnexpectedCharacterException;
import kroppeb.stareval.exception.UnexpectedEndingException;

class Tokenizer {
	private Tokenizer() {
	}

	static ExpressionElement parse(String input, ParserOptions options) throws ParseException {
		return parseInternal(new StringReader(input), options);
	}

	static ExpressionElement parseInternal(StringReader input, ParserOptions options) throws ParseException {
		// parser stack
		final Parser stack = new Parser();
		ParserOptions.TokenRules tokenRules = options.getTokenRules();

		while (input.canRead()) {
			input.skipWhitespace();

			if (!input.canRead()) {
				break;
			}

			char c = input.read();

			if (tokenRules.isIdStart(c)) {
				final String id = readWhile(input, tokenRules::isIdPart);
				stack.visitId(id);
			} else if (c == '.' && stack.canReadAccess()) {
				input.skipWhitespace();

				if (input.canRead()) {
					char start = input.read();

					if (!tokenRules.isAccessStart(start)) {
						throw new UnexpectedCharacterException("a valid accessor", start, input.getCurrentIndex());
					}

					final String access = readWhile(input, tokenRules::isAccessPart);
					stack.visitAccess(access);
				} else {
					throw new UnexpectedEndingException("An expression can't end with '.'");
				}
			} else if (tokenRules.isNumberStart(c)) {
				// start parsing a number
				final String numberString = readWhile(input, tokenRules::isNumberPart);
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

			// NB: Do not skip whitespace here implicitly.
			input.skipOneCharacter();
		}

		return input.substring();
	}

	private interface CharPredicate {
		boolean test(char c);
	}
}


