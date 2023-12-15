package net.coderbot.iris.gl.debug;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.coderbot.iris.Iris;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.opengl.GL32C;

import java.util.Set;

public class TimerQuerier {
	private static final Object2ObjectMap<String, TimerQuery>[] queries = new Object2ObjectOpenHashMap[5];
	private static int frameId = 0;

	static {
		for (int i = 0; i < 5; i++) {
			queries[i] = new Object2ObjectOpenHashMap<>();
		}
	}

	private static final Set<TimerQuery> unusedQueries = new ObjectArraySet<>();

	public static void initRenderer() {
		int[] queries = new int[100];
		GL32C.glGenQueries(queries);
		for (int i = 0; i < 100; i++) {
			unusedQueries.add(new TimerQuery(queries[i]));
		}
	}

	public static TimerQuery giveQuery() {
		if (unusedQueries.isEmpty()) {
			Iris.logger.warn("Congrats, you overran the query system. Adding a new query. (If this stops after a bit, it's not an error, you just have a lot going on)");
			unusedQueries.add(new TimerQuery(GL32C.glGenQueries()));
		}
		TimerQuery query = unusedQueries.iterator().next();
		unusedQueries.remove(query);
		return query;
	}


	public static void advanceFrameAndReset() {
		// Advance the frame ID
		frameId = (frameId + 1) % 5;

		int frameToGet = frameId - 4;
		if (frameToGet < 0) frameToGet += 5;

		queries[frameToGet].forEach((name, query) -> {
			if (Screen.hasControlDown()) {
				Iris.logger.warn("Query result for " + name + " was " + ((float) query.returnResult() / 1000000f) + "ms");
			}
			query.stopUsing();
			unusedQueries.add(query);
		});

		queries[frameToGet].clear();
	}

	public static void monitorQuery(String name, TimerQuery query) {
		queries[frameId].put(name, query);
	}
}
