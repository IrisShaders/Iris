package net.coderbot.iris.pipeline.transform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.basic.ASTNode;
import io.github.douira.glsl_transformer.ast.node.declaration.FunctionParameter;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.EmptyDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.FunctionDefinition;
import io.github.douira.glsl_transformer.ast.node.statement.CompoundStatement;
import io.github.douira.glsl_transformer.ast.node.statement.Statement;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.FunctionPrototype;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.util.Type;
import net.coderbot.iris.gl.shader.ShaderType;

public class CompatibilityTransformer {
	static Logger LOGGER = LogManager.getLogger(CompatibilityTransformer.class);

	private static StorageQualifier getConstQualifier(TypeQualifier qualifier) {
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
	}

	public static void transformEach(ASTParser t, TranslationUnit tree, Root root, Parameters parameters) {
		/**
		 * Removes const storage qualifier from declarations in functions if they are
		 * initialized with const parameters. Const parameters are immutable parameters
		 * and can't be used to initialize const declarations because they expect
		 * constant, not just immutable, expressions. This varies between drivers and
		 * versions.
		 * See https://wiki.shaderlabs.org/wiki/Compiler_Behavior_Notes
		 */
		Map<FunctionDefinition, Set<String>> constFunctions = new HashMap<>();
		Set<String> constParameters = new HashSet<>();
		for (FunctionDefinition definition : root.nodeIndex.get(FunctionDefinition.class)) {
			// stop on functions without parameters
			FunctionPrototype prototype = definition.getFunctionPrototype();
			if (prototype.children.isEmpty()) {
				continue;
			}

			// find the const parameters
			Set<String> names = new HashSet<>(prototype.children.size());
			for (FunctionParameter parameter : prototype.children) {
				if (getConstQualifier(parameter.getType().getTypeQualifier()) != null) {
					String name = parameter.getName().getName();
					names.add(name);
					constParameters.add(name);
				}
			}
			if (!constParameters.isEmpty()) {
				constFunctions.put(definition, names);
			}
		}

		// find the reference expressions for the const parameters
		// and check that they are in the right function and are of the right type
		boolean constDeclarationHit = false;
		for (String name : constParameters) {
			for (Identifier id : root.identifierIndex.get(name)) {
				ReferenceExpression reference = id.getAncestor(ReferenceExpression.class);
				if (reference == null) {
					continue;
				}
				TypeAndInitDeclaration taid = reference.getAncestor(TypeAndInitDeclaration.class);
				if (taid == null) {
					continue;
				}
				FunctionDefinition inDefinition = taid.getAncestor(FunctionDefinition.class);
				if (inDefinition == null) {
					continue;
				}
				Set<String> definitionParameters = constFunctions.get(inDefinition);
				if (definitionParameters == null) {
					continue;
				}
				if (definitionParameters.contains(name)) {
					// remove the const qualifier from the reference expression
					TypeQualifier qualifier = taid.getType().getTypeQualifier();
					StorageQualifier constQualifier = getConstQualifier(qualifier);
					if (constQualifier == null) {
						continue;
					}
					constQualifier.detachAndDelete();
					if (qualifier.getChildren().isEmpty()) {
						qualifier.detachAndDelete();
					}
					constDeclarationHit = true;
				}
			}
		}

		if (constDeclarationHit) {
			LOGGER.warn(
					"Removed the const keyword from declarations that use const parameters. Const declarations usually require constant initializer expresions which immutable parameters are not. This is done to ensure better compatibility drivers' varying behavior at different versions. See Section 4.3.2 on Constant Qualifiers and Section 4.3.3 on Constant Expressions in the GLSL 4.1 and 4.2 specifications for more information.");
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
		 * 
		 * It doesn't bother with array specifiers because they are only legal in
		 * geometry shaders, but then also only as an in declaration. The out
		 * declaration in the vertex shader is still just a single value. Missing out
		 * declarations in the geometry shader are also just normal.
		 * 
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
				TranslationUnit currentTree = trees.get(currentType);
				if (currentTree == null) {
					continue;
				}
				Root currentRoot = currentTree.getRoot();

				// find the main function
				Optional<FunctionDefinition> mainFunction = prevRoot.identifierIndex.getStream("main")
						.map(id -> id.getBranchAncestor(FunctionDefinition.class, FunctionDefinition::getFunctionPrototype))
						.filter(Objects::nonNull).findAny();
				if (!mainFunction.isPresent()) {
					LOGGER.warn(
							"A shader is missing a main function and could not be compatibility-patched.");
					continue;
				}
				CompoundStatement mainFunctionStatements = mainFunction.get().getBody();

				for (ExternalDeclaration declaration : currentRoot.nodeIndex.get(DeclarationExternalDeclaration.class)) {
					if (inDeclarationMatcher.matchesExtract(declaration)) {
						String name = inDeclarationMatcher.getStringDataMatch("name");
						BuiltinNumericTypeSpecifier specifier = inDeclarationMatcher.getNodeMatch("type",
								BuiltinNumericTypeSpecifier.class);

						if (!outDeclarations.contains(name)) {
							// make sure the declared in is actually used
							if (!currentRoot.identifierIndex.getAncestors(name, ReferenceExpression.class).findAny().isPresent()) {
								continue;
							}

							if (specifier == null) {
								LOGGER.warn(
										"The in declaration '" + name + "' in the " + currentType.glShaderType.name()
												+ " shader that has a missing corresponding out declaration in the previous stage "
												+ prevType.name() + " has a non-numeric type and could not be compatibility-patched.");
								continue;
							}

							ExternalDeclaration inDeclaration = t.parseExternalDeclaration(prevTree, outDeclarationTemplate);
							prevTree.injectNode(ASTInjectionPoint.BEFORE_DECLARATIONS, inDeclaration);
							// rename happens later

							Type specifierType = specifier.type;
							prevRoot.identifierIndex.getOne(typeTag).getAncestor(TypeSpecifier.class)
									.replaceByAndDelete(new BuiltinNumericTypeSpecifier(specifierType));

							Statement init = t.parseStatement(prevTree, initTemplate);
							mainFunctionStatements.getChildren().add(0, init);
							prevRoot.identifierIndex.rename(nameTag, name);

							if (specifierType.isScalar()) {
								prevRoot.identifierIndex.getOneReferenceExpression(typeTag)
										.replaceByAndDelete(LiteralExpression.getDefaultValue(specifierType));
							} else {
								Root.indexBuildSession(prevRoot, () -> {
									prevRoot.identifierIndex.getOneReferenceExpression(typeTag)
											.replaceByAndDelete(new FunctionCallExpression(
													new Identifier(specifierType.getCompactName()), Stream.of(LiteralExpression.getDefaultValue(specifierType))));
								});
							}

							LOGGER.warn(
									"The in declaration '" + name + "'' in the " + currentType.glShaderType.name()
											+ " shader is missing a corresponding out declaration in the previous stage "
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
