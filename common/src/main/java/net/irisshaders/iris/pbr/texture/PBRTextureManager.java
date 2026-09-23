package net.irisshaders.iris.pbr.texture;

import com.mojang.renderpearl.api.textures.GpuTexture;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.state.StateUpdateNotifiers;
import net.irisshaders.iris.pbr.TextureTracker;
import net.irisshaders.iris.pbr.loader.PBRTextureLoader;
import net.irisshaders.iris.pbr.loader.PBRTextureLoader.PBRTextureConsumer;
import net.irisshaders.iris.pbr.loader.PBRTextureLoaderRegistry;
import net.irisshaders.iris.targets.backed.NativeImageBackedSingleColorTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PBRTextureManager {
	public static final PBRTextureManager INSTANCE = new PBRTextureManager();


	private static Runnable normalTextureChangeListener;
	private static Runnable specularTextureChangeListener;

	static {
		StateUpdateNotifiers.normalTextureChangeNotifier = listener -> normalTextureChangeListener = listener;
		StateUpdateNotifiers.specularTextureChangeNotifier = listener -> specularTextureChangeListener = listener;
	}

	private final Set<GpuTexture> toLoadNextFrame = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<GpuTexture, PBRTextureHolder> holders = new IdentityHashMap<>();
	private final PBRTextureConsumerImpl consumer = new PBRTextureConsumerImpl();
	// Not PBRTextureHolderImpl to directly reference fields
	private final PBRTextureHolder defaultHolder = new PBRTextureHolder() {
		@Override
		public @NotNull AbstractTexture normalTexture() {
			return defaultNormalTexture;
		}

		@Override
		public @NotNull AbstractTexture specularTexture() {
			return defaultSpecularTexture;
		}
	};
	private NativeImageBackedSingleColorTexture defaultNormalTexture;
	private NativeImageBackedSingleColorTexture defaultSpecularTexture;

	private PBRTextureManager() {
	}

	private static void dumpTexture(Dumpable dumpable, Identifier id, Path path) {
		try {
			dumpable.dumpContents(id, path);
		} catch (IOException e) {
			Iris.logger.error("Failed to dump texture {}", id, e);
		}
	}

	private static void closeTexture(AbstractTexture texture) {
		try {
			texture.close();
		} catch (Exception e) {
			//
		}
	}

	public static void notifyPBRTexturesChanged() {
		if (normalTextureChangeListener != null) {
			normalTextureChangeListener.run();
		}

		if (specularTextureChangeListener != null) {
			specularTextureChangeListener.run();
		}
	}

	public void onNewFrame() {
		if (!toLoadNextFrame.isEmpty()) {
			var pending = List.copyOf(this.toLoadNextFrame);
			this.toLoadNextFrame.clear();

			var it = pending.iterator();
			while (it.hasNext()) {
				GpuTexture id = it.next();

				if (id.isClosed()) {
					continue;
				}

				PBRTextureHolder holder = loadHolder(id);
				holders.put(id, holder);
			}
		}
	}

	public void init() {
		defaultNormalTexture = new NativeImageBackedSingleColorTexture(PBRType.NORMAL.getDefaultValue());
		defaultSpecularTexture = new NativeImageBackedSingleColorTexture(PBRType.SPECULAR.getDefaultValue());
	}

	public PBRTextureHolder getDefaultHolder() {
		return defaultHolder;
	}

	public PBRTextureHolder getHolder(GpuTexture id) {
		PBRTextureHolder holder = holders.get(id);
		if (holder == null) {
			return defaultHolder;
		}
		return holder;
	}

	/**
	 * will return next frame if there isn't any!
	 */
	public PBRTextureHolder getOrLoadHolder(GpuTexture id) {
		PBRTextureHolder holder = holders.get(id);
		if (holder == null) {
			toLoadNextFrame.add(id);
			return defaultHolder;
		}
		return holder;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private PBRTextureHolder loadHolder(GpuTexture id) {
		AbstractTexture texture = TextureTracker.INSTANCE.getTexture(id);
		if (texture != null) {
			Class<? extends AbstractTexture> clazz = texture.getClass();
			PBRTextureLoader loader = PBRTextureLoaderRegistry.INSTANCE.getLoader(clazz);
			if (loader != null) {
				consumer.clear();
				try {
					loader.load(texture, Minecraft.getInstance().getResourceManager(), consumer);
					return consumer.toHolder();
				} catch (Exception e) {
					Iris.logger.warn("Failed to load PBR textures for texture " + id, e);
				}
			}
		}
		return defaultHolder;
	}

	public void onDeleteTexture(GpuTexture id) {
		this.toLoadNextFrame.remove(id);

		PBRTextureHolder holder = holders.remove(id);
		if (holder != null) {
			closeHolder(holder);
		}
	}

	public void dumpTextures(Path path) {
		for (PBRTextureHolder holder : holders.values()) {
			if (holder != defaultHolder) {
				dumpHolder(holder, path);
			}
		}
	}

	private void dumpHolder(PBRTextureHolder holder, Path path) {
		AbstractTexture normalTexture = holder.normalTexture();
		AbstractTexture specularTexture = holder.specularTexture();
		if (normalTexture != defaultNormalTexture && normalTexture instanceof PBRDumpable dumpable) {
			dumpTexture(dumpable, dumpable.getDefaultDumpLocation(), path);
		}
		if (specularTexture != defaultSpecularTexture && specularTexture instanceof PBRDumpable dumpable) {
			dumpTexture(dumpable, dumpable.getDefaultDumpLocation(), path);
		}
	}

	public void clear() {
		this.toLoadNextFrame.clear();

		var previous = List.copyOf(this.holders.values());
		this.holders.clear();

		for (PBRTextureHolder holder : previous) {
			if (holder != defaultHolder) {
				closeHolder(holder);
			}
		}
		holders.clear();
	}

	public void close() {
		clear();
		defaultNormalTexture.close();
		defaultSpecularTexture.close();
	}

	private void closeHolder(PBRTextureHolder holder) {
		AbstractTexture normalTexture = holder.normalTexture();
		AbstractTexture specularTexture = holder.specularTexture();
		if (normalTexture != defaultNormalTexture) {
			closeTexture(normalTexture);
		}
		if (specularTexture != defaultSpecularTexture) {
			closeTexture(specularTexture);
		}
	}

	private record PBRTextureHolderImpl(AbstractTexture normalTexture, AbstractTexture specularTexture) implements PBRTextureHolder {
		@Override
		public @NotNull AbstractTexture normalTexture() {
			return normalTexture;
		}

		@Override
		public @NotNull AbstractTexture specularTexture() {
			return specularTexture;
		}
	}

	private class PBRTextureConsumerImpl implements PBRTextureConsumer {
		private AbstractTexture normalTexture;
		private AbstractTexture specularTexture;
		private boolean changed;

		@Override
		public void acceptNormalTexture(@NotNull AbstractTexture texture) {
			normalTexture = texture;
			changed = true;
		}

		@Override
		public void acceptSpecularTexture(@NotNull AbstractTexture texture) {
			specularTexture = texture;
			changed = true;
		}

		public void clear() {
			normalTexture = defaultNormalTexture;
			specularTexture = defaultSpecularTexture;
			changed = false;
		}

		public PBRTextureHolder toHolder() {
			if (changed) {
				return new PBRTextureHolderImpl(normalTexture, specularTexture);
			} else {
				return defaultHolder;
			}
		}
	}
}
