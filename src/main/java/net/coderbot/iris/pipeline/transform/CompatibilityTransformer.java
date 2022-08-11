package net.coderbot.iris.pipeline.transform;

import java.util.Objects;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.Declaration;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.FunctionDefinition;
import io.github.douira.glsl_transformer.ast.node.statement.terminal.DeclarationStatement;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifierPart;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;

public class CompatibilityTransformer {
	public static void transform(
			ASTTransformer<?> t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		// find all non-global const declarations and remove the const qualifier.
		// happens on all versions because Nvidia wrongly allows non-constant
		// initializers const declarations (and instead treats them only as immutable)
		// at and below version 4.1 so shaderpacks contain such illegal declarations
		// which break on AMD. It also has to be patched above 4.2 because AMD wrongly
		// doesn't allow non-global const declarations to be initialized with
		// non-constant expressions.
		root.process(root.nodeIndex.getStream(DeclarationStatement.class)
				.map(declarationStatement -> {
					// test for type and init declaration
					Declaration declaration = declarationStatement.getDeclaration();
					if (!(declaration instanceof TypeAndInitDeclaration)) {
						return null;
					}

					// test for not global
					if (!declarationStatement.hasAncestor(FunctionDefinition.class)) {
						return null;
					}

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
	}
}
