package net.coderbot.iris.pipeline.transform;

import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.basic.ASTNode;
import io.github.douira.glsl_transformer.ast.node.declaration.Declaration;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.EmptyDeclaration;
import io.github.douira.glsl_transformer.ast.node.statement.terminal.DeclarationStatement;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifierPart;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;

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

	// does transformations that require cross-shader type data
	public static void transformGrouped(
			ASTParser t,
			Map<PatchShaderType, TranslationUnit> trees,
			Parameters parameters) {

	}
}
