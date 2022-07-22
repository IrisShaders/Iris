package net.coderbot.iris.pipeline.transform;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import io.github.douira.glsl_transformer.cst.core.CachePolicy;
import io.github.douira.glsl_transformer.cst.core.WrapIdentifier;
import io.github.douira.glsl_transformer.cst.transform.CSTInjectionPoint;

/**
 * Users of this transformation have to insert irisMain(); themselves because it
 * can appear at varying positions in the new string.
 */
abstract class MainWrapper<R extends Parameters> extends WrapIdentifier<R> {
	protected abstract String getMainContent();

	{
		detectionResult("irisMain");
		wrapTarget("main");
		injectionLocation(CSTInjectionPoint.END);
		injectionExternalDeclarations(CachePolicy.ON_JOB);
	}

	@Override
	protected Collection<String> getInjectionExternalDeclarations() {
		return ImmutableList.of("void main() { " + getMainContent() + " }");
	}
}
