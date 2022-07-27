package net.coderbot.iris.pipeline.transform;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 1L;
	private final int maxSize;

	public LRUCache(int maxSize, float loadFactor) {
		super((int) Math.ceil((float) maxSize / loadFactor) + 1, loadFactor, true);
		this.maxSize = maxSize;
	}

	public LRUCache(int maxSize) {
		this(maxSize, 0.75f);
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}
}
