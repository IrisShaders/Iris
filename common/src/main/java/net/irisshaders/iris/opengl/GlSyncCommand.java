package net.irisshaders.iris.opengl;

import net.irisshaders.iris.backend.SyncCommand;
import net.irisshaders.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL43C;

import java.util.Objects;

final class GlSyncCommand implements SyncCommand {
	private int barriers;
	private Stage stage;

	@Override
	public SyncCommand read(Object object, Access access) {
		Objects.requireNonNull(object);
		this.barriers |= switch (access) {
			case SHADER ->
				GL42C.GL_TEXTURE_FETCH_BARRIER_BIT | GL42C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL43C.GL_SHADER_STORAGE_BARRIER_BIT;
			case UNIFORM -> GL42C.GL_UNIFORM_BARRIER_BIT;
			case VERTEX -> GL42C.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT;
			case INDEX -> GL42C.GL_ELEMENT_ARRAY_BARRIER_BIT;
			case INDIRECT -> GL42C.GL_COMMAND_BARRIER_BIT;
			case COLOR_ATTACHMENT, DEPTH_ATTACHMENT -> GL42C.GL_FRAMEBUFFER_BARRIER_BIT;
			case TRANSFER -> GL42C.GL_TEXTURE_UPDATE_BARRIER_BIT | GL42C.GL_BUFFER_UPDATE_BARRIER_BIT;
		};

		return this;
	}

	@Override
	public SyncCommand write(Object object, Access access) {
		return this.read(object, access);
	}

	@Override
	public SyncCommand nextIs(Stage stage) {
		this.stage = Objects.requireNonNull(stage);

		return this;
	}

	@Override
	public void run() {
		if (this.stage == null) {
			throw new IllegalStateException("missing synchronization stage");
		}

		if (this.barriers != 0) {
			IrisRenderSystem.memoryBarrier(this.barriers);
		}

		this.barriers = 0;
		this.stage = null;
	}
}
