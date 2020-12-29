package net.coderbot.iris.shaderpack.transform;

import java.util.function.Supplier;

class Tests {
	public static void main(String[] args) {
		test("basic injection", "#version\n//Injected\n//After", () -> {
			StringTransformations transformations = new StringTransformations("#version\n//After");

			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "//Injected");

			return transformations.toString();
		});

		test("inject with content before #version", "// Example prefix\n\n#version\n//Injected\n//After", () -> {
			StringTransformations transformations = new StringTransformations("// Example prefix\n\n#version\n//After");

			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "//Injected");

			return transformations.toString();
		});

		test("multiple injections", "// Example prefix\n\n#version\n//Injected\n//After", () -> {
			StringTransformations transformations = new StringTransformations("// Example prefix\n\n#version\n//After");

			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "//Injected");

			return transformations.toString();
		});
	}

	private static <T> void test(String name, T expected, Supplier<T> testCase) {
		T actual;

		try {
			actual = testCase.get();
		} catch (Throwable e) {
			System.err.println("Test \"" + name + "\" failed with an exception:");
			e.printStackTrace();

			return;
		}

		if (!expected.equals(actual)) {
			System.err.println("Test \"" + name + "\" failed: Expected " + expected + ", got " + actual);
		} else {
			System.out.println("Test \"" + name + "\" passed");
		}
	}
}
