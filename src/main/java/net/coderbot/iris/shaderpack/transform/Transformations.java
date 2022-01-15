package net.coderbot.iris.shaderpack.transform;

public interface Transformations {
	boolean contains(String content);
	void injectLine(InjectionPoint at, String line);
	void replaceExact(String from, String to);
	void replaceRegex(String regex, String to);
	String getPrefix();
	void setPrefix(String prefix);
	void define(String key, String value);

	enum InjectionPoint {
		/**
		 * in glsl-transformer: InjectionLocation.BEFORE_DIRECTIVES (but only roughly)
		 */
		DEFINES,

		/**
		 * in glsl-transformer: InjectionLocation.BEFORE_DECLARATIONS
		 */
		BEFORE_CODE,

		/**
		 * in glsl-transformer: InjectionLocation.BEFORE_EOF
		 */
		END
	}
}
