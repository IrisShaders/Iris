package net.irisshaders.iris.shaderpack.error;

import org.apache.commons.lang3.StringUtils;

public record RusticError(String severity, String message, String detailMessage, String file, int lineNumber,
						  String badLine) {


	@Override
	public String toString() {
		return severity + ": " + message + "\n"
			+ " --> " + file + ":" + lineNumber + "\n"
			+ "  |\n"
			+ "  | " + badLine + "\n"
			+ "  | " + StringUtils.repeat('^', badLine.length()) + " " + detailMessage + "\n"
			+ "  |";
	}
}
