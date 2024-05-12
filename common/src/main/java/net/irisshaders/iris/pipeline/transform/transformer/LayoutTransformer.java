package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.abstract_node.ASTNode;
import io.github.douira.glsl_transformer.ast.node.declaration.DeclarationMember;
import io.github.douira.glsl_transformer.ast.node.declaration.FunctionParameter;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.EmptyDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.FunctionDefinition;
import io.github.douira.glsl_transformer.ast.node.statement.Statement;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.LayoutQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.NamedLayoutQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.specifier.ArraySpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.FunctionPrototype;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.struct.StructDeclarator;
import io.github.douira.glsl_transformer.ast.node.type.struct.StructMember;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.ast.transform.Template;
import io.github.douira.glsl_transformer.ast.transform.TransformationException;
import io.github.douira.glsl_transformer.parser.ParseShape;
import io.github.douira.glsl_transformer.util.Type;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LayoutTransformer {
	private static final Logger LOGGER = LogManager.getLogger(LayoutTransformer.class);

	private static final ShaderType[] pipeline = {ShaderType.VERTEX, ShaderType.TESSELATION_CONTROL, ShaderType.TESSELATION_EVAL, ShaderType.GEOMETRY, ShaderType.FRAGMENT};
	private static final Matcher<ExternalDeclaration> outDeclarationMatcher = new DeclarationMatcher(
		StorageType.OUT);
	private static final Matcher<ExternalDeclaration> inDeclarationMatcher = new DeclarationMatcher(
		StorageType.IN);
	private static final Matcher<ExternalDeclaration> nonLayoutOutDeclarationMatcher = new Matcher<>(
		"out float name;",
		ParseShape.EXTERNAL_DECLARATION) {
		{
			markClassWildcard("qualifier", pattern.getRoot().nodeIndex.getUnique(TypeQualifier.class));
			markClassWildcard("type", pattern.getRoot().nodeIndex.getUnique(BuiltinNumericTypeSpecifier.class));
			markClassWildcard("name*",
				pattern.getRoot().identifierIndex.getUnique("name").getAncestor(DeclarationMember.class));
		}

		@Override
		public boolean matchesExtract(ExternalDeclaration tree) {
			boolean result = super.matchesExtract(tree);
			if (!result) {
				return false;
			}

			// look for an out qualifier but no layout qualifier
			TypeQualifier qualifier = getNodeMatch("qualifier", TypeQualifier.class);
			var hasOutQualifier = false;
			for (TypeQualifierPart part : qualifier.getParts()) {
				if (part instanceof StorageQualifier storageQualifier) {
					if (storageQualifier.storageType == StorageType.OUT) {
						hasOutQualifier = true;
					}
				} else if (part instanceof LayoutQualifier) {
					return false;
				}
			}
			return hasOutQualifier;
		}
	};
	private static final Matcher<ExternalDeclaration> nonLayoutInDeclarationMatcher = new Matcher<>(
		"in float name;",
		ParseShape.EXTERNAL_DECLARATION) {
		{
			markClassWildcard("qualifier", pattern.getRoot().nodeIndex.getUnique(TypeQualifier.class));
			markClassWildcard("type", pattern.getRoot().nodeIndex.getUnique(BuiltinNumericTypeSpecifier.class));
			markClassWildcard("name*",
				pattern.getRoot().identifierIndex.getUnique("name").getAncestor(DeclarationMember.class));
		}

		@Override
		public boolean matchesExtract(ExternalDeclaration tree) {
			boolean result = super.matchesExtract(tree);
			if (!result) {
				return false;
			}

			// look for an out qualifier but no layout qualifier
			TypeQualifier qualifier = getNodeMatch("qualifier", TypeQualifier.class);
			var hasOutQualifier = false;
			for (TypeQualifierPart part : qualifier.getParts()) {
				if (part instanceof StorageQualifier storageQualifier) {
					if (storageQualifier.storageType == StorageType.IN) {
						hasOutQualifier = true;
					}
				} else if (part instanceof LayoutQualifier) {
					return false;
				}
			}
			return hasOutQualifier;
		}
	};
	private static final Template<ExternalDeclaration> layoutedOutDeclarationTemplate = Template
		.withExternalDeclaration("out __type __name;");
	private static final Template<ExternalDeclaration> layoutedInDeclarationTemplate = Template
		.withExternalDeclaration("in __type __name;");
	private static final String attachTargetPrefix = "outColor";
	private static final List<String> reservedWords = List.of("texture");

	static {
		layoutedOutDeclarationTemplate.markLocalReplacement(
			layoutedOutDeclarationTemplate.getSourceRoot().nodeIndex.getOne(TypeQualifier.class));
		layoutedOutDeclarationTemplate.markLocalReplacement("__type", TypeSpecifier.class);
		layoutedOutDeclarationTemplate.markLocalReplacement("__name", DeclarationMember.class);
		layoutedInDeclarationTemplate.markLocalReplacement(
			layoutedInDeclarationTemplate.getSourceRoot().nodeIndex.getOne(TypeQualifier.class));
		layoutedInDeclarationTemplate.markLocalReplacement("__type", TypeSpecifier.class);
		layoutedInDeclarationTemplate.markLocalReplacement("__name", DeclarationMember.class);
	}

	private static StorageQualifier getConstQualifier(TypeQualifier qualifier) {
		if (qualifier == null) {
			return null;
		}
		for (TypeQualifierPart constQualifier : qualifier.getChildren()) {
			if (constQualifier instanceof StorageQualifier storageQualifier) {
				if (storageQualifier.storageType == StorageType.CONST) {
					return storageQualifier;
				}
			}
		}
		return null;
	}

	private static TypeQualifier makeQualifierOut(TypeQualifier typeQualifier) {
		for (TypeQualifierPart qualifierPart : typeQualifier.getParts()) {
			if (qualifierPart instanceof StorageQualifier storageQualifier) {
				if (((StorageQualifier) qualifierPart).storageType == StorageType.IN) {
					storageQualifier.storageType = StorageType.OUT;
				}
			}
		}
		return typeQualifier;
	}

	// does transformations that require cross-shader type data
	public static void transformGrouped(
		ASTParser t,
		Map<PatchShaderType, TranslationUnit> trees,
		Parameters parameters) {
		/*
		  find attributes that are declared as "in" in geometry or fragment but not
		  declared as "out" in the previous stage. The missing "out" declarations for
		  these attributes are added and initialized.

		  It doesn't bother with array specifiers because they are only legal in
		  geometry shaders, but then also only as an in declaration. The out
		  declaration in the vertex shader is still just a single value. Missing out
		  declarations in the geometry shader are also just normal.

		  TODO:
		  - fix issues where Iris' own declarations are detected and patched like
		  iris_FogFragCoord if there are geometry shaders present
		  - improved geometry shader support? They use funky declarations
		 */
		ShaderType prevType = null;
		final Object2IntMap<String>[] lastMap = new Object2IntMap[]{null};
		for (ShaderType type : pipeline) {
			PatchShaderType[] patchTypes = PatchShaderType.fromGlShaderType(type);

			// check if the patch types have sources and continue if not
			boolean hasAny = false;
			for (PatchShaderType currentType : patchTypes) {
				if (trees.get(currentType) != null) {
					hasAny = true;
				}
			}

			if (!hasAny) {
				continue;
			}



			TranslationUnit currentTree = trees.get(patchTypes[0]);
			if (currentTree == null) {
				continue;
			}
			Root currentRoot = currentTree.getRoot();



			currentRoot.indexBuildSession((root) -> {
				if (root != null) {
					if (lastMap[0] != null) {
						transformIn(lastMap[0], t, currentTree, root, parameters);
					}

					lastMap[0] = transformOut(t, currentTree, root, parameters);
				}
			});

		}
	}

	public static Object2IntMap<String> transformOut(ASTParser t, TranslationUnit tree, Root root, Parameters parameters) {
		// do layout attachment (attaches a location(layout = 4) to the out declaration
		// outColor4 for example)

		// iterate the declarations
		ArrayList<NewDeclarationData> newDeclarationData = new ArrayList<>();
		int location = 0;

		Object2IntMap<String> map = new Object2IntArrayMap<>();
		ArrayList<ExternalDeclaration> declarationsToRemove = new ArrayList<>();
		for (DeclarationExternalDeclaration declaration : root.nodeIndex.get(DeclarationExternalDeclaration.class)) {
			if (!nonLayoutOutDeclarationMatcher.matchesExtract(declaration)) {
				continue;
			}

			// find the matching outColor members
			List<DeclarationMember> members = nonLayoutOutDeclarationMatcher
				.getNodeMatch("name*", DeclarationMember.class)
				.getAncestor(TypeAndInitDeclaration.class)
				.getMembers();
			TypeQualifier typeQualifier = nonLayoutOutDeclarationMatcher.getNodeMatch("qualifier", TypeQualifier.class);
			BuiltinNumericTypeSpecifier typeSpecifier = nonLayoutOutDeclarationMatcher.getNodeMatch("type",
				BuiltinNumericTypeSpecifier.class);
			int addedDeclarations = 0;
			for (DeclarationMember member : members) {
				String name = member.getName().getName();

				map.put(name, location);

				Iris.logger.warn("Found a declaration named " + name);
				newDeclarationData.add(new NewDeclarationData(typeQualifier, typeSpecifier, member, location++, name));
				addedDeclarations++;
			}

			// if the member list is now empty, remove the declaration
			if (addedDeclarations == members.size()) {
				declarationsToRemove.add(declaration);
			}
		}
		tree.getChildren().removeAll(declarationsToRemove);
		for (ExternalDeclaration declaration : declarationsToRemove) {
			declaration.detachParent();
		}

		// generate new declarations with layout qualifiers for each outColor member
		ArrayList<ExternalDeclaration> newDeclarations = new ArrayList<>();

		// Note: since everything is wrapped in a big Root.indexBuildSession, we don't
		// need to do it manually here
		for (NewDeclarationData data : newDeclarationData) {
			DeclarationMember member = data.member;
			member.detach();
			TypeQualifier newQualifier = data.qualifier.cloneInto(root);
			newQualifier.getChildren()
				.add(0, new LayoutQualifier(Stream.of(new NamedLayoutQualifierPart(
					new Identifier("location"),
					new LiteralExpression(Type.INT32, data.location)))));
			ExternalDeclaration newDeclaration = layoutedOutDeclarationTemplate.getInstanceFor(root,
				newQualifier,
				data.type.cloneInto(root),
				member);
			newDeclarations.add(newDeclaration);
		}
		tree.injectNodes(ASTInjectionPoint.BEFORE_DECLARATIONS, newDeclarations);

		return map;
	}

	public static void transformIn(Object2IntMap<String> map, ASTParser t, TranslationUnit tree, Root root, Parameters parameters) {
		// do layout attachment (attaches a location(layout = 4) to the out declaration
		// outColor4 for example)

		// iterate the declarations
		ArrayList<NewDeclarationData> newDeclarationData = new ArrayList<>();

		ArrayList<ExternalDeclaration> declarationsToRemove = new ArrayList<>();
		for (DeclarationExternalDeclaration declaration : root.nodeIndex.get(DeclarationExternalDeclaration.class)) {
			if (!nonLayoutInDeclarationMatcher.matchesExtract(declaration)) {
				continue;
			}

			// find the matching outColor members
			List<DeclarationMember> members = nonLayoutInDeclarationMatcher
				.getNodeMatch("name*", DeclarationMember.class)
				.getAncestor(TypeAndInitDeclaration.class)
				.getMembers();
			TypeQualifier typeQualifier = nonLayoutInDeclarationMatcher.getNodeMatch("qualifier", TypeQualifier.class);
			BuiltinNumericTypeSpecifier typeSpecifier = nonLayoutInDeclarationMatcher.getNodeMatch("type",
				BuiltinNumericTypeSpecifier.class);
			int addedDeclarations = 0;
			for (DeclarationMember member : members) {
				String name = member.getName().getName();
				Iris.logger.warn("Found a member with name " + name);

				if (!map.containsKey(name)) {
					continue;
				}

				newDeclarationData.add(new NewDeclarationData(typeQualifier, typeSpecifier, member, map.getInt(name), name));
				addedDeclarations++;
			}

			// if the member list is now empty, remove the declaration
			if (addedDeclarations == members.size()) {
				declarationsToRemove.add(declaration);
			}
		}
		tree.getChildren().removeAll(declarationsToRemove);
		for (ExternalDeclaration declaration : declarationsToRemove) {
			declaration.detachParent();
		}

		// generate new declarations with layout qualifiers for each outColor member
		ArrayList<ExternalDeclaration> newDeclarations = new ArrayList<>();

		// Note: since everything is wrapped in a big Root.indexBuildSession, we don't
		// need to do it manually here
		for (NewDeclarationData data : newDeclarationData) {
			DeclarationMember member = data.member;
			member.detach();
			TypeQualifier newQualifier = data.qualifier.cloneInto(root);
			newQualifier.getChildren()
				.add(0, new LayoutQualifier(Stream.of(new NamedLayoutQualifierPart(
					new Identifier("location"),
					new LiteralExpression(Type.INT32, data.location)))));
			ExternalDeclaration newDeclaration = layoutedInDeclarationTemplate.getInstanceFor(root,
				newQualifier,
				data.type.cloneInto(root),
				member);
			newDeclarations.add(newDeclaration);
		}
		tree.injectNodes(ASTInjectionPoint.BEFORE_DECLARATIONS, newDeclarations);
	}

	private static class DeclarationMatcher extends Matcher<ExternalDeclaration> {
		private final StorageType storageType;

		{
			markClassWildcard("qualifier", pattern.getRoot().nodeIndex.getUnique(TypeQualifier.class));
			markClassWildcard("type", pattern.getRoot().nodeIndex.getUnique(BuiltinNumericTypeSpecifier.class));
			markClassWildcard("name*",
				pattern.getRoot().identifierIndex.getUnique("name").getAncestor(DeclarationMember.class));
		}

		public DeclarationMatcher(StorageType storageType) {
			super("out float name;", ParseShape.EXTERNAL_DECLARATION);
			this.storageType = storageType;
		}

		@Override
		public boolean matchesExtract(ExternalDeclaration tree) {
			boolean result = super.matchesExtract(tree);
			if (!result) {
				return false;
			}
			TypeQualifier qualifier = getNodeMatch("qualifier", TypeQualifier.class);
			for (TypeQualifierPart part : qualifier.getParts()) {
				if (part instanceof StorageQualifier storageQualifier) {
					if (storageQualifier.storageType == storageType) {
						return true;
					}
				}
			}
			return false;
		}
	}

	record NewDeclarationData(TypeQualifier qualifier, TypeSpecifier type, DeclarationMember member, int location, String name) {
	}
}
