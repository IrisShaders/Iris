// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl;

public abstract class GlObject {
	private static final int INVALID_HANDLE = Integer.MIN_VALUE;

	private int handle;

	protected final void setHandle(int handle) {
		this.handle = handle;
	}

	public final int getHandle() {
		this.checkHandle();

		return this.handle;
	}

	protected final void checkHandle() {
		if (!this.isHandleValid()) {
			throw new IllegalStateException("Handle is not valid");
		}
	}

	protected final boolean isHandleValid() {
		return this.handle != INVALID_HANDLE;
	}

	public final void invalidateHandle() {
		this.handle = INVALID_HANDLE;
	}
}
