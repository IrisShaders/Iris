package net.coderbot.iris.pipeline.transform;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.basic.ASTNode;
import io.github.douira.glsl_transformer.ast.node.declaration.Declaration;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.EmptyDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.FunctionDefinition;
import io.github.douira.glsl_transformer.ast.node.statement.terminal.DeclarationStatement;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.gl.shader.ShaderType;

public class CompatibilityTransformer {
	static Logger LOGGER = LogManager.getLogger(CompatibilityTransformer.class);

	public static void transformEach(ASTParser t, TranslationUnit tree, Root root, Parameters parameters) {
		// find all non-global const declarations and remove the const qualifier.
		// happens on all versions because Nvidia wrongly allows non-constant
		// initializers const declarations (and instead treats them only as immutable)
		// at and below version 4.1 so shaderpacks contain such illegal declarations
		// which break on AMD. It also has to be patched above 4.2 because AMD wrongly
		// doesn't allow non-global const declarations to be initialized with
		// non-constant expressions.
		boolean constDeclarationHit = root.process(root.nodeIndex.getStream(DeclarationStatement.class)
				.map(declarationStatement -> {
					// test for type and init declaration
					Declaration declaration = declarationStatement.getDeclaration();
					if (!(declaration instanceof TypeAndInitDeclaration)) {
						return null;
					}

					// all declaration statements must be non-global since statements have to be
					// inside a function definition

					// test for const qualifier
					TypeAndInitDeclaration taid = (TypeAndInitDeclaration) declaration;
					TypeQualifier qualifier = taid.getType().getTypeQualifier();
					if (qualifier == null) {
						return null;
					}
					for (TypeQualifierPart constQualifier : qualifier.getChildren()) {
						if (constQualifier instanceof StorageQualifier) {
							StorageQualifier storageQualifier = (StorageQualifier) constQualifier;
							if (storageQualifier.storageType == StorageQualifier.StorageType.CONST) {
								return storageQualifier;
							}
						}
					}
					return null;
				})
				.filter(Objects::nonNull),
				constQualifier -> {
					TypeQualifier qualifier = (TypeQualifier) constQualifier.getParent();
					constQualifier.detachAndDelete();
					if (qualifier.getChildren().isEmpty()) {
						qualifier.detachAndDelete();
					}
				});
		if (constDeclarationHit) {
			LOGGER.warn(
					"Removed the const keyword from non-global function definitions. This is done to ensure better compatibility drivers' varying behavior at different versions. See Section 4.3.2 on Constant Qualifiers and Section 4.3.3 on Constant Expressions in the GLSL 4.1 and 4.2 specifications for more information.");
		}

		// remove empty external declarations
		boolean emptyDeclarationHit = root.process(
				root.nodeIndex.getStream(EmptyDeclaration.class),
				ASTNode::detachAndDelete);
		if (emptyDeclarationHit) {
			LOGGER.warn(
					"Removed empty external declarations (\";\"). Lone semicolons in the global scope, also when placed after an unrelated function definition, are an empty external declaration which constitutes a syntax error for some drivers.");
		}
	}

	private static final ShaderType[] pipeline = { ShaderType.VERTEX, ShaderType.GEOMETRY, ShaderType.FRAGMENT };
	private static final AutoHintedMatcher<ExternalDeclaration> outDeclarationMatcher = new AutoHintedMatcher<ExternalDeclaration>(
			"out float __name;", Matcher.externalDeclarationPattern, "__") {
		{
			markClassWildcard("type", pattern.getRoot().nodeIndex.getOne(BuiltinNumericTypeSpecifier.class));
		}
	};
	private static final AutoHintedMatcher<ExternalDeclaration> inDeclarationMatcher = new AutoHintedMatcher<ExternalDeclaration>(
			"in float __name;", Matcher.externalDeclarationPattern, "__") {
		{
			markClassWildcard("type", pattern.getRoot().nodeIndex.getOne(BuiltinNumericTypeSpecifier.class));
		}
	};

	private static final String tag = "__";
	private static final String typeTag = tag + "1";
	private static final String nameTag = tag + "2";
	private static final String outDeclarationTemplate = "out " + typeTag + " " + nameTag + ";";
	private static final String initTemplate = nameTag + " = " + typeTag + ";";

