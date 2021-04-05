package kroppeb.stareval.parser;


import kroppeb.stareval.token.*;
import net.minecraft.client.util.CharPredicate;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
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
		// parser state
		final ParserState state = new ParserState();
		
		
		while (input.canRead()) {
			char c = input.read();
			
			if (parserParts.isIdStart(c)) {
				Token token = parseIdGroup(input);
				state.push(token);
			} else if (parserParts.isNumberStart(c)) {
				// start parsing a number
				final String numberString = readWhile(input, parserParts::isNumberPart);
				state.push(new NumberToken(numberString));
			} else if (c == '(') {
				state.push(new UnfinishedArgsToken());
			} else if (c == ',') {
				state.commaReduce();
			} else if (c == ')') {
				state.bracketReduce();
			} else {
				if (state.peek() instanceof ExpressionToken) {
					// maybe binary operator
					OpResolver<BinaryOp> resolver = options.binaryOpResolvers.get(c);
					if (resolver != null) {
						state.push(new BinaryOperatorToken(resolver.check(input)));
						continue;
					}
				} else {
					// maybe unary operator
					OpResolver<UnaryOp> resolver = options.unaryOpResolvers.get(c);
					if (resolver != null) {
						state.push(new UnaryOperatorToken(resolver.check(input)));
						continue;
					}
				}
				
				throw new Exception("unknown char: '" + c + "'");
			}
		}
		ExpressionToken result = state.expressionReducePop();
		if (!state.stack.isEmpty()) {
			throw new Exception("stack isn't empty: " + state.stack + " top: " + result);
		}
		
		return result;
	}
	
	@NotNull
	private Token parseIdGroup(StringReader input) throws Exception {
		final String id = readWhile(input, this.parserParts::isIdPart);
		if(!input.canRead())
			return new IdToken(id);
		
		char c = input.peek();
		
		if (c == '(') {
			// TODO do we really need to make this a separate token?
			//		I think I did it cause something with the access
			return new CallBaseToken(id);
		}
		
		AccessableToken token = new IdToken(id);
		
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


