package net.irisshaders.iris.shaderpack.transform;

public interface Transformations {
	boolean contains(String content);

	void injectLine(InjectionPoint at, String line);

	void replaceExact(String from, String to);

	void replaceRegex(String regex, String to);

	String getPrefix();

	void setPrefix(String prefix);

	void define(String key, String value);

	enum InjectionPoint {
		DEFINES,
		BEFORE_CODE,
		END
	}
}
