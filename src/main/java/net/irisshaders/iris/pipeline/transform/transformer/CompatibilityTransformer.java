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

public class CompatibilityTransformer {
	private static final Logger LOGGER = LogManager.getLogger(CompatibilityTransformer.class);

	private static final AutoHintedMatcher<Expression> sildursWaterFract = new AutoHintedMatcher<>(
		"fract(worldpos.y + 0.001)", ParseShape.EXPRESSION);
	private static final ShaderType[] pipeline = {ShaderType.VERTEX, ShaderType.TESSELATION_CONTROL, ShaderType.TESSELATION_EVAL, ShaderType.GEOMETRY, ShaderType.FRAGMENT};
	private static final Matcher<ExternalDeclaration> outDeclarationMatcher = new DeclarationMatcher(
		StorageType.OUT);
	private static final Matcher<ExternalDeclaration> inDeclarationMatcher = new DeclarationMatcher(
		StorageType.IN);
	private static final String tagPrefix = "iris_template_";
	private static final Template<ExternalDeclaration> declarationTemplate = Template
		.withExternalDeclaration("out __type __name;");
	private static final Template<Statement> initTemplate = Template.withStatement("__decl = __value;");
	private static final Template<ExternalDeclaration> variableTemplate = Template
		.withExternalDeclaration("__type __internalDecl;");
	private static final Template<Statement> statementTemplate = Template
		.withStatement("__oldDecl = vec3(__internalDecl);");
	private static final Template<Statement> statementTemplateVector = Template
		.withStatement("__oldDecl = vec3(__internalDecl, vec4(0));");
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
	private static final Template<ExternalDeclaration> layoutedOutDeclarationTemplate = Template
		.withExternalDeclaration("out __type __name;");
	private static final String attachTargetPrefix = "outColor";
	private static final List<String> reservedWords = List.of("texture");

	static {
		declarationTemplate
			.markLocalReplacement(declarationTemplate.getSourceRoot().nodeIndex.getUnique(TypeQualifier.class));
		declarationTemplate.markLocalReplacement("__type", TypeSpecifier.class);
		declarationTemplate.markIdentifierReplacement("__name");
		initTemplate.markIdentifierReplacement("__decl");
		initTemplate.markLocalReplacement("__value", ReferenceExpression.class);
		variableTemplate.markLocalReplacement("__type", TypeSpecifier.class);
		variableTemplate.markIdentifierReplacement("__internalDecl");
		statementTemplate.markIdentifierReplacement("__oldDecl");
		statementTemplate.markIdentifierReplacement("__internalDecl");
		statementTemplate.markLocalReplacement(
			statementTemplate.getSourceRoot().nodeIndex.getStream(BuiltinNumericTypeSpecifier.class)
				.filter(specifier -> specifier.type == Type.F32VEC3).findAny().get());
		statementTemplateVector.markIdentifierReplacement("__oldDecl");
		statementTemplateVector.markIdentifierReplacement("__internalDecl");
		statementTemplateVector.markLocalReplacement(
			statementTemplateVector.getSourceRoot().nodeIndex.getStream(BuiltinNumericTypeSpecifier.class)
				.filter(specifier -> specifier.type == Type.F32VEC3).findAny().get());
	}

	static {
		layoutedOutDeclarationTemplate.markLocalReplacement(
			layoutedOutDeclarationTemplate.getSourceRoot().nodeIndex.getOne(TypeQualifier.class));
		layoutedOutDeclarationTemplate.markLocalReplacement("__type", TypeSpecifier.class);
		layoutedOutDeclarationTemplate.markLocalReplacement("__name", DeclarationMember.class);
	}

	private static StorageQualifier getConstQualifier(TypeQualifier qualifier) {
		if (qualifier == null) {
			return null;
		}
		for (TypeQualifierPart constQualifier : qualifier.getChildren()) {
			if (constQualifier instanceof StorageQualifier storageQualifier) {
				if (storageQualifier.storageType == StorageQualifier.StorageType.CONST) {
					return storageQualifier;
				}
			}
		}
		return null;
	}