	// does transformations that require cross-shader type data
	public static void transformGrouped(
			ASTParser t,
			Map<PatchShaderType, TranslationUnit> trees,
			Parameters parameters) {
		/**
		 * find attributes that are declared as "in" in geometry or fragment but not
		 * declared as "out" in the previous stage. The missing "out" declarations for
		 * these attributes are added and initialized.
		 * TODO:
		 * - improve this when there is node cloning support in glsl-transformer
		 * - fix issues where Iris' own declarations are detected and patched like
		 * iris_FogFragCoord if there are geometry shaders present
		 * - improved geometry shader support? They use funky declarations
		 */
		ShaderType prevType = null;
		for (int i = 0; i < pipeline.length; i++) {
			ShaderType type = pipeline[i];
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

			// if the current type has sources but the previous one doesn't, set the
			// previous one and continue
			if (prevType == null) {
				prevType = type;
				continue;
			}

			PatchShaderType prevPatchTypes = PatchShaderType.fromGlShaderType(prevType)[0];
			TranslationUnit prevTree = trees.get(prevPatchTypes);
			Root prevRoot = prevTree.getRoot();

			// find out declarations
			Set<String> outDeclarations = new HashSet<String>();
			for (ExternalDeclaration declaration : prevRoot.nodeIndex.get(DeclarationExternalDeclaration.class)) {
				if (outDeclarationMatcher.matchesExtract(declaration)) {
					outDeclarations.add(outDeclarationMatcher.getStringDataMatch("name"));
				}
			}

			// add out declarations that are missing for in declarations
			for (PatchShaderType currentType : patchTypes) {
				TranslationUnit tree = trees.get(currentType);
				if (tree == null) {
					continue;
				}
				Root root = tree.getRoot();

				// find the main function
				var mainFunction = prevRoot.identifierIndex.getStream("main")
						.map(id -> id.getBranchAncestor(FunctionDefinition.class, FunctionDefinition::getFunctionPrototype))
						.filter(Objects::nonNull).findAny();
				if (!mainFunction.isPresent()) {
					LOGGER.warn(
							"A shader is missing a main function and could not be compatibility-patched.");
					continue;
				}
				var mainFunctionStatements = mainFunction.get().getBody();

				for (ExternalDeclaration declaration : root.nodeIndex.get(DeclarationExternalDeclaration.class)) {
					if (inDeclarationMatcher.matchesExtract(declaration)) {
						var name = inDeclarationMatcher.getStringDataMatch("name");
						var specifier = inDeclarationMatcher.getNodeMatch("type", BuiltinNumericTypeSpecifier.class);

						if (!outDeclarations.contains(name)) {
							if (specifier == null) {
								LOGGER.warn(
										"The in declaration " + name + " in the " + currentType.glShaderType.name()
												+ " shader that has a missing corresponding out declaration in the previous stage "
												+ prevType.name() + " has a non-numeric type and could not be compatibility-patched.");
							}
							var inDeclaration = t.parseExternalDeclaration(prevTree, outDeclarationTemplate);
							prevTree.injectNode(ASTInjectionPoint.BEFORE_DECLARATIONS, inDeclaration);
							// rename happens later

							prevRoot.identifierIndex.getOne(typeTag).getAncestor(TypeSpecifier.class)
									.replaceByAndDelete(new BuiltinNumericTypeSpecifier(specifier.type));

							var init = t.parseStatement(prevTree, initTemplate);
							mainFunctionStatements.getChildren().add(0, init);
							prevRoot.identifierIndex.rename(nameTag, name);
							prevRoot.identifierIndex.getOneReferenceExpression(typeTag)
									.replaceByAndDelete(LiteralExpression.getDefaultValue(specifier.type));

							LOGGER.warn(
									"The in declaration " + name + " in the " + currentType.glShaderType.name()
											+ " shader that has a missing corresponding out declaration in the previous stage "
											+ prevType.name() + " and has been compatibility-patched.");

							// update out declarations to prevent duplicates
							outDeclarations.add(name);
						}
					}
				}
			}

			prevType = type;
		}
	}
}
