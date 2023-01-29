package net.coderbot.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinFixedTypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.shaderpack.texture.TextureStage;

public class TextureTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			TextureStage stage, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		textureMap.forEach((stringTextureTypeTextureStageTri, s) -> {
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
							&& isTypeValid(stringTextureTypeTextureStageTri.getSecond(), fixed.type)) {
						root.rename(stringTextureTypeTextureStageTri.getFirst(), s);
						break;
					}
				}
			}
		});
	}

	private static boolean isTypeValid(TextureType expectedType, BuiltinFixedTypeSpecifier.BuiltinType extractedType) {
		if (expectedType == TextureType.TEXTURE_1D) {
			return extractedType == BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER1D ||
			extractedType == BuiltinFixedTypeSpecifier.BuiltinType.ISAMPLER1D ||
			extractedType == BuiltinFixedTypeSpecifier.BuiltinType.USAMPLER1D;
		} else if (expectedType == TextureType.TEXTURE_RECTANGLE) {
			return extractedType == BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER2DRECT ||
				extractedType == BuiltinFixedTypeSpecifier.BuiltinType.ISAMPLER2DRECT ||
				extractedType == BuiltinFixedTypeSpecifier.BuiltinType.USAMPLER2DRECT;
		} else if (expectedType == TextureType.TEXTURE_2D) {
			return extractedType == BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER2D ||
				extractedType == BuiltinFixedTypeSpecifier.BuiltinType.ISAMPLER2D ||
				extractedType == BuiltinFixedTypeSpecifier.BuiltinType.USAMPLER2D;
		} else if (expectedType == TextureType.TEXTURE_3D) {
			return extractedType == BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER3D ||
				extractedType == BuiltinFixedTypeSpecifier.BuiltinType.ISAMPLER3D ||
				extractedType == BuiltinFixedTypeSpecifier.BuiltinType.USAMPLER3D;
		} else {
			throw new IllegalStateException("Unexpected enum! " + expectedType);
		}
	}
}
