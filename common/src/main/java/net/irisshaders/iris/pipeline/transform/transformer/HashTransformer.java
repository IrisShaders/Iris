package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.UnaryExpression;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.parser.ParseShape;

import java.util.List;

// This replaces the fract(sin(dot(input, vec2Constant)) * largeConstant) which is used to generate a random hash.
// On NVIDIA Vulkan, this causes all sorts of issues (such as BSL's waving animation breaking), so it's replaced with an integer hash.
public class HashTransformer {
	private static final AutoHintedMatcher<Expression> sineHash = new AutoHintedMatcher<>("fract(sin(dot(input, vec2(a, b))) * scale)",
		ParseShape.EXPRESSION) {
		{
			markClassWildcard("input", pattern.getRoot().identifierIndex.getUnique("input").getAncestor(ReferenceExpression.class),
				Expression.class);
			for (String name : List.of("a", "b", "scale")) {
				markClassedPredicateWildcard(name, pattern.getRoot().identifierIndex.getUnique(name).getAncestor(ReferenceExpression.class),
					Expression.class,
					expression -> name.equals("scale") ? number(expression) > 100.0 : Double.isFinite(number(expression)));
			}
		}
	};

	public static void transform(ASTParser t, TranslationUnit tree, Root root) {
		boolean replaced = false;
		for (Identifier identifier : root.identifierIndex.getStream("fract").toList()) {
			if (!(identifier.getParent() instanceof FunctionCallExpression call) || !sineHash.matchesExtract(call)) {
				continue;
			}
			Expression input = sineHash.getNodeMatch("input", Expression.class);
			FunctionCallExpression replacement = (FunctionCallExpression) t.parseExpression(root, "iris_hash21()");
			input.detach();
			replacement.getParameters().add(input);
			call.replaceByAndDelete(replacement);
			replaced = true;
		}
		if (replaced) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, """
				float iris_hash21(vec2 p) {
				    uvec2 q = floatBitsToUint(p);
				    uint h = q.x * 3266489917u + q.y;
				    h = h * 747796405u + 2891336453u;
				    h = ((h >> 15u) ^ h) * 277803737u;
				    h = ((h >> 22u) ^ h);
				    return float(h) * (1.0 / 4294967295.0);
				}
				""");
		}
	}

	private static double number(Expression expression) {
		if (expression instanceof LiteralExpression literal) {
			return literal.isFloatingPoint() ? literal.getFloating() : literal.isInteger() ? literal.getInteger() : Double.NaN;
		}
		if (expression instanceof UnaryExpression unary) {
			return switch (expression.getExpressionType()) {
				case GROUPING, IDENTITY -> number(unary.getOperand());
				case NEGATION -> -number(unary.getOperand());
				default -> Double.NaN;
			};
		}
		return Double.NaN;
	}
}
