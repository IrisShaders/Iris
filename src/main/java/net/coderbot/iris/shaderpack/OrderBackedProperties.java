package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

/**
 * Properties backed by a {@link java.util.LinkedHashMap}, in order to preserve iteration order
 */
public class OrderBackedProperties extends Properties {
	private transient final Map<Object, Object> backing = Object2ObjectMaps.synchronize(new Object2ObjectLinkedOpenHashMap<>());

	@Override
	public synchronized Object put(Object key, Object value) {
		backing.put(key, value);

		return super.put(key, value);
	}

	@Override
	public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
		this.backing.forEach(action);
	}
}
