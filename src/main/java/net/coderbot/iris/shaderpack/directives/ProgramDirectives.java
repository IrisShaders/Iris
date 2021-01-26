package net.coderbot.iris.shaderpack.directives;

import java.util.Optional;

import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.parse.CommentDirectiveParser;
import net.coderbot.iris.shaderpack.parse.ShaderProperties;

public class ProgramDirectives {
	private int[] drawBuffers;
	private float viewportScale;


	public ProgramDirectives(ShaderPack.ProgramSource source, ShaderProperties properties) {
		// First try to find it in the fragment source, then in the vertex source.
		// If there's no explicit declaration, then by default /* DRAWBUFFERS:0 */ is inferred.
		drawBuffers = findDrawbuffersDirective(source.getFragmentSource())
			.orElseGet(() -> findDrawbuffersDirective(source.getVertexSource()).orElse(new int[]{0}));
		viewportScale = 1.0f;

		if (properties != null) {
			viewportScale = properties.viewportScaleOverrides.getOrDefault(source.getName(), 1.0f);
		}
	}

	private static Optional<int[]> findDrawbuffersDirective(Optional<String> stageSource) {
		return stageSource
			.flatMap(fragment -> CommentDirectiveParser.findDirective(fragment, "DRAWBUFFERS"))
			.map(String::toCharArray)
			.map(ProgramDirectives::parseDigits);
	}

	private static int[] parseDigits(char[] directiveChars) {
		int[] buffers = new int[directiveChars.length];
		int index = 0;

		for (char buffer : directiveChars) {
			buffers[index++] = Character.digit(buffer, 10);
		}

		return buffers;
	}

	public int[] getDrawBuffers() {
		return drawBuffers;
	}

	public float getViewportScale() {
		return viewportScale;
	}
}
