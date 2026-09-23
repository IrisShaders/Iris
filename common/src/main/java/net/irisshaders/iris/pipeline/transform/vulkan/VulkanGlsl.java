package net.irisshaders.iris.pipeline.transform.vulkan;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.IterationConditionInitializer;
import io.github.douira.glsl_transformer.ast.node.abstract_node.ASTNode;
import io.github.douira.glsl_transformer.ast.node.declaration.DeclarationMember;
import io.github.douira.glsl_transformer.ast.node.declaration.FunctionParameter;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.BinaryExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.UnaryExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.FunctionDefinition;
import io.github.douira.glsl_transformer.ast.node.statement.CompoundStatement;
import io.github.douira.glsl_transformer.ast.node.statement.loop.LoopStatement;
import io.github.douira.glsl_transformer.ast.node.type.initializer.ExpressionInitializer;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.LayoutQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.NamedLayoutQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.ArraySpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.util.Type;
import net.irisshaders.iris.pipeline.transform.Std140Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class VulkanGlsl {
	private VulkanGlsl() {
	}

	public static boolean hasStorage(TypeQualifier qualifier, StorageQualifier.StorageType storage) {
		return qualifier != null && qualifier.getParts().stream()
			.anyMatch(p -> p instanceof StorageQualifier s && s.storageType == storage);
	}

	public static NamedLayoutQualifierPart layout(TypeQualifier qualifier, String name) {
		if (qualifier == null) {
			return null;
		}

		return qualifier.getParts().stream().filter(LayoutQualifier.class::isInstance).map(LayoutQualifier.class::cast)
			.flatMap(q -> q.getParts().stream()).filter(NamedLayoutQualifierPart.class::isInstance)
			.map(NamedLayoutQualifierPart.class::cast).filter(p -> p.getName().getName().equals(name)).findFirst().orElse(null);
	}

	public static void removeLayout(TypeQualifier qualifier, String... names) {
		Set<String> removed = Set.of(names);

		for (var part : List.copyOf(qualifier.getParts())) {
			if (part instanceof LayoutQualifier layout) {
				for (var item : List.copyOf(layout.getParts())) {
					if (item instanceof NamedLayoutQualifierPart named && removed.contains(named.getName().getName())) {
						item.detachAndDelete();
					}
				}

				if (layout.getParts().isEmpty()) {
					layout.detachAndDelete();
				}
			}
		}
	}

	public static void setLayout(TypeQualifier qualifier, String name, int value) {
		removeLayout(qualifier, name);
		qualifier.getParts().add(0,
			new LayoutQualifier(Stream.of(new NamedLayoutQualifierPart(new Identifier(name), new LiteralExpression(Type.INT32, value)))));
	}

	public static int integer(Expression expression, Root root) {
		return integer(expression, root, new HashSet<>());
	}

	private static int integer(Expression expression, Root root, Set<String> visiting) {
		if (expression instanceof LiteralExpression literal && literal.isInteger()) {
			return Math.toIntExact(literal.getInteger());
		}

		if (expression instanceof ReferenceExpression reference) {
			String name = reference.getIdentifier().getName();

			if (!visiting.add(name)) {
				throw new IllegalArgumentException("cyclic layout constant: " + name);
			}

			try {
				for (var identifier : root.identifierIndex.get(name)) {
					if (identifier.getParent() instanceof DeclarationMember member && member.getParent() instanceof TypeAndInitDeclaration declaration && hasStorage(
						declaration.getType().getTypeQualifier(),
						StorageQualifier.StorageType.CONST) && member.getInitializer() instanceof ExpressionInitializer initializer) {
						return integer(initializer.getExpression(), root, visiting);
					}
				}
			} finally {
				visiting.remove(name);
			}
		}

		if (expression instanceof UnaryExpression unary) {
			int value = integer(unary.getOperand(), root, visiting);

			return switch (expression.getExpressionType()) {
				case GROUPING, IDENTITY -> value;
				case NEGATION -> Math.negateExact(value);
				case BITWISE_NOT -> ~value;
				default -> throw new IllegalArgumentException("unsupported unary layout constant");
			};
		}

		if (expression instanceof BinaryExpression binary) {
			int left = integer(binary.getLeft(), root, visiting);
			int right = integer(binary.getRight(), root, visiting);

			return switch (expression.getExpressionType()) {
				case ADDITION -> Math.addExact(left, right);
				case SUBTRACTION -> Math.subtractExact(left, right);
				case MULTIPLICATION -> Math.multiplyExact(left, right);
				case DIVISION -> left / right;
				case MODULO -> left % right;
				case SHIFT_LEFT -> left << right;
				case SHIFT_RIGHT -> left >> right;
				case BITWISE_AND -> left & right;
				case BITWISE_OR -> left | right;
				case BITWISE_XOR -> left ^ right;
				default -> throw new IllegalArgumentException("unsupported layout constant: " + expression.getExpressionType());
			};
		}

		throw new IllegalArgumentException("expected an integer layout constant, got " + expression);
	}

	public static List<Integer> dimensions(Root root, ArraySpecifier... arrays) {
		List<Integer> dimensions = new ArrayList<>();

		for (ArraySpecifier array : arrays) {
			if (array == null) {
				continue;
			}

			for (Expression dimension : array.getDimensions()) {
				int size = integer(dimension, root);

				if (size <= 0) {
					throw new IllegalArgumentException("array size must be positive");
				}

				dimensions.add(size);
			}
		}

		return dimensions;
	}

	public static Std140Type uniformType(BuiltinNumericTypeSpecifier specifier, DeclarationMember member, Root root) {
		Type type = specifier.type;
		int[] shape = type.getDimensions();

		if (type.getCompactName() == null || (type.getBitDepth() != 1 && type.getBitDepth() != 32 && type.getBitDepth() != 64)) {
			throw new IllegalArgumentException("unsupported uniform scalar width: " + type);
		}

		return new Std140Type(type.getCompactName(), Math.max(4, type.getBitDepth() / 8), type.isMatrix() ? shape[0] : 1,
			type.isScalar() ? 1 : shape[type.isMatrix() ? 1 : 0],
			dimensions(root, member.getArraySpecifier(), specifier.getArraySpecifier()));
	}

	public static boolean isShadowed(ReferenceExpression reference, String name) {
		return isShadowed(reference, name, null);
	}

	public static boolean isShadowed(ReferenceExpression reference, String name, FunctionParameter parameter) {
		var function = reference.getAncestor(FunctionDefinition.class);

		if (function == null) {
			return false;
		}

		for (var identifier : reference.getRoot().identifierIndex.get(name)) {
			ASTNode declaration = identifier.getParent();

			if (declaration == parameter) {
				continue;
			}

			if (declaration instanceof FunctionParameter && declaration.getAncestor(FunctionDefinition.class) == function) {
				return true;
			}

			if (!(declaration instanceof DeclarationMember || declaration instanceof IterationConditionInitializer) || declaration.getAncestor(
				FunctionDefinition.class) != function) {
				continue;
			}

			if (declaration == reference.getAncestor(DeclarationMember.class) || declaration == reference.getAncestor(
				IterationConditionInitializer.class)) {
				continue;
			}

			ASTNode scope = declaration.getAncestor(node -> node instanceof CompoundStatement || node instanceof LoopStatement);

			if (scope == null || reference.getAncestor(node -> node == scope) == null) {
				continue;
			}

			if (scope instanceof CompoundStatement compound) {
				ASTNode declaredAt = childOf(scope, declaration);
				ASTNode referencedAt = childOf(scope, reference);
				int declarationIndex = compound.getStatements().indexOf(declaredAt);
				int referenceIndex = compound.getStatements().indexOf(referencedAt);

				if (declarationIndex > referenceIndex) {
					continue;
				}

				if (declarationIndex == referenceIndex && declaration instanceof DeclarationMember member && reference.getAncestor(
					DeclarationMember.class) instanceof DeclarationMember referencedMember && member.getParent() == referencedMember.getParent() && member.getParent() instanceof TypeAndInitDeclaration typed && typed.getMembers()
					.indexOf(member) > typed.getMembers().indexOf(referencedMember)) {
					continue;
				}
			}

			return true;
		}

		return false;
	}

	private static ASTNode childOf(ASTNode parent, ASTNode node) {
		while (node.getParent() != parent) {
			node = node.getParent();
		}

		return node;
	}
}
