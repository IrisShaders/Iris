package net.coderbot.iris.postprocess;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.coderbot.iris.gl.program.Program;
import org.lwjgl.opengl.GL20C;

public class CompositeRenderPass {
	private static final int NUM_COLOR_ATTACHMENTS = 8;

	private final Program program;
	private final ColorAttachmentSettings[] attachmentSettings;

	public CompositeRenderPass(Program program) {
		this.program = program;
		this.attachmentSettings = new ColorAttachmentSettings[NUM_COLOR_ATTACHMENTS];

		for (int i = 0; i < NUM_COLOR_ATTACHMENTS; i++) {
			this.attachmentSettings[i] = new ColorAttachmentSettings();
		}
	}

	public void begin() {
		IntList drawBuffers = new IntArrayList();

		for (int i = 0; i < NUM_COLOR_ATTACHMENTS; i++) {
			if (attachmentSettings[i].isDrawBuffer) {
				drawBuffers.add(i);
			}
		}

		GL20C.glDrawBuffers(drawBuffers.toIntArray());
	}

	public void end() {

	}

	public static class ColorAttachmentSettings {
		boolean isDrawBuffer = false;
		boolean needsFlip = true;
	}
}
