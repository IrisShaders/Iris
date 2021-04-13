package kroppeb.stareval.parser;


import kroppeb.stareval.token.AccessToken;
import kroppeb.stareval.token.AccessableToken;
import kroppeb.stareval.token.BinaryOperatorToken;
import kroppeb.stareval.token.ExpressionToken;
import kroppeb.stareval.token.IdToken;
import kroppeb.stareval.token.NumberToken;
import kroppeb.stareval.token.Token;
import kroppeb.stareval.token.UnaryOperatorToken;
import kroppeb.stareval.token.UnfinishedArgsToken;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.util.CharPredicate;


public class Parser {
	final ParserOptions options;
	final ParserOptions.ParserParts parserParts;
	
	public Parser(ParserOptions options) {
		this.options = options;
		this.parserParts = options.parserParts;
	}
	
	public ExpressionToken parse(final String input) throws Exception {
		return parseInternal(new StringReader(input));
	}
	
	ExpressionToken parseInternal(final StringReader input) throws Exception {
		// parser stack
		final TokenStack stack = new TokenStack();
		
		
		while (input.canRead()) {
			char c = input.read();
			
			if (parserParts.isIdStart(c)) {
				Token token = parseIdGroup(input);
				stack.push(token);
			} else if (parserParts.isNumberStart(c)) {
				// start parsing a number
				final String numberString = readWhile(input, parserParts::isNumberPart);
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
					OpResolver<BinaryOp> resolver = options.binaryOpResolvers.get(c);
					if (resolver != null) {
						stack.push(new BinaryOperatorToken(resolver.check(input)));
						continue;
					}
				} else {
					// maybe unary operator
					OpResolver<UnaryOp> resolver = options.unaryOpResolvers.get(c);
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

		if(!input.canRead()) {
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


