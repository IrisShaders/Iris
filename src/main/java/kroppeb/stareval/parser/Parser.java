package kroppeb.stareval.parser;


import kroppeb.stareval.token.*;
import net.minecraft.client.util.CharPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Parser {
	final List<Token> stack = new ArrayList<>();
	
	private Token peek() {
		if (stack.size() > 0)
			return stack.get(stack.size() - 1);
		return null;
	}
	
	private Token peek(int offset) {
		if (stack.size() > offset)
			return stack.get(stack.size() - 1 - offset);
		return null;
	}
	
	private void pop(int count) throws Exception {
		for (int i = 0; i < count; i++) {
			pop();
		}
	}
	
	private Token pop() throws Exception {
		if (stack.isEmpty()) {
			throw new Exception();
		}
		return stack.remove(stack.size() - 1);
	}
	
	private void push(Token token) throws Exception {
		Token top = this.peek();
		if (token instanceof ExpressionToken) {
			if (token instanceof ArgsToken && top instanceof CallBaseToken) {
				pop();
				push(new CallToken(((CallBaseToken) top).id, ((ArgsToken) token).tokens));
				return;
			}
			if (top instanceof UnaryOperatorToken) {
				this.pop();
				token = new UnaryExpressionToken(((UnaryOperatorToken) top).op, (ExpressionToken) token);
			}
		} else if (token instanceof BinaryOperatorToken) {
			assert peek() instanceof ExpressionToken;
			
			if (stack.size() >= 3) {
				Token other = this.peek(1);
				if (other instanceof BinaryOperatorToken &&
						((BinaryOperatorToken) other).op.priority < ((BinaryOperatorToken) token).op.priority) {
					// a * b +
					ExpressionToken b = (ExpressionToken) this.pop();
					this.pop();
					ExpressionToken a = (ExpressionToken) this.pop();
					this.push(new BinaryExpressionToken(((BinaryOperatorToken) other).op, a, b));
				}
			}
			
			if (stack.size() >= 3) {
				Token other = this.peek(1);
				if (other instanceof BinaryOperatorToken &&
						((BinaryOperatorToken) other).op.priority == ((BinaryOperatorToken) token).op.priority) {
					// a * b +
					ExpressionToken b = (ExpressionToken) this.pop();
					this.pop();
					ExpressionToken a = (ExpressionToken) this.pop();
					this.push(new BinaryExpressionToken(((BinaryOperatorToken) other).op, a, b));
				}
			}
		}
		
		this.stack.add(token);
	}
	
	private ExpressionToken expressionReducePop() throws Exception {
		Token top = pop();
		assert top instanceof ExpressionToken;
		while (stack.size() >= 2) {
			Token x = peek(0);
			Token a = peek(1);
			
			
			if (x instanceof BinaryOperatorToken) {
				// a should have been asserted when x got added
				assert a instanceof ExpressionToken;
				
				pop(2);
				top = new BinaryExpressionToken(((BinaryOperatorToken) x).op, (ExpressionToken) a,
						(ExpressionToken) top);
			} else {
				break;
			}
		}
		return (ExpressionToken) top;
	}
	
	private void commaReduce() throws Exception {
		// ( expr,
		// UnfinishedArgs Expression (commaReduce)
		// => UnfinishedArgs
		
		ExpressionToken expr = expressionReducePop();
		UnfinishedArgsToken args = (UnfinishedArgsToken) this.peek();
		if (args == null) {
			throw new Exception();
		}
		args.tokens.add(expr);
	}
	
	private void bracketReduce() throws Exception {
		// allows for trailing comma
		// ( ... )
		// UnfinishedArgsToken Expression? (callReduce)
		
		if (this.peek() instanceof ExpressionToken) {
			this.commaReduce();
		}
		
		UnfinishedArgsToken args = (UnfinishedArgsToken) this.pop();
		
		this.push(new ArgsToken(args.tokens));
	}
	
	
	public static ExpressionToken parse(final String input, final ParserOptions options) throws Exception {
		return parseInternal(new StringReader(input), options);
	}
	
	static ExpressionToken parseInternal(final StringReader input, final ParserOptions options) throws Exception {
		// parser state
		final Parser state = new Parser();

outerLoop:
		while (input.canRead()) {
			char c = input.read();
			
			if (isIdStart(c)) {
				Token token = parseIdGroup(input);
				state.push(token);
			} else if (isNumberStart(c)) {
				// start parsing a number
				final String numberString = readWhile(input, Parser::isNumberPart);
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
	private static Token parseIdGroup(StringReader input) throws Exception {
		final String id = readWhile(input, Parser::isIdPart);
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
					if (!isAccessStart(input.read())) {
						throw new Exception("expected a valid access");
					}
					
					token = new AccessToken(token, readWhile(input, Parser::isAccessPart));
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
	
	static boolean isNumber(final char c) {
		return c >= '0' && c <= '9';
	}
	
	static boolean isLowerCaseLetter(final char c) {
		return c >= 'a' && c <= 'z';
	}
	
	static boolean isUpperCaseLetter(final char c) {
		return c >= 'A' && c <= 'Z';
	}
	
	static boolean isLetter(final char c) {
		return isLowerCaseLetter(c) || isUpperCaseLetter(c);
	}
	
	static boolean isIdStart(final char c) {
		return isLetter(c) || c == '_';
	}
	
	static boolean isIdPart(final char c) {
		return isIdStart(c) || isNumber(c);
	}
	
	static boolean isNumberStart(final char c) {
		return isNumber(c) || c == '.';
	}
	
	static boolean isNumberPart(final char c) {
		return isNumberStart(c) || isLetter(c);
	}
	
	static boolean isAccessStart(final char c) {
		return isIdStart(c) || isNumber(c);
	}
	
	static boolean isAccessPart(final char c) {
		return isAccessStart(c);
	}
}
