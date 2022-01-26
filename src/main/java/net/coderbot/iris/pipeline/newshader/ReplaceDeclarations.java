package net.coderbot.iris.pipeline.newshader;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.pattern.ParseTreeMatch;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.ExternalDeclarationContext;
import io.github.douira.glsl_transformer.GLSLParser.FunctionHeaderContext;
import io.github.douira.glsl_transformer.GLSLParser.TranslationUnitContext;
import io.github.douira.glsl_transformer.GLSLParser.VariableIdentifierContext;
import io.github.douira.glsl_transformer.transform.SemanticException;
import io.github.douira.glsl_transformer.transform.Transformation;
import io.github.douira.glsl_transformer.transform.WalkPhase;
import io.github.douira.glsl_transformer.tree.ExtendedContext;

//TODO: treat each found declaration with the same location=0 as the same declaration and replace all of them identically
/**
 * The declaration replacement finds layout declarations and replaces all
 * references to them with function calls and other code.
 * 
 * NOTE: this class is here because it was in glsl-transformer before but it's
 * actually not supposed to be part of that so I moved it.
 */
public class ReplaceDeclarations<T> extends Transformation<T> {
  private static class Declaration {
    private final String type;
    private final String name;

    public Declaration(String type, String name) {
      this.type = type;
      this.name = name;
    }

    public String getType() {
      return type;
    }

    public String getName() {
      return name;
    }
  }

  private Map<String, Declaration> declarations;

  @Override
  protected void resetState() {
    declarations = new HashMap<>();
  }

  /**
   * Creates a new declaration replacement transformation with a walk phase for
   * finding declarations and one for inserting calls to the generated functions.
   */
  {
    addPhase(new WalkPhase<T>() {
      ParseTreePattern declarationPattern;

      @Override
      protected void init() {
        declarationPattern = compilePattern("layout (location = 0) <type:storageQualifier> vec4 <name:IDENTIFIER>;",
            GLSLParser.RULE_externalDeclaration);
      }

      @Override
      public void enterExternalDeclaration(ExternalDeclarationContext ctx) {
        ParseTreeMatch match = declarationPattern.match(ctx);
        if (match.succeeded()) {
          // check for valid format and add to the list if it is valid
          String type = match.get("type").getText();
          String name = match.get("name").getText();

          if (name == "iris_Position") {
            throw new SemanticException(String.format("Disallowed GLSL declaration with the name \"{0}\"!", name), ctx);
          }

          if (type.equals("in") || type.equals("attribute")) {
            declarations.put(name, new Declaration(type, name));
            removeNode((ExtendedContext) match.getTree());
          }
        }
      }

      @Override
      public void enterFunctionHeader(FunctionHeaderContext ctx) {
        if (ctx.IDENTIFIER().getText().equals("iris_getModelSpaceVertexPosition")) {
          throw new SemanticException(
              String.format("Disallowed GLSL declaration with the name \"{0}\"!", "iris_getModelSpaceVertexPosition"),
              ctx);
        }
      }

      @Override
      protected boolean isActiveAfterWalk() {
        return !declarations.isEmpty();
      }

      @Override
      protected void afterWalk(TranslationUnitContext ctx) {
        // is only run if phase is found to be active
        // TODO: the function content and the new attribute declaration
        injectExternalDeclaration(InjectionPoint.BEFORE_EOF, "void iris_getModelSpaceVertexPosition() { }");
        injectExternalDeclaration(InjectionPoint.BEFORE_FUNCTIONS,
            "layout (location = 0) attribute vec4 iris_Position;");
      }
    });

    addPhase(new WalkPhase<T>() {
      @Override
      protected boolean isActive() {
        return !declarations.isEmpty();
      }

      @Override
      public void enterVariableIdentifier(VariableIdentifierContext ctx) {
        // check for one of the identifiers we're looking for
        TerminalNode identifier = ctx.IDENTIFIER();
        Declaration matchingDeclaration = declarations.get(identifier.getText());
        if (matchingDeclaration != null) {
          // perform replacement of this reference
          replaceNode(ctx, "iris_getModelSpaceVertexPosition()", GLSLParser::expression);
        }
      }
    });
  }
}
