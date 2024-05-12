package net.irisshaders.iris.shaderpack.transform;

public class StringTransformations implements Transformations {
	private String prefix;
	private String extensions;
	private StringBuilder injections;
	private String body;
	private StringBuilder suffix;

	public StringTransformations(String base) {
		int versionStringStart = base.indexOf("#version");

		if (versionStringStart == -1) {
			throw new IllegalArgumentException("A valid shader should include a version string");
		}

		String prefix = base.substring(0, versionStringStart);
		base = base.substring(versionStringStart);

		int splitPoint = base.indexOf("\n") + 1;

		this.prefix = prefix + base.substring(0, splitPoint);
		this.extensions = "";
		this.injections = new StringBuilder();
		this.body = base.substring(splitPoint);
		this.suffix = new StringBuilder("\n");

		if (!body.contains("#extension")) {
			// Don't try to hoist #extension lines if there are none.
			return;
		}

		// We need to avoid injecting non-preprocessor code fragments before #extension
		// declarations. Luckily, JCPP hoists #extension directives to be right after #version
		// directives.
		StringBuilder extensions = new StringBuilder();
		StringBuilder body = new StringBuilder();

		boolean inBody = false;

		for (String line : this.body.split("\\R")) {
			String trimmedLine = line.trim();

			if (!trimmedLine.isEmpty()
				&& !trimmedLine.startsWith("#extension")
				&& !trimmedLine.startsWith("//")) {
				inBody = true;
			}

			if (inBody) {
				body.append(line);
				body.append('\n');
			} else {
				extensions.append(line);
				extensions.append('\n');
			}
		}

		this.extensions = extensions.toString();
		this.body = body.toString();
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public boolean contains(String content) {
		return toString().contains(content);
	}

	@Override
	public void define(String key, String value) {
		// TODO: This isn't super efficient, but oh well.
		extensions = extensions + "#define " + key + " " + value + "\n";
	}

	@Override
	public void injectLine(InjectionPoint at, String line) {
		if (at == InjectionPoint.BEFORE_CODE) {
			injections.append(line);
			injections.append('\n');
		} else if (at == InjectionPoint.DEFINES) {
			// TODO: This isn't super efficient, but oh well.
			extensions = extensions + line + "\n";
		} else if (at == InjectionPoint.END) {
			suffix.append(line);
			suffix.append('\n');
		} else {
			throw new IllegalArgumentException("Unsupported injection point: " + at);
		}
	}

	@Override
	public void replaceExact(String from, String to) {
		if (from.contains("\n")) {
			// Block newline replacements for now, since that could mean that we're trying to replace across
			// injections / body and that will result in weird behavior.
			throw new UnsupportedOperationException();
		}

		prefix = prefix.replace(from, to);
		extensions = extensions.replace(from, to);
		injections = new StringBuilder(injections.toString().replace(from, to));
		body = body.replace(from, to);
		suffix = new StringBuilder(suffix.toString().replace(from, to));
	}

	@Override
	public void replaceRegex(String regex, String to) {
		prefix = prefix.replaceAll(regex, to);
		extensions = extensions.replaceAll(regex, to);
		injections = new StringBuilder(injections.toString().replaceAll(regex, to));
		body = body.replaceAll(regex, to);
		suffix = new StringBuilder(suffix.toString().replaceAll(regex, to));
	}

	@Override
	public String toString() {
		return prefix + extensions + injections + body + suffix;
	}
}
