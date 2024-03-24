package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.DeclarationMember;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.ArrayAccessExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinFixedTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinFixedTypeSpecifier.BuiltinType.TypeKind;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.ast.transform.Template;
import io.github.douira.glsl_transformer.parser.ParseShape;
import io.github.douira.glsl_transformer.util.Type;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommonTransformer {
	public static final AutoHintedMatcher<Expression> glTextureMatrix0 = new AutoHintedMatcher<>(
		"gl_TextureMatrix[0]", ParseShape.EXPRESSION);
	public static final AutoHintedMatcher<Expression> glTextureMatrix1 = new AutoHintedMatcher<>(
		"gl_TextureMatrix[1]", ParseShape.EXPRESSION);
	public static final AutoHintedMatcher<Expression> glTextureMatrix2 = new AutoHintedMatcher<>(
		"gl_TextureMatrix[2]", ParseShape.EXPRESSION);
	public static final Matcher<ExternalDeclaration> sampler = new Matcher<>(
		"uniform Type name;", ParseShape.EXTERNAL_DECLARATION) {
		{
			markClassedPredicateWildcard("type",
				pattern.getRoot().identifierIndex.getUnique("Type").getAncestor(TypeSpecifier.class),
				BuiltinFixedTypeSpecifier.class,
				specifier -> specifier.type.kind == TypeKind.SAMPLER);
			markClassWildcard("name*",
				pattern.getRoot().identifierIndex.getUnique("name").getAncestor(DeclarationMember.class));
		}
	};

	private static final AutoHintedMatcher<Expression> glFragDataI = new AutoHintedMatcher<>(
		"gl_FragData[index]", ParseShape.EXPRESSION) {
		{
			markClassedPredicateWildcard("index",
				pattern.getRoot().identifierIndex.getUnique("index").getAncestor(ReferenceExpression.class),
				LiteralExpression.class,
				literalExpression -> literalExpression.isInteger() && literalExpression.getInteger() >= 0);
		}
	};

	private static final Template<ExternalDeclaration> fragDataDeclaration = Template
		.withExternalDeclaration("layout (location = __index) out vec4 __name;");
	private static final List<Expression> replaceExpressions = new ArrayList<>();
	private static final List<Long> replaceIndexes = new ArrayList<>();
	private static final Template<ExternalDeclaration> inputDeclarationTemplate = Template.withExternalDeclaration(
		"uniform int __name;");

	static {
		fragDataDeclaration.markLocalReplacement("__index", ReferenceExpression.class);
		fragDataDeclaration.markIdentifierReplacement("__name");
	}

	static {
		inputDeclarationTemplate.markLocalReplacement(
			inputDeclarationTemplate.getSourceRoot().nodeIndex.getOne(StorageQualifier.class));
		inputDeclarationTemplate.markLocalReplacement(
			inputDeclarationTemplate.getSourceRoot().nodeIndex.getOne(BuiltinNumericTypeSpecifier.class));
		inputDeclarationTemplate.markIdentifierReplacement("__name");
	}

	static void renameFunctionCall(Root root, String oldName, String newName) {
		root.process(root.identifierIndex.getStream(oldName)
				.filter(id -> id.getParent() instanceof FunctionCallExpression),
			id -> id.setName(newName));
	}

	static void renameAndWrapShadow(ASTParser t, Root root, String oldName, String innerName) {
		root.process(root.identifierIndex.getStream(oldName)
				.filter(id -> id.getParent() instanceof FunctionCallExpression),
			id -> {
				FunctionCallExpression functionCall = (FunctionCallExpression) id.getParent();
				functionCall.getFunctionName().setName(innerName);
				FunctionCallExpression wrapper = (FunctionCallExpression) t.parseExpression(root, "vec4()");
				functionCall.replaceBy(wrapper);
				wrapper.getParameters().add(functionCall);
			});
	}

	public static void patchMultiTexCoord3(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		Parameters parameters) {
		if (parameters.type.glShaderType == ShaderType.VERTEX
			&& root.identifierIndex.has("gl_MultiTexCoord3")
			&& !root.identifierIndex.has("mc_midTexCoord")) {
			// TODO: proper type conversion
			// gl_MultiTexCoord3 is a super legacy alias of mc_midTexCoord. We don't do this
			// replacement if we think mc_midTexCoord could be defined just we can't handle
			// an existing declaration robustly. But basically the proper way to do this is
			// to define mc_midTexCoord only if it's not defined, and if it is defined,
			// figure out its type, then replace all occurrences of gl_MultiTexCoord3 with
			// the correct conversion from mc_midTexCoord's declared type to vec4.
			root.rename("gl_MultiTexCoord3", "mc_midTexCoord");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"attribute vec4 mc_midTexCoord;");
		}
	}

	public static void upgradeStorageQualifiers(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		Parameters parameters) {
		for (StorageQualifier qualifier : root.nodeIndex.get(StorageQualifier.class)) {
			if (qualifier.storageType == StorageType.ATTRIBUTE) {
				qualifier.storageType = StorageType.IN;
			} else if (qualifier.storageType == StorageType.VARYING) {
				qualifier.storageType = parameters.type.glShaderType == ShaderType.VERTEX
					? StorageType.OUT
					: StorageType.IN;
			}
		}
	}

	public static void transform(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		Parameters parameters,
		boolean core) {
		// TODO: What if the shader does gl_PerVertex.gl_FogFragCoord ?

		root.rename("gl_FogFragCoord", "iris_FogFragCoord");

		// TODO: This doesn't handle geometry shaders... How do we do that?
		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"out float iris_FogFragCoord;");
			tree.prependMainFunctionBody(t, "iris_FogFragCoord = 0.0f;");
		} else if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"in float iris_FogFragCoord;");
		}

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// TODO: This is incorrect and is just the bare minimum needed for SEUS v11 &
			// Renewed to compile. It works because they don't actually use gl_FrontColor
			// even though they write to it.

			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"vec4 iris_FrontColor;");
			root.rename("gl_FrontColor", "iris_FrontColor");
		}

		if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
			// TODO: Find a way to properly support gl_FragColor, see TransformPatcherOld
			// which implements this
			if (root.identifierIndex.has("gl_FragColor")) {
				Iris.logger.warn(
					"[Patcher] gl_FragColor is not supported yet, please use gl_FragData! Assuming that the shaderpack author intended to use gl_FragData[0]...");
				root.replaceReferenceExpressions(t, "gl_FragColor", "gl_FragData[0]");
			}

			if (root.identifierIndex.has("gl_TexCoord")) {
				root.rename("gl_TexCoord", "irs_texCoords");
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec4 irs_texCoords[3];");
			}

			if (root.identifierIndex.has("gl_Color")) {
				root.rename("gl_Color", "irs_Color");
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec4 irs_Color;");
			}

			// change gl_FragData[i] to iris_FragDatai
			replaceExpressions.clear();
			replaceIndexes.clear();
			Set<Long> replaceIndexesSet = new HashSet<>();
			for (Identifier id : root.identifierIndex.get("gl_FragData")) {
				ArrayAccessExpression accessExpression = id.getAncestor(ArrayAccessExpression.class);
				if (accessExpression == null || !glFragDataI.matchesExtract(accessExpression)) {
					continue;
				}
				replaceExpressions.add(accessExpression);
				long index = glFragDataI.getNodeMatch("index", LiteralExpression.class).getInteger();
				replaceIndexes.add(index);
				replaceIndexesSet.add(index);
			}
			for (int i = 0; i < replaceExpressions.size(); i++) {
				replaceExpressions.get(i).replaceByAndDelete(
					new ReferenceExpression(new Identifier("iris_FragData" + replaceIndexes.get(i))));
			}
			for (long index : replaceIndexesSet) {
				tree.injectNode(ASTInjectionPoint.BEFORE_DECLARATIONS,
					fragDataDeclaration.getInstanceFor(root,
						new LiteralExpression(Type.INT32, index),
						new Identifier("iris_FragData" + index)));
			}
			replaceExpressions.clear();
			replaceIndexes.clear();

			// insert alpha test for iris_FragData0 in the fragment shader
			if ((parameters.getAlphaTest() != AlphaTest.ALWAYS && !core) && replaceIndexesSet.contains(0L)) {
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform float iris_currentAlphaTest;");
				tree.appendMainFunctionBody(t,
					parameters.getAlphaTest().toExpression("iris_FragData0.a", "iris_currentAlphaTest", "	"));
			}
		}

		if (parameters.type.glShaderType == ShaderType.VERTEX || parameters.type.glShaderType == ShaderType.FRAGMENT) {
			upgradeStorageQualifiers(t, tree, root, parameters);
		}

		// addition: rename all uses of texture and gcolor to gtexture if it's *not*
		// used as a function call.
		// it only does this if they are declared as samplers and makes sure that there
		// is only one sampler declaration.
		RenameTargetResult gcolorResult = getGtextureRenameTargets("gcolor", root);
		RenameTargetResult textureResult = getGtextureRenameTargets("texture", root);
		DeclarationMember samplerDeclarationMember = null;
		Stream<Identifier> targets = Stream.empty();
		if (gcolorResult != null) {
			samplerDeclarationMember = gcolorResult.samplerDeclarationMember;
			targets = Stream.concat(targets, gcolorResult.targets);
		}
		if (textureResult != null) {
			// if two exist, remove the member from the second one
			if (samplerDeclarationMember == null) {
				samplerDeclarationMember = textureResult.samplerDeclarationMember;
			} else {
				DeclarationMember secondDeclarationMember = textureResult.samplerDeclarationMember;
				if (((TypeAndInitDeclaration) secondDeclarationMember.getParent()).getMembers().size() == 1) {
					textureResult.samplerDeclaration.detachAndDelete();
				} else {
					secondDeclarationMember.detachAndDelete();
				}
			}
			targets = Stream.concat(targets, textureResult.targets);
		}
		if (samplerDeclarationMember != null) {
			samplerDeclarationMember.getName().setName("gtexture");
		}
		root.process(targets.filter(id -> !(id.getParent() instanceof FunctionCallExpression)),
			id -> id.setName("gtexture"));

		// This must be defined and valid in all shader passes, including composite
		// passes. A shader that relies on this behavior is SEUS v11 - it reads
		// gl_Fog.color and breaks if it is not properly defined.
		root.rename("gl_Fog", "iris_Fog");
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			"uniform float iris_FogDensity;",
			"uniform float iris_FogStart;",
			"uniform float iris_FogEnd;",
			"uniform vec4 iris_FogColor;",
			"struct iris_FogParameters {" +
				"vec4 color;" +
				"float density;" +
				"float start;" +
				"float end;" +
				"float scale;" +
				"};",
			"iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor, iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0 / (iris_FogEnd - iris_FogStart));");

		// TODO: Add similar functions for all legacy texture sampling functions
		renameFunctionCall(root, "texture2D", "texture");
		renameFunctionCall(root, "texture3D", "texture");
		renameFunctionCall(root, "texture2DLod", "textureLod");
		renameFunctionCall(root, "texture3DLod", "textureLod");
		renameFunctionCall(root, "texture2DGrad", "textureGrad");
		renameFunctionCall(root, "texture2DGradARB", "textureGrad");
		renameFunctionCall(root, "texture3DGrad", "textureGrad");
		renameFunctionCall(root, "texelFetch2D", "texelFetch");
		renameFunctionCall(root, "texelFetch3D", "texelFetch");
		renameFunctionCall(root, "textureSize2D", "textureSize");

		renameAndWrapShadow(t, root, "shadow2D", "texture");
		renameAndWrapShadow(t, root, "shadow2DLod", "textureLod");
	}

	private static RenameTargetResult getGtextureRenameTargets(String name, Root root) {
		List<Identifier> gtextureTargets = new ArrayList<>();
		DeclarationExternalDeclaration samplerDeclaration = null;
		DeclarationMember samplerDeclarationMember = null;

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
		return new RenameTargetResult(samplerDeclaration, samplerDeclarationMember, gtextureTargets.stream());
	}

	public static void applyIntelHd4000Workaround(Root root) {
		// This is a driver bug workaround that seems to be needed on HD 4000 and HD
		// 2500 drivers.
		//
		// Without this, they will see the call to ftransform() without taking into
		// account the fact that we've defined the function ourselves, and thus will
		// allocate attribute location 0 to gl_Vertex since the driver thinks that we
		// are referencing gl_Vertex by calling the compatibility-profile ftransform()
		// function - despite using a core profile shader version where that function
		// does not exist.
		//
		// Then, when we try to bind the attribute locations for our vertex format, the
		// shader will fail to link. By renaming the function, we avoid the driver bug
		// here, since it no longer thinks that we're trying to call the compatibility
		// profile fransform() function.
		//
		// See: https://github.com/IrisShaders/Iris/issues/441
		//
		// Note that this happens after we've added our ftransform function - so that
		// both all the calls and our function are renamed in one go.
		root.rename("ftransform", "iris_ftransform");
	}

	public static void replaceGlMultiTexCoordBounded(
		ASTParser t,
		Root root,
		int minimum,
		int maximum) {
		root.replaceReferenceExpressions(t,
			root.getPrefixIdentifierIndex().prefixQueryFlat("gl_MultiTexCoord")
				.filter(id -> {
					int index = Integer.parseInt(id.getName().substring("gl_MultiTexCoord".length()));
					return index >= minimum && index <= maximum;
				}),
			"vec4(0.0, 0.0, 0.0, 1.0)");
	}

	public static void addIfNotExists(Root root, ASTParser t, TranslationUnit tree, String name, Type type,
									  StorageType storageType) {
		if (root.externalDeclarationIndex.getStream(name)
			.noneMatch((entry) -> entry.declaration() instanceof DeclarationExternalDeclaration)) {
			tree.injectNode(ASTInjectionPoint.BEFORE_DECLARATIONS, inputDeclarationTemplate.getInstanceFor(root,
				new StorageQualifier(storageType),
				new BuiltinNumericTypeSpecifier(type),
				new Identifier(name)));
		}
	}

	private record RenameTargetResult(DeclarationExternalDeclaration samplerDeclaration,
									  DeclarationMember samplerDeclarationMember, Stream<Identifier> targets) {
	}
}
