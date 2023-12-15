package net.coderbot.iris.gl.debug;

import net.coderbot.iris.Iris;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL42C;

public class TimerQuery {
	private final int query;
	private boolean inUse = false;

	private static boolean isInQuery = false;
	private static String name;

	public TimerQuery(int query) {
		this.query = query;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void startQuery(String name) {
		if (inUse || isInQuery) {
			throw new IllegalStateException("Query " + name + " already in use");
		}
		TimerQuery.name = name;

		inUse = true;
		isInQuery = true;

		GL42C.glBeginQuery(GL42C.GL_TIME_ELAPSED, query);
	}

	public void startMonitoring() {
		if (!inUse) {
			throw new IllegalStateException("Not in use, giving up");
		}

		isInQuery = false;
		GL42C.glEndQuery(GL42C.GL_TIME_ELAPSED);

		TimerQuerier.monitorQuery(name, this);
	}

	@ApiStatus.Internal
	public int returnResult() {
		return GL42C.glGetQueryObjecti(query, GL42C.GL_QUERY_RESULT);
	}

	protected void stopUsing() {
		if (!inUse) {
			throw new IllegalStateException("Query not in use");
		}

		inUse = false;
	}
}