	public static void transformEach(ASTParser t, TranslationUnit tree, Root root, Parameters parameters) {
		if (parameters.type == PatchShaderType.VERTEX) {
			if (root.replaceExpressionMatches(t, sildursWaterFract, "fract(worldpos.y + 0.01)")) {
				Iris.logger.warn("Patched fract(worldpos.y + 0.001) to fract(worldpos.y + 0.01) to fix " +
					"waving water disconnecting from other water blocks; See https://github.com/IrisShaders/Iris/issues/509");
			}
		}

		/*
		  Removes const storage qualifier from declarations in functions if they are
		  initialized with const parameters. Const parameters are immutable parameters
		  and can't be used to initialize const declarations because they expect
		  constant, not just immutable, expressions. This varies between drivers and
		  versions. Also removes the const qualifier from declarations that use the
		  identifiers from which the declaration was removed previously.
		  See https://wiki.shaderlabs.org/wiki/Compiler_Behavior_Notes
		 */
		Map<FunctionDefinition, Set<String>> constFunctions = new HashMap<>();
		Set<String> processingSet = new HashSet<>();
		List<FunctionDefinition> unusedFunctions = new LinkedList<>();
		for (FunctionDefinition definition : root.nodeIndex.get(FunctionDefinition.class)) {
			// check if this function is ever used
			FunctionPrototype prototype = definition.getFunctionPrototype();
			String functionName = prototype.getName().getName();
			if (!functionName.equals("main") && root.identifierIndex.getStream(functionName).count() <= 1) {
				// remove unused functions
				// unused function removal can be helpful since some drivers don't do some
				// checks on unused functions. Additionally, sometimes bugs in unused code can
				// be avoided this way.
				// TODO: integrate into debug mode (allow user to disable this behavior for
				// debugging purposes)
				unusedFunctions.add(definition);
				/*
				 * else if (unusedFunctions.size() == 1) {
				 * LOGGER.warn(
				 * "Removing unused function " + functionName
				 * +
				 * " and omitting further such messages outside of debug mode. See debugging.md for more information."
				 * );
				 * }
				 */
				continue;
			}

			// stop on functions without parameters
			if (prototype.getChildren().isEmpty()) {
				continue;
			}

			// find the const parameters
			Set<String> names = new HashSet<>(prototype.getChildren().size());
			for (FunctionParameter parameter : prototype.getChildren()) {
				if (getConstQualifier(parameter.getType().getTypeQualifier()) != null) {
					String name = parameter.getName().getName();
					names.add(name);
					processingSet.add(name);
				}
			}
			if (!names.isEmpty()) {
				constFunctions.put(definition, names);
			}
		}

		// remove collected unused functions
		if (!Iris.getIrisConfig().areDebugOptionsEnabled()) {
			for (FunctionDefinition definition : unusedFunctions) {
				definition.detachAndDelete();
			}
		}

		// find the reference expressions for the const parameters
		// and check that they are in the right function and are of the right type
		boolean constDeclarationHit = false;
		Deque<String> processingQueue = new ArrayDeque<>(processingSet);
		while (!processingQueue.isEmpty()) {
			String name = processingQueue.poll();
			processingSet.remove(name);
			for (Identifier id : root.identifierIndex.get(name)) {
				// since this searches for reference expressions, this won't accidentally find
				// the name as the name of a declaration member
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
				Set<String> constIdsInFunction = constFunctions.get(inDefinition);
				if (constIdsInFunction == null) {
					continue;
				}
				if (constIdsInFunction.contains(name)) {
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

					// add all members of the declaration to the list of const parameters to process
					for (DeclarationMember member : taid.getMembers()) {
						String memberName = member.getName().getName();

						// the name may not be the same as the parameter name
						if (constIdsInFunction.contains(memberName)) {
							throw new TransformationException("Illegal redefinition of const parameter " + name);
						}

						constIdsInFunction.add(memberName);

						// don't add to the queue twice if it's already been added by a different scope
						if (!processingSet.contains(memberName)) {
							processingQueue.add(memberName);
							processingSet.add(memberName);
						}
					}
				}
			}
		}

		if (constDeclarationHit) {
			LOGGER.warn(
				"Removed the const keyword from declarations that use const parameters. See debugging.md for more information.");
		}

		// remove empty external declarations
		boolean emptyDeclarationHit = root.process(
			root.nodeIndex.getStream(EmptyDeclaration.class),
			ASTNode::detachAndDelete);
		if (emptyDeclarationHit) {
			LOGGER.warn(
				"Removed empty external declarations (\";\").");
		}

		// rename reserved words within files
		for (String reservedWord : reservedWords) {
			String newName = "iris_renamed_" + reservedWord;
			if (root.process(root.identifierIndex.getStream(reservedWord).filter(
					id -> !(id.getParent() instanceof FunctionCallExpression)
						&& !(id.getParent() instanceof FunctionPrototype)),
				id -> id.setName(newName))) {
				LOGGER.warn("Renamed reserved word \"" + reservedWord + "\" to \"" + newName + "\".");
			}
		}

		// transform that moves unsized array specifiers on struct members from the type
		// to the identifier of a type and init declaration. Some drivers appear to not
		// be able to detect the unsized array if it's on the type.
		for (StructMember structMember : root.nodeIndex.get(StructMember.class)) {
			// check if the type specifier has an array specifier
			TypeSpecifier typeSpecifier = structMember.getType().getTypeSpecifier();
			ArraySpecifier arraySpecifier = typeSpecifier.getArraySpecifier();
			if (arraySpecifier == null) {
				continue;
			}

			// check if the array specifier is unsized
			if (!arraySpecifier.getChildren().isNullEmpty()) {
				continue;
			}

			// remove itself from the parent (makes it null)
			arraySpecifier.detach();

			// move the empty array specifier to all members
			boolean reusedOriginal = false;
			for (StructDeclarator declarator : structMember.getDeclarators()) {
				if (declarator.getArraySpecifier() != null) {
					throw new TransformationException("Member already has an array specifier");
				}

				// clone the array specifier into this member, re-use if possible
				declarator.setArraySpecifier(reusedOriginal ? arraySpecifier.cloneInto(root) : arraySpecifier);
				reusedOriginal = true;
			}

			LOGGER.warn(
				"Moved unsized array specifier (of the form []) from the type to each of the the declaration member(s) "
					+ structMember.getDeclarators().stream().map(StructDeclarator::getName).map(Identifier::getName)
					.collect(Collectors.joining(", "))
					+ ". See debugging.md for more information.");
		}
	}

