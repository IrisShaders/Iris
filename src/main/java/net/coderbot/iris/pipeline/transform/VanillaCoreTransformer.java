package net.coderbot.iris.pipeline.transform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.DeclarationMember;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.LayoutQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.NamedLayoutQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.ast.transform.Template;
import io.github.douira.glsl_transformer.util.Type;

public class VanillaCoreTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			VanillaParameters parameters) {
		root.rename("alphaTestRef", "iris_currentAlphaTest");
		root.rename("modelViewMatrix", "iris_ModelViewMat");
		root.rename("modelViewMatrixInverse", "iris_ModelViewMatInverse");
		root.rename("projectionMatrix", "iris_ProjMat");
		root.rename("projectionMatrixInverse", "iris_ProjMatInverse");
		root.rename("textureMatrix", "iris_TextureMat");
		root.rename("normalMatrix", "iris_NormalMat");
		root.rename("chunkOffset", "iris_ChunkOffset");

		if (parameters.type == PatchShaderType.VERTEX) {
			root.rename("vaPosition", "iris_Position");
			root.rename("vaColor", "iris_Color");
			root.rename("vaNormal", "iris_Normal");
			root.rename("vaUV0", "iris_UV0");
			root.rename("vaUV1", "iris_UV1");
			root.rename("vaUV2", "iris_UV2");
		}

		if (parameters.inputs.hasOverlay()) {
			AttributeTransformer.patchOverlayColor(t, tree, root, parameters);
		}

		// if more than just outColor needs to be patched, DO NOT call this method
		// multiple times (that is unnecessarily slow)
		attachLayout(t, tree, root);
	}

	private static final AutoHintedMatcher<ExternalDeclaration> nonLayoutOutDeclarationMatcher = new AutoHintedMatcher<ExternalDeclaration>(
			"out float name;",
			Matcher.externalDeclarationPattern) {
		{
			markClassWildcard("qualifier", pattern.getRoot().nodeIndex.getOne(TypeQualifier.class));
			markClassWildcard("type", pattern.getRoot().nodeIndex.getOne(BuiltinNumericTypeSpecifier.class));
			markClassWildcard("name*",
					pattern.getRoot().identifierIndex.getOne("name").getAncestor(DeclarationMember.class));
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
				if (part instanceof StorageQualifier) {
					StorageQualifier storageQualifier = (StorageQualifier) part;
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

	private static final Template<ExternalDeclaration> layoutedOutDeclarationTemplate = Template
			.withExternalDeclaration("out __type __name;");
	static {
		layoutedOutDeclarationTemplate
				.markLocalReplacement(layoutedOutDeclarationTemplate.getSourceRoot().nodeIndex.getOne(TypeQualifier.class));
		layoutedOutDeclarationTemplate.markLocalReplacement("__type", TypeSpecifier.class);
		layoutedOutDeclarationTemplate.markLocalReplacement("__name", DeclarationMember.class);
	}

	private static final class NewDeclarationData {
		TypeQualifier qualifier;
		TypeSpecifier type;
		DeclarationMember member;
		int number;

		NewDeclarationData(TypeQualifier qualifier, TypeSpecifier type, DeclarationMember member, int number) {
			this.qualifier = qualifier;
			this.type = type;
			this.member = member;
			this.number = number;
		}
	}

	private static final String prefix = "outColor";

	private static void attachLayout(ASTParser t, TranslationUnit tree, Root root) {
		// iterate the declarations
		var newDeclarationData = new ArrayList<NewDeclarationData>();
		var declarationsToRemove = new ArrayList<ExternalDeclaration>();
		for (DeclarationExternalDeclaration declaration : root.nodeIndex.get(DeclarationExternalDeclaration.class)) {
			if (!nonLayoutOutDeclarationMatcher.matchesExtract(declaration)) {
				continue;
			}

			// find the matching outColor members
			var members = nonLayoutOutDeclarationMatcher
					.getNodeMatch("name*", DeclarationMember.class)
					.getAncestor(TypeAndInitDeclaration.class)
					.getMembers();
			var typeQualifier = nonLayoutOutDeclarationMatcher.getNodeMatch("qualifier", TypeQualifier.class);
			var typeSpecifier = nonLayoutOutDeclarationMatcher.getNodeMatch("type", BuiltinNumericTypeSpecifier.class);
			int addedDeclarations = 0;
			for (DeclarationMember member : members) {
				var name = member.getName().getName();
				if (!name.startsWith(prefix)) {
					continue;
				}

				// get the number suffix after the prefix
				var numberSuffix = name.substring(prefix.length());
				if (numberSuffix.isEmpty()) {
					continue;
				}

				// make sure it's a number and is between 0 and 7
				int number;
				try {
					number = Integer.parseInt(numberSuffix);
				} catch (NumberFormatException e) {
					continue;
				}
				if (number < 0 || number > 7) {
					continue;
				}

				newDeclarationData.add(new NewDeclarationData(typeQualifier, typeSpecifier, member, number));
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

		// for test consistency: sort the new declarations by position in the
		// original declaration and then translation unit index
		newDeclarationData.sort(Comparator
				.<NewDeclarationData>comparingInt(
						data -> tree.getChildren().indexOf(data.member.getAncestor(ExternalDeclaration.class)))
				.thenComparingInt(
						data -> data.member.getAncestor(TypeAndInitDeclaration.class).getMembers().indexOf(data.member)));

		// generate new declarations with layout qualifiers for each outColor member
		var newDeclarations = new ArrayList<ExternalDeclaration>();
		Root.indexBuildSession(root, () -> {
			for (NewDeclarationData data : newDeclarationData) {
				var member = data.member;
				member.detach();
				var newQualifier = data.qualifier.cloneInto(root);
				newQualifier.getChildren()
						.add(new LayoutQualifier(Stream.of(new NamedLayoutQualifierPart(
								new Identifier("location"),
								new LiteralExpression(Type.INT32, data.number)))));
				var newDeclaration = layoutedOutDeclarationTemplate.getInstanceFor(root,
						newQualifier,
						data.type.cloneInto(root),
						member);
				newDeclarations.add(newDeclaration);
			}
		});
		tree.injectNodes(ASTInjectionPoint.BEFORE_DECLARATIONS, newDeclarations);
	}
}
