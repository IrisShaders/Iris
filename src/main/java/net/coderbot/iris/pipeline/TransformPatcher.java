package net.coderbot.iris.pipeline;

import org.antlr.v4.runtime.Token;

import io.github.douira.glsl_transformer.core.*;
import io.github.douira.glsl_transformer.core.target.ThrowTargetImpl;
import io.github.douira.glsl_transformer.print.filter.*;
import io.github.douira.glsl_transformer.transform.*;
import net.coderbot.iris.gl.shader.ShaderType;

/**
 * The transform patcher (triforce 2) uses glsl-transformer to do shader
 * transformation.
 * 
 * NOTE: This patcher expects (and ensures) that the string doesn't contain any
 * (!) preprocessor directives. The only allowed ones are #extension and #pragma
 * as they are considered "parsed" directives. If any other directive appears in
 * the string, it will throw.
 * 
 * TODO: JCPP has to be configured to remove preprocessor directives entirely
 */
public class TransformPatcher extends Patcher {

	private TransformationManager<Parameters> manager;

	private static enum Patch {
		EXAMPLE
	}

	private static class Parameters extends JobParameters {
		public final Patch patch;
		public final ShaderType type;

		public Parameters(Patch patch, ShaderType type) {
			this.patch = patch;
			this.type = type;
		}

		@Override
		public boolean equals(JobParameters other) {
			if (other instanceof Parameters) {
				Parameters otherParams = (Parameters) other;
				return otherParams.patch == patch && otherParams.type == type;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return patch.hashCode() ^ type.hashCode();
		}
	}

	private static class ExampleParameters extends Parameters {
		public final Object thing;

		public ExampleParameters(Patch patch, ShaderType type, Object thing) {
			super(patch, type);
			this.thing = thing;
		}
	}

	{
		TokenFilter<Parameters> parseTokenFilter = new ChannelFilter<Parameters>(TokenChannel.PREPROCESSOR) {
			@Override
			public boolean isTokenAllowed(Token token) {
				if (!super.isTokenAllowed(token)) {
					throw new SemanticException("Unparsed preprocessor directives such as '" + token.getText()
							+ "' may not be present at this stage of shader processing!");
				}
				return true;
			}
		};

		LifecycleUser<Parameters> detectReserved = new SearchTerminals<Parameters>()
				.singleTarget(
						new ThrowTargetImpl<Parameters>(
								"iris_",
								"Detected a potential reference to unstable and internal Iris shader interfaces (iris_). This isn't currently supported."))
				.requireFullMatch(false);

		// #region examplePatch
		LifecycleUser<Parameters> wrapExample = new WrapIdentifier<Parameters>()
				.wrapTarget("gl_NormalMatrix")
				.detectionResult("u_NormalMatrix")
				.parsedReplacement("mat3(u_NormalMatrix)")
				.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
				.injectionExternalDeclaration("uniform mat4 u_NormalMatrix;");
		// #endregion examplePatch

		manager = new TransformationManager<Parameters>(new Transformation<Parameters>() {
			@Override
			protected void setupGraph() {
				Patch patch = getJobParameters().patch;
				ShaderType type = getJobParameters().type;

				addEndDependent(detectReserved);

				// patchExample
				if (patch == Patch.EXAMPLE) {
					if (type == ShaderType.VERTEX) {
						addEndDependent(wrapExample);
					}
				}
			}
		});

		manager.setParseTokenFilter(parseTokenFilter);
	}

	private String transform(String source, Parameters parameters) {
		String result = manager.transform(source, parameters);
		// TODO: optionally logging here
		return result;
	}

	@Override
	public String patchExampleInternal(String source, ShaderType type, Object thing) {
		return transform(source, new ExampleParameters(Patch.EXAMPLE, type, thing));
	}
}
