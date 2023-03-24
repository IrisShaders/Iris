package net.coderbot.iris.test.custom_uniforms;

import kroppeb.stareval.element.ExpressionElement;
import kroppeb.stareval.exception.ParseException;
import kroppeb.stareval.expression.ConstantExpression;
import kroppeb.stareval.expression.Expression;
import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import kroppeb.stareval.parser.Parser;
import kroppeb.stareval.resolver.ExpressionResolver;
import net.coderbot.iris.parsing.IrisFunctions;
import net.coderbot.iris.parsing.IrisOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class IrisFunctionsTest {
	@Test
	void testIf() throws ParseException {
		trivialTest(Type.Boolean, "if(1, 1, 0)", result -> {
			Assertions.assertTrue(result.booleanReturn);
		});
	}

	@Test
	void testIfTF() throws ParseException {
		trivialTest(Type.Boolean, "if(true, true, false)", result -> {
			Assertions.assertTrue(result.booleanReturn);
		});
	}

	@Test
	void testAdd() throws ParseException {
		trivialTest(Type.Int, "1 + 1", result -> {
			Assertions.assertEquals(result.intReturn, 2);
		});
	}

	@Test
	void testAnd() throws ParseException {
		trivialTest(Type.Boolean, "1 && 0", result -> {
			Assertions.assertFalse(result.booleanReturn);
		});
	}

	@Test
	void testAndTF() throws ParseException {
		trivialTest(Type.Boolean, "true && false", result -> {
			Assertions.assertFalse(result.booleanReturn);
		});
	}

	private static void trivialTest(Type outputType, String expressionStr, Consumer<FunctionReturn> verifier) throws ParseException {
		ExpressionElement element = Parser.parse(expressionStr, IrisOptions.options);
		ExpressionResolver resolver = new ExpressionResolver(IrisFunctions.functions, x -> {
			if (x.equals("true") || x.equals("false")) {
				return Type.Boolean;
			} else {
				return null;
			}
		}, true);

		FunctionReturn functionReturn = new FunctionReturn();
		FunctionContext context = new FunctionContext() {
			@Override
			public Expression getVariable(String name) {
				if (name.equals("true")) {
					return new ConstantExpression(Type.Boolean) {
						@Override
						public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
							functionReturn.booleanReturn = true;
						}
					};
				} else if (name.equals("false")) {
					return new ConstantExpression(Type.Boolean) {
						@Override
						public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
							functionReturn.booleanReturn = false;
						}
					};
				}

				return null;
			}

			@Override
			public boolean hasVariable(String name) {
				return name.equals("false") || name.equals("true");
			}
		};

		Expression expression = resolver.resolveExpression(outputType, element);
		expression.evaluateTo(context, functionReturn);

		verifier.accept(functionReturn);
	}
}
