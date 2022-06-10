package net.coderbot.iris.pipeline.transform;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import io.github.douira.glsl_transformer.core.CachePolicy;
import io.github.douira.glsl_transformer.core.WrapIdentifier;
import io.github.douira.glsl_transformer.transform.InjectionPoint;

/**
 * Users of this transformation have to insert irisMain(); themselves because it
 * can appear at varying positions in the new string.
 */
abstract class MainWrapper<R extends Parameters> extends WrapIdentifier<R> {
	protected abstract String getMainContent();

	{
		detectionResult("irisMain");
		wrapTarget("main");
		injectionLocation(InjectionPoint.BEFORE_EOF);
		injectionExternalDeclarations(CachePolicy.ON_JOB);
	}

	@Override
	protected Collection<String> getInjectionExternalDeclarations() {
		return ImmutableList.of("void main() { " + getMainContent() + " }");
	}
}
