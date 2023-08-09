package net.coderbot.iris.gl;

import org.jetbrains.annotations.ApiStatus;

public abstract class GlResource {
	private int id;
	private boolean isValid;

	protected GlResource(int id) {
		this.id = id;
		isValid = true;
	}

	public final void destroy() {
		destroyInternal();
		isValid = false;
	}

	protected abstract void destroyInternal();

	protected void assertValid() {
		if (!isValid) {
			throw new IllegalStateException("Tried to use a destroyed GlResource");
		}
	}

	protected int getGlId() {
		assertValid();

		return id;
	}

	@ApiStatus.Internal
	protected void setNewId(int id) {
		this.id = id;
		this.isValid = true;
	}
}
