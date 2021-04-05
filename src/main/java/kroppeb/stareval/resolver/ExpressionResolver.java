package kroppeb.stareval.resolver;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kroppeb.stareval.expression.CallExpression;
import kroppeb.stareval.expression.ConstantExpression;
import kroppeb.stareval.expression.Expression;
import kroppeb.stareval.function.*;
import kroppeb.stareval.token.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExpressionResolver {
	private final FunctionResolver functionResolver;
	private final Function<String, Type> variableTypeMap;
	private final boolean enableDebugging;
	
	private List<String> logs;
	
	public ExpressionResolver(FunctionResolver functionResolver, Function<String, Type> variableTypeMap) {
		this(functionResolver, variableTypeMap, false);
	}
	
	public ExpressionResolver(FunctionResolver functionResolver, Function<String, Type> variableTypeMap, boolean enableDebugging) {
		this.functionResolver = functionResolver;
		this.variableTypeMap = variableTypeMap;
		this.enableDebugging = enableDebugging;
	}
	
	
	public Expression resolveExpression(Type targetType, ExpressionToken expression) {
		clearLogs();
		Expression result = resolveExpressionInternal(targetType, expression, true, true);
		if (result != null) {
			return result;
		}
		throw new RuntimeException("Couldn't resolve: \n" + String.join("\n", extractLogs()));
	}
	
	Expression resolveCallExpressionInternal(
			Type targetType,
			String name,
			List<ExpressionToken> inner,
			boolean implicit
	) {
		int innerLength = inner.size();
		Expression result = null;
functions:
		for (TypedFunction f : this.functionResolver.resolve(name, targetType)) {
			Type[] paramTypes = f.getParameterTypes();
			if (paramTypes.length != innerLength)
				continue;
			
			Expression[] params = new Expression[innerLength];
			for (int i = 0; i < innerLength; i++) {
				Expression expression = resolveExpressionInternal(paramTypes[i], inner.get(i),
						!implicit || innerLength > 1, implicit);
				if (expression == null)
					continue functions;
				params[i] = expression;
			}
			// FIXME
			if (result != null)
				throw new RuntimeException("Ambiguity");
			
			result = new CallExpression(f, params);
		}
		return result;
	}
	
	
	Expression resolveCallExpression(
			Type targetType,
			String name,
			List<ExpressionToken> inner,
			boolean allowNonImplicit,
			boolean allowImplicit) {
		
		log("[DEBUG] resolving function %s with args %s to type %s",
				name, inner, targetType);
		
		Expression result = null;
		
		if (allowNonImplicit) {
			result = resolveCallExpressionInternal(targetType, name, inner, false);
		}
		
		if (result != null) {
			log("[DEBUG] resolved function %s with args %s to type %s directly",
					name, inner, targetType);
			return result;
		} else if (!allowImplicit) {
			log("[DEBUG] Failed to resolve function %s with args %s to type %s directly",
					name, inner, targetType);
			return null;
		}
		
		List<? extends TypedFunction> casts = this.functionResolver.resolve("<cast>", targetType);
		
		for (TypedFunction f : casts) {
			Expression u = resolveCallExpression(f.getParameterTypes()[0], name, inner, true, true);
			if (u == null)
				continue;
			if (result != null)
				throw new RuntimeException("Ambiguity");
			result = new CallExpression(f, new Expression[]{u});
		}
		if (result != null) {
			log("[DEBUG] resolved function %s with args %s to type %s using only final cast",
					name, inner, targetType);
			return result;
		}
		
		result = resolveCallExpressionInternal(targetType, name, inner, true);
		if(result != null){
			log("[DEBUG] resolved function %s with args %s to type %s using implicit inner casts",
					name, inner, targetType);
		}else{
			log("[DEBUG] failed to resolve function %s with args %s to type %s",
					name, inner, targetType);
		}
		return result;
	}
	
	public List<String> extractLogs() {
		List<String> old = this.logs;
		clearLogs();
		return old;
	}
	
	public void clearLogs() {
		this.logs = new ArrayList<>();
	}
	
	private void log(String str) {
		if (this.enableDebugging)
			this.logs.add(str);
	}
	
	private void log(String str, Object... args) {
		if (this.enableDebugging)
			this.logs.add(String.format(str, args));
	}
	
	private void log(Supplier<String> str) {
		if (this.enableDebugging)
			log(str.get());
	}
	
	private Expression resolveExpressionInternal(
			Type targetType,
			ExpressionToken expression,
			boolean allowNonImplicit,
			boolean allowImplicit) {
		Expression castable;
		Type innerType;
		
		log("[DEBUG] resolving %s to type %s (%d%d)",
				expression, targetType, allowNonImplicit ? 1 : 0, allowImplicit ? 1 : 0);
		if (expression instanceof UnaryExpressionToken) {
			// I want java 15 =(
			UnaryExpressionToken token = (UnaryExpressionToken) expression;
			return this.resolveCallExpression(targetType, token.op.name, Collections.singletonList(token.inner),
					allowNonImplicit, allowImplicit);
		} else if (expression instanceof BinaryExpressionToken) {
			BinaryExpressionToken token = (BinaryExpressionToken) expression;
			return this.resolveCallExpression(targetType, token.op.name, Arrays.asList(token.left, token.right),
					allowNonImplicit, allowImplicit);
		} else if (expression instanceof CallToken) {
			CallToken token = (CallToken) expression;
			return this.resolveCallExpression(targetType, token.id, token.args,
					allowNonImplicit, allowImplicit);
		} else if (expression instanceof AccessToken) {
			AccessToken token = (AccessToken) expression;
			return this.resolveCallExpression(targetType, "<access$" + token.index + ">",
					Collections.singletonList(token.base),
					allowNonImplicit, allowImplicit);
		} else if (expression instanceof NumberToken) {
			NumberToken token = (NumberToken) expression;
			ConstantExpression exp = resolveNumber(token.number);
			if (exp.getType().equals(targetType))
				return exp;
			// TODO: implicit casting is split up too much
			if (!allowImplicit)
				return null;
			castable = exp;
			innerType = exp.getType();
		} else if (expression instanceof IdToken) {
			IdToken token = (IdToken) expression;
			final String name = token.id;
			Type type = this.variableTypeMap.apply(name);
			if (type == null)
				throw new RuntimeException("Unknown variable: " + name);
			if (type.equals(targetType))
				return new Expression() {
					@Override
					public void evaluateTo(FunctionContext c, FunctionReturn r) {
						c.getVariable(name).evaluateTo(c, r);
					}
				};
			if (!allowImplicit)
				return null;
			castable = (c, r) -> c.getVariable(name).evaluateTo(c, r);
			;
			innerType = type;
		} else {
			throw new RuntimeException("unexpected token: " + expression.toString());
		}
		
		List<? extends TypedFunction> casts = this.functionResolver.resolve("<cast>", targetType);
		
		for (TypedFunction f : casts) {
			if (f.getParameterTypes()[0].equals(innerType)) {
				return new CallExpression(f, new Expression[]{castable});
			}
		}
		return null;
	}
	
	private final Map<String, ConstantExpression> numbers = new Object2ObjectOpenHashMap<>();
	
	private ConstantExpression resolveNumber(String s) {
		return numbers.computeIfAbsent(s, str -> {
			NumberFormatException p;
			try {
				final int val;
				if (str.length() >= 2 && str.charAt(0) == '0') {
					switch (str.charAt(1)) {
						case 'b': {
							val = Integer.parseInt(str.substring(2), 2);
							break;
						}
						case 'x': {
							val = Integer.parseInt(str.substring(2), 16);
							break;
						}
						default: {
							val = Integer.parseInt(str.substring(1), 8);
							break;
						}
					}
				} else
					val = Integer.parseInt(str);
				
				return new ConstantExpression(Type.Int) {
					@Override
					public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
						functionReturn.intReturn = val;
					}
				};
			} catch (NumberFormatException ex) {
				p = ex;
			}
			
			try {
				final float res = Float.parseFloat(str);
				return new ConstantExpression(Type.Float) {
					@Override
					public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
						functionReturn.floatReturn = res;
					}
				};
			} catch (NumberFormatException ex) {
				RuntimeException exception = new RuntimeException("Illegal number: " + str, ex);
				exception.addSuppressed(p);
				throw exception;
			}
		});
	}
}
