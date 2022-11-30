package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.DeclarationMember;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinFixedTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.shaderpack.texture.TextureStage;

class TextureTransformer {
	public static final AutoHintedMatcher<ExternalDeclaration> sampler = new AutoHintedMatcher<ExternalDeclaration>(
			"uniform Type name;", Matcher.externalDeclarationPattern, "__") {
		{
			markClassedPredicateWildcard("type",
					pattern.getRoot().identifierIndex.getOne("Type").getAncestor(TypeSpecifier.class),
					BuiltinFixedTypeSpecifier.class,
					specifier -> specifier.type.kind == BuiltinFixedTypeSpecifier.BuiltinType.TypeKind.SAMPLER);
			markClassWildcard("name*", pattern.getRoot().identifierIndex.getOne("name").getAncestor(DeclarationMember.class));
		}
	};

	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			TextureStage stage, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		textureMap.forEach((stringTextureTypeTextureStageTri, s) -> {
			Iris.logger.warn("A " + stringTextureTypeTextureStageTri.toString() + " " + s);
			if (stringTextureTypeTextureStageTri.getThird() == stage) {
				String name = stringTextureTypeTextureStageTri.getFirst();

				// check if the declaration has the right type and rename if one is found
				// iterates all hits of the identifier and checks the ancestors
				for (Identifier id : root.identifierIndex.get(name)) {
					TypeAndInitDeclaration initDeclaration = (TypeAndInitDeclaration) id.getAncestor(
							2, 0, TypeAndInitDeclaration.class::isInstance);
					if (initDeclaration == null) {
						continue;
					}
					DeclarationExternalDeclaration declaration = (DeclarationExternalDeclaration) initDeclaration.getAncestor(
							1, 0, DeclarationExternalDeclaration.class::isInstance);
					if (declaration == null) {
						continue;
					}
					if (initDeclaration.getType().getTypeSpecifier() instanceof BuiltinFixedTypeSpecifier fixed
							&& fixed.type == convertType(stringTextureTypeTextureStageTri.getSecond())) {
						root.rename(stringTextureTypeTextureStageTri.getFirst(), s);
						break;
					}
				}
			}
		});
	}

	private static BuiltinFixedTypeSpecifier.BuiltinType convertType(TextureType extractedType) {
		switch (extractedType) {
			case TEXTURE_1D:
				return BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER1D;
			case TEXTURE_2D:
				return BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER2D;
			case TEXTURE_3D:
				return BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER3D;
			default:
				throw new IllegalStateException("What is this enum? " + extractedType.name());
		}
	}
}
