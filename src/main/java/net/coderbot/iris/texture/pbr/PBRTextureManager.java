package net.coderbot.iris.texture.pbr;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.rendertarget.NativeImageBackedSingleColorTexture;
import net.coderbot.iris.texture.TextureTracker;
import net.coderbot.iris.texture.pbr.loader.PBRTextureLoader;
import net.coderbot.iris.texture.pbr.loader.PBRTextureLoader.PBRTextureConsumer;
import net.coderbot.iris.texture.pbr.loader.PBRTextureLoaderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.NotNull;

public class PBRTextureManager {
	public static final PBRTextureManager INSTANCE = new PBRTextureManager();

	public static final boolean DEBUG = System.getProperty("iris.pbr.debug") != null;

	// Using the nullary ctor or 0 causes errors
	private final ObjectArrayList<PBRTextureHolder> holders = new ObjectArrayList<>(ObjectArrayList.DEFAULT_INITIAL_CAPACITY);
	private final PBRTextureConsumerImpl consumer = new PBRTextureConsumerImpl();

	private NativeImageBackedSingleColorTexture defaultNormalTexture;
	private NativeImageBackedSingleColorTexture defaultSpecularTexture;
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

	public void init() {
		defaultNormalTexture = new NativeImageBackedSingleColorTexture(PBRType.NORMAL.getDefaultValue());
		defaultSpecularTexture = new NativeImageBackedSingleColorTexture(PBRType.SPECULAR.getDefaultValue());
	}

	public PBRTextureHolder getHolder(int id) {
		if (id < holders.size()) {
			PBRTextureHolder holder = holders.get(id);
			if (holder != null) {
				return holder;
			}
		}
		return defaultHolder;
	}

	public PBRTextureHolder getOrLoadHolder(int id) {
		if (id >= holders.size()) {
			holders.size(id + 1);
		}
		PBRTextureHolder holder = holders.get(id);
		if (holder == null) {
			holder = loadHolder(id);
			holders.set(id, holder);
		}
		return holder;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private PBRTextureHolder loadHolder(int id) {
		try {
			AbstractTexture texture = TextureTracker.INSTANCE.getTexture(id);
			if (texture != null) {
				Class<? extends AbstractTexture> clazz = texture.getClass();
				PBRTextureLoader loader = PBRTextureLoaderRegistry.INSTANCE.getLoader(clazz);
				if (loader != null) {
					loader.load(texture, Minecraft.getInstance().getResourceManager(), consumer);
					return consumer.toHolder();
				}
			}
		} catch (Exception e) {
			Iris.logger.debug("Failed to load PBR textures for texture " + id, e);
		} finally {
			consumer.clear();
		}
		return defaultHolder;
	}

	public void onDeleteTexture(int id) {
		if (id < holders.size()) {
			PBRTextureHolder holder = holders.set(id, null);
			if (holder != null) {
				closeHolder(holder);
			}
		}
	}

	public void clear() {
		for (PBRTextureHolder holder : holders) {
			if (holder != null) {
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
			try {
				normalTexture.close();
			} catch (Exception e) {
				//
			}
			normalTexture.releaseId();
		}
		if (specularTexture != defaultSpecularTexture) {
			try {
				specularTexture.close();
			} catch (Exception e) {
				//
			}
			specularTexture.releaseId();
		}
	}

	private class PBRTextureConsumerImpl implements PBRTextureConsumer {
		private AbstractTexture normalTexture;
		private AbstractTexture specularTexture;

		@Override
		public void acceptNormalTexture(AbstractTexture texture) {
			normalTexture = texture;
		}

		@Override
		public void acceptSpecularTexture(AbstractTexture texture) {
			specularTexture = texture;
		}

		private PBRTextureHolder toHolder() {
			if (normalTexture == null && specularTexture == null) {
				return defaultHolder;
			}
			if (normalTexture == null) {
				normalTexture = defaultNormalTexture;
			}
			if (specularTexture == null) {
				specularTexture = defaultSpecularTexture;
			}
			return new PBRTextureHolderImpl(normalTexture, specularTexture);
		}

		private void clear() {
			normalTexture = null;
			specularTexture = null;
		}
	}

	private static class PBRTextureHolderImpl implements PBRTextureHolder {
		private final AbstractTexture normalTexture;
		private final AbstractTexture specularTexture;

		public PBRTextureHolderImpl(AbstractTexture normalTexture, AbstractTexture specularTexture) {
			this.normalTexture = normalTexture;
			this.specularTexture = specularTexture;
		}

		@Override
		public @NotNull AbstractTexture normalTexture() {
			return normalTexture;
		}

		@Override
		public @NotNull AbstractTexture specularTexture() {
			return specularTexture;
		}
	}
}