	private static Statement getInitializer(Root root, String name, Type type) {
		return initTemplate.getInstanceFor(root,
			new Identifier(name),
			type.isScalar()
				? LiteralExpression.getDefaultValue(type)
				: root.indexNodes(() -> new FunctionCallExpression(
				new Identifier(type.getMostCompactName()),
				Stream.of(LiteralExpression.getDefaultValue(type)))));
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

			// test if the prefix tag is used for some reason
			if (prevRoot.getPrefixIdentifierIndex().prefixQueryFlat(tagPrefix).findAny().isPresent()) {
				LOGGER.warn("The prefix tag " + tagPrefix + " is used in the shader, bailing compatibility transformation.");
				return;
			}

			// find out declarations
			Map<String, BuiltinNumericTypeSpecifier> outDeclarations = new HashMap<>();
			for (DeclarationExternalDeclaration declaration : prevRoot.nodeIndex.get(DeclarationExternalDeclaration.class)) {
				if (outDeclarationMatcher.matchesExtract(declaration)) {
					BuiltinNumericTypeSpecifier extractedType = outDeclarationMatcher.getNodeMatch("type",
						BuiltinNumericTypeSpecifier.class);
					for (DeclarationMember member : outDeclarationMatcher
						.getNodeMatch("name*", DeclarationMember.class)
						.getAncestor(TypeAndInitDeclaration.class)
						.getMembers()) {
						String name = member.getName().getName();
						if (!name.startsWith("gl_")) {
							outDeclarations.put(name, extractedType);
						}
					}
				}
			}

			// add out declarations that are missing for in declarations
			for (PatchShaderType currentType : patchTypes) {
				TranslationUnit currentTree = trees.get(currentType);
				if (currentTree == null) {
					continue;
				}
				Root currentRoot = currentTree.getRoot();

				for (ExternalDeclaration declaration : currentRoot.nodeIndex.get(DeclarationExternalDeclaration.class)) {
					if (!inDeclarationMatcher.matchesExtract(declaration)) {
						continue;
					}

					BuiltinNumericTypeSpecifier inTypeSpecifier = inDeclarationMatcher.getNodeMatch("type",
						BuiltinNumericTypeSpecifier.class);
					for (DeclarationMember inDeclarationMember : inDeclarationMatcher
						.getNodeMatch("name*", DeclarationMember.class)
						.getAncestor(TypeAndInitDeclaration.class)
						.getMembers()) {
						String name = inDeclarationMember.getName().getName();
						if (name.startsWith("gl_")) {
							continue;
						}

						// patch missing declarations with an initialization
						if (!outDeclarations.containsKey(name)) {
							// make sure the declared in is actually used
							if (!currentRoot.identifierIndex.getAncestors(name, ReferenceExpression.class).findAny().isPresent()) {
								continue;
							}

							if (inTypeSpecifier == null) {
								LOGGER.warn(
									"The in declaration '" + name + "' in the " + currentType.glShaderType.name()
										+ " shader that has a missing corresponding out declaration in the previous stage "
										+ prevType.name()
										+ " has a non-numeric type and could not be compatibility-patched. See debugging.md for more information.");
								continue;
							}
							Type inType = inTypeSpecifier.type;

							// insert the new out declaration but copy over the type qualifiers, except for
							// the in/out qualifier
							TypeQualifier outQualifier = (TypeQualifier) inDeclarationMatcher
								.getNodeMatch("qualifier").cloneInto(prevRoot);
							makeQualifierOut(outQualifier);
							prevTree.injectNode(ASTInjectionPoint.BEFORE_DECLARATIONS, declarationTemplate.getInstanceFor(prevRoot,
								outQualifier,
								inTypeSpecifier.cloneInto(prevRoot),
								new Identifier(name)));

							// add the initializer to the main function
							prevTree.prependMainFunctionBody(getInitializer(prevRoot, name, inType));

							// update out declarations to prevent duplicates
							outDeclarations.put(name, null);

							LOGGER.warn(
								"The in declaration '" + name + "' in the " + currentType.glShaderType.name()
									+ " shader is missing a corresponding out declaration in the previous stage "
									+ prevType.name()
									+ " and has been compatibility-patched. See debugging.md for more information.");
						}

						// patch mismatching declaration with a local variable and a cast
						else {
							// there is an out declaration for this in declaration, check if the types match
							BuiltinNumericTypeSpecifier outTypeSpecifier = outDeclarations.get(name);

							// skip newly inserted out declarations
							if (outTypeSpecifier == null) {
								continue;
							}

							Type inType = inTypeSpecifier.type;
							Type outType = outTypeSpecifier.type;

							// check if the out declaration is an array-type, if so, skip it.
							// this only checks the out declaration because it's the one that when it's an
							// array type means that both declarations are arrays and we're not just in the
							// case of a geometry shader where the in declaration is an array and the out
							// declaration is not
							if (outTypeSpecifier.getArraySpecifier() != null) {
								LOGGER.warn(
									"The out declaration '" + name + "' in the " + prevPatchTypes.glShaderType.name()
										+ " shader that has a missing corresponding in declaration in the next stage "
										+ type.name()
										+ " has an array type and could not be compatibility-patched. See debugging.md for more information.");
								continue;
							}

							// skip if the type matches, nothing has to be done
							if (inType == outType) {
								// if the types match but it's never assigned a value,
								// an initialization is added
								if (prevRoot.identifierIndex.get(name).size() > 1) {
									continue;
								}

								// add an initialization statement for this declaration
								prevTree.prependMainFunctionBody(getInitializer(prevRoot, name, inType));
								outDeclarations.put(name, null);

								LOGGER.warn(
									"The in declaration '" + name + "' in the " + currentType.glShaderType.name()
										+ " shader that is never assigned to in the previous stage "
										+ prevType.name()
										+ " has been compatibility-patched by adding an initialization for it. See debugging.md for more information.");
								continue;
							}

							// bail and warn on mismatching dimensionality
							if (outType.getDimension() != inType.getDimension()) {
								LOGGER.warn(
									"The in declaration '" + name + "' in the " + currentType.glShaderType.name()
										+ " shader has a mismatching dimensionality (scalar/vector/matrix) with the out declaration in the previous stage "
										+ prevType.name()
										+ " and could not be compatibility-patched. See debugging.md for more information.");
								continue;
							}

							boolean isVector = outType.isVector();

							// rename all references of this out declaration to a new name (iris_)
							String newName = tagPrefix + name;
							prevRoot.identifierIndex.rename(name, newName);

							// rename the original out declaration back to the original name
							TypeAndInitDeclaration outDeclaration = outTypeSpecifier.getAncestor(TypeAndInitDeclaration.class);
							if (outDeclaration == null) {
								continue;
							}

							List<DeclarationMember> outMembers = outDeclaration.getMembers();
							DeclarationMember outMember = null;
							for (DeclarationMember member : outMembers) {
								if (member.getName().getName().equals(newName)) {
									outMember = member;
								}
							}
							if (outMember == null) {
								throw new TransformationException("The targeted out declaration member is missing!");
							}
							outMember.getName().replaceByAndDelete(new Identifier(name));

							// move the declaration member out of the declaration in case there is more than
							// one member to avoid changing the other member's type as well.
							if (outMembers.size() > 1) {
								outMember.detach();
								outTypeSpecifier = outTypeSpecifier.cloneInto(prevRoot);
								DeclarationExternalDeclaration singleOutDeclaration = (DeclarationExternalDeclaration) declarationTemplate
									.getInstanceFor(prevRoot,
										makeQualifierOut(outDeclaration.getType().getTypeQualifier().cloneInto(prevRoot)),
										outTypeSpecifier,
										new Identifier(name));
								((TypeAndInitDeclaration) singleOutDeclaration.getDeclaration()).getMembers().set(0, outMember);
								prevTree.injectNode(ASTInjectionPoint.BEFORE_DECLARATIONS, singleOutDeclaration);
							}

							// add a global variable with the new name and the old type
							prevTree.injectNode(ASTInjectionPoint.BEFORE_DECLARATIONS, variableTemplate.getInstanceFor(prevRoot,
								outTypeSpecifier.cloneInto(prevRoot),
								new Identifier(newName)));

							// insert a statement at the end of the main function that sets the value of the
							// out declaration to the value of the global variable and does a type cast
							prevTree.appendMainFunctionBody(
								(isVector && outType.getDimensions()[0] < inType.getDimensions()[0] ? statementTemplateVector
									: statementTemplate).getInstanceFor(prevRoot,
									new Identifier(name),
									new Identifier(newName),
									inTypeSpecifier.cloneInto(prevRoot)));

							// make the out declaration use the same type as the fragment shader
							outTypeSpecifier.replaceByAndDelete(inTypeSpecifier.cloneInto(prevRoot));

							// don't do the patch twice
							outDeclarations.put(name, null);

							LOGGER.warn(
								"The out declaration '" + name + "' in the " + prevType.name()
									+ " shader has a different type " + outType.getMostCompactName()
									+ " than the corresponding in declaration of type " + inType.getMostCompactName()
									+ " in the following stage " + currentType.glShaderType.name()
									+ " and has been compatibility-patched. See debugging.md for more information.");
						}
					}
				}
			}

			prevType = type;
		}
	}

	public static void transformFragmentCore(ASTParser t, TranslationUnit tree, Root root, Parameters parameters) {
		// do layout attachment (attaches a location(layout = 4) to the out declaration
		// outColor4 for example)

		// iterate the declarations
		ArrayList<NewDeclarationData> newDeclarationData = new ArrayList<>();
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
				if (!name.startsWith(attachTargetPrefix)) {
					continue;
				}

				// get the number suffix after the prefix
				String numberSuffix = name.substring(attachTargetPrefix.length());
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
				if (number < 0 || 7 < number) {
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

		// generate new declarations with layout qualifiers for each outColor member
		ArrayList<ExternalDeclaration> newDeclarations = new ArrayList<>();

		// Note: since everything is wrapped in a big Root.indexBuildSession, we don't
		// need to do it manually here
		for (NewDeclarationData data : newDeclarationData) {
			DeclarationMember member = data.member;
			member.detach();
			TypeQualifier newQualifier = data.qualifier.cloneInto(root);
			newQualifier.getChildren()
				.add(new LayoutQualifier(Stream.of(new NamedLayoutQualifierPart(
					new Identifier("location"),
					new LiteralExpression(Type.INT32, data.number)))));
			ExternalDeclaration newDeclaration = layoutedOutDeclarationTemplate.getInstanceFor(root,
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

	record NewDeclarationData(TypeQualifier qualifier, TypeSpecifier type, DeclarationMember member, int number) {
	}
}
