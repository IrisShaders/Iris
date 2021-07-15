package net.coderbot.iris.shaderpack.transform;

public class StringTransformations implements Transformations {
	private String prefix;
	private String extensions;
	private StringBuilder injections;
	private StringBuilder mainHead;
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
		this.mainHead = new StringBuilder();
		this.body = base.substring(splitPoint);
		this.suffix = new StringBuilder("\n");

		if (!body.contains("#extension")) {
			// Don't try to hoist #extension lines if there are none.
			return;
		}

		// We need to make a best effort avoid injecting non-preprocessor code fragments before #extension
		// declarations.
		//
		// Some strict drivers (like Mesa drivers) really do not like this.
		StringBuilder extensions = new StringBuilder();
		StringBuilder body = new StringBuilder();

		boolean inBody = false;

		for (String line : this.body.split("\\R")) {
			String trimmedLine = line.trim();

			if (!trimmedLine.isEmpty()
					&& !trimmedLine.startsWith("#extension")
					&& !trimmedLine.startsWith("#define")
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
		extensions = "#define " + key + " " + value + "\n" + extensions;
	}

	@Override
	public void injectLine(InjectionPoint at, String line) {
		if (at == InjectionPoint.BEFORE_CODE) {
			injections.append(line);
			injections.append('\n');
		} else if (at == InjectionPoint.DEFINES) {
			// TODO: This isn't super efficient, but oh well.
			extensions = line + "\n" + extensions;
		} else if (at == InjectionPoint.END) {
			suffix.append(line);
			suffix.append('\n');
		} else if (at == InjectionPoint.MAIN_HEAD) {
			mainHead.append(line);
			mainHead.append('\n');
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
		mainHead = new StringBuilder(mainHead.toString().replace(from, to));
		body = body.replace(from, to);
		suffix = new StringBuilder(suffix.toString().replace(from, to));
	}

	@Override
	public String toString() {
		String body = this.body;

		String mainInject = mainHead.toString();
		if(!mainInject.isEmpty()) {
			StringBuilder newBody = new StringBuilder();
			int splitPoint = body.indexOf("void main()");
			while(splitPoint >= 0) {
				splitPoint = findEndOfFunction(body, splitPoint);
				if(splitPoint == -1) {
					throw new RuntimeException("Main function is illegal");
				}

				newBody.append(body, 0, splitPoint);
				body = body.substring(splitPoint);

				newBody.append(mainInject);

				splitPoint = body.indexOf("void main()");
			}

			body = newBody.append(body).toString();
		}

		return prefix + extensions + injections + body + suffix;
	}

	private int findEndOfFunction(String code, int beginIndex) {
		int currentIndex = code.indexOf('{', beginIndex);
		if(currentIndex == -1) {
			return -1;
		}

		int blockDepth = 1;
		while(blockDepth > 0) {
			int nextOpen = code.indexOf('{', currentIndex + 1);
			int nextClose = code.indexOf('}', currentIndex + 1);

			if(nextClose == -1) {
				return -1;
			}
			if(nextOpen == -1 || nextOpen > nextClose) {
				currentIndex = nextClose;
				blockDepth--;
			} else {
				currentIndex = nextOpen;
				blockDepth++;
			}
		}
		return currentIndex;
	}
}
