package net.coderbot.iris.shaderpack.transform;

public interface Transformations {
	boolean contains(String content);
	void injectLine(InjectionPoint at, String line);
	void replaceExact(String from, String to);

	enum InjectionPoint {
		AFTER_VERSION
	}
}
