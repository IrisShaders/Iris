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
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.shaderpack.texture.TextureStage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Implements AttributeShaderTransformer using glsl-transformer AST
 * transformation methods.
 */
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
			if (stringTextureTypeTextureStageTri.getThird() == stage) {
				RenameTargetResult targetResult = getTextureRenameTargets(stringTextureTypeTextureStageTri.getFirst(), root);
				if (targetResult != null && targetResult.extractedType.type == convertType(stringTextureTypeTextureStageTri.getSecond())) {
					root.rename(stringTextureTypeTextureStageTri.getFirst(), s);
				}
			}
		});
	}

	private static BuiltinFixedTypeSpecifier.BuiltinType convertType(TextureType extractedType) {
		switch (extractedType) {
			case TEXTURE_1D: return BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER1D;
			case TEXTURE_2D: return BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER2D;
			case TEXTURE_3D: return BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER3D;
			default: throw new IllegalStateException("What is this enum? " + extractedType.name());
		}
	}

	private static class RenameTargetResult {
		public final DeclarationExternalDeclaration samplerDeclaration;
		public final DeclarationMember samplerDeclarationMember;
		public final Stream<Identifier> targets;
		public final BuiltinFixedTypeSpecifier extractedType;

		public RenameTargetResult(DeclarationExternalDeclaration samplerDeclaration,
								  DeclarationMember samplerDeclarationMember, Stream<Identifier> targets, BuiltinFixedTypeSpecifier extractedType) {
			this.samplerDeclaration = samplerDeclaration;
			this.samplerDeclarationMember = samplerDeclarationMember;
			this.targets = targets;
			this.extractedType = extractedType;
		}
	}

	private static RenameTargetResult getTextureRenameTargets(String name, Root root) {
		List<Identifier> gtextureTargets = new ArrayList<>();
		DeclarationExternalDeclaration samplerDeclaration = null;
		DeclarationMember samplerDeclarationMember = null;
		BuiltinFixedTypeSpecifier extractedType = null;

		// collect targets until we find out if the name is a sampler or not
		for (Identifier id : root.identifierIndex.get(name)) {
			gtextureTargets.add(id);
			if (samplerDeclaration != null) {
				continue;
			}
			DeclarationExternalDeclaration externalDeclaration = (DeclarationExternalDeclaration) id.getAncestor(
				3, 0, DeclarationExternalDeclaration.class::isInstance);
			if (externalDeclaration == null) {
				continue;
			}
			if (sampler.matchesExtract(externalDeclaration)) {
				// check that any of the members match the name
				boolean foundNameMatch = false;
				for (DeclarationMember member : sampler
					.getNodeMatch("name*", DeclarationMember.class)
					.getAncestor(TypeAndInitDeclaration.class).getMembers()) {
					if (member.getName().getName().equals(name)) {
						foundNameMatch = true;
					}
				}
				extractedType = sampler.getNodeMatch("type",
					BuiltinFixedTypeSpecifier.class);
				if (!foundNameMatch) {
					return null;
				}

				// no need to check any more declarations
				samplerDeclaration = externalDeclaration;
				samplerDeclarationMember = id.getAncestor(DeclarationMember.class);

				// remove since we are treating the declaration specially
				gtextureTargets.remove(gtextureTargets.size() - 1);
				continue;
			}
			// we found a declaration using this name, but it's not a sampler,
			// renaming this name is disabled
			return null;
		}
		if (samplerDeclaration == null) {
			// no sampler declaration found, renaming this name is disabled
			return null;
		}
		return new RenameTargetResult(samplerDeclaration, samplerDeclarationMember, gtextureTargets.stream(), extractedType);
	}
}
