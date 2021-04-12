package kroppeb.stareval.parser;

import java.util.ArrayList;
import java.util.List;

import kroppeb.stareval.token.ArgsToken;
import kroppeb.stareval.token.BinaryExpressionToken;
import kroppeb.stareval.token.BinaryOperatorToken;
import kroppeb.stareval.token.CallBaseToken;
import kroppeb.stareval.token.CallToken;
import kroppeb.stareval.token.ExpressionToken;
import kroppeb.stareval.token.Token;
import kroppeb.stareval.token.UnaryExpressionToken;
import kroppeb.stareval.token.UnaryOperatorToken;
import kroppeb.stareval.token.UnfinishedArgsToken;

class ParserState {
	final List<Token> stack = new ArrayList<>();
	
	Token peek() {
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
	
	void push(Token token) throws Exception {
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
	
	ExpressionToken expressionReducePop() throws Exception {
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
	
	void commaReduce() throws Exception {
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
	
	void bracketReduce() throws Exception {
		// allows for trailing comma
		// ( ... )
		// UnfinishedArgsToken Expression? (callReduce)
		
		if (this.peek() instanceof ExpressionToken) {
			this.commaReduce();
		}
		
		UnfinishedArgsToken args = (UnfinishedArgsToken) this.pop();
		
		this.push(new ArgsToken(args.tokens));
	}
}
