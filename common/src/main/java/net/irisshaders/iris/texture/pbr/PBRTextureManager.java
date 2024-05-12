package net.irisshaders.iris.texture.pbr;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.state.StateUpdateNotifiers;
import net.irisshaders.iris.mixin.GlStateManagerAccessor;
import net.irisshaders.iris.targets.backed.NativeImageBackedSingleColorTexture;
import net.irisshaders.iris.texture.TextureTracker;
import net.irisshaders.iris.texture.pbr.loader.PBRTextureLoader;
import net.irisshaders.iris.texture.pbr.loader.PBRTextureLoader.PBRTextureConsumer;
import net.irisshaders.iris.texture.pbr.loader.PBRTextureLoaderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class PBRTextureManager {
	public static final PBRTextureManager INSTANCE = new PBRTextureManager();


	private static Runnable normalTextureChangeListener;
	private static Runnable specularTextureChangeListener;

	static {
		StateUpdateNotifiers.normalTextureChangeNotifier = listener -> normalTextureChangeListener = listener;
		StateUpdateNotifiers.specularTextureChangeNotifier = listener -> specularTextureChangeListener = listener;
	}

	private final Int2ObjectMap<PBRTextureHolder> holders = new Int2ObjectOpenHashMap<>();
	private final PBRTextureConsumerImpl consumer = new PBRTextureConsumerImpl();

	private NativeImageBackedSingleColorTexture defaultNormalTexture;
	private NativeImageBackedSingleColorTexture defaultSpecularTexture;
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

	private PBRTextureManager() {
	}

	private static void dumpTexture(Dumpable dumpable, ResourceLocation id, Path path) {
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
		texture.releaseId();
	}

	public static void notifyPBRTexturesChanged() {
		if (normalTextureChangeListener != null) {
			normalTextureChangeListener.run();
		}

		if (specularTextureChangeListener != null) {
			specularTextureChangeListener.run();
		}
	}

	public void init() {
		defaultNormalTexture = new NativeImageBackedSingleColorTexture(PBRType.NORMAL.getDefaultValue());
		defaultSpecularTexture = new NativeImageBackedSingleColorTexture(PBRType.SPECULAR.getDefaultValue());
	}

	public PBRTextureHolder getHolder(int id) {
		PBRTextureHolder holder = holders.get(id);
		if (holder == null) {
			return defaultHolder;
		}
		return holder;
	}

	public PBRTextureHolder getOrLoadHolder(int id) {
		PBRTextureHolder holder = holders.get(id);
		if (holder == null) {
			holder = loadHolder(id);
			holders.put(id, holder);
		}
		return holder;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private PBRTextureHolder loadHolder(int id) {
		AbstractTexture texture = TextureTracker.INSTANCE.getTexture(id);
		if (texture != null) {
			Class<? extends AbstractTexture> clazz = texture.getClass();
			PBRTextureLoader loader = PBRTextureLoaderRegistry.INSTANCE.getLoader(clazz);
			if (loader != null) {
				int previousTextureBinding = GlStateManagerAccessor.getTEXTURES()[GlStateManagerAccessor.getActiveTexture()].binding;
				consumer.clear();
				try {
					loader.load(texture, Minecraft.getInstance().getResourceManager(), consumer);
					return consumer.toHolder();
				} catch (Exception e) {
					Iris.logger.debug("Failed to load PBR textures for texture " + id, e);
				} finally {
					GlStateManager._bindTexture(previousTextureBinding);
				}
			}
		}
		return defaultHolder;
	}

	public void onDeleteTexture(int id) {
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
		for (PBRTextureHolder holder : holders.values()) {
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

	private record PBRTextureHolderImpl(AbstractTexture normalTexture,
										AbstractTexture specularTexture) implements PBRTextureHolder {
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
