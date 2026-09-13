package net.irisshaders.iris.pbr;

import com.mojang.renderpearl.api.textures.GpuTexture;
import net.irisshaders.iris.gl.state.StateUpdateNotifiers;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public final class TextureTracker {
	public static final TextureTracker INSTANCE = new TextureTracker();
	private static Runnable bindTextureListener;

	static {
		StateUpdateNotifiers.bindTextureNotifier = listener -> bindTextureListener = listener;
	}

	private final Map<GpuTexture, AbstractTexture> textures = new IdentityHashMap<>();
	private GpuTexture boundTexture;
	private final Map<GpuTexture, Integer> identities = new IdentityHashMap<>();
	private int nextIdentity = 1;

	public int identity(GpuTexture texture) {
		return texture == null ? 0 : identities.computeIfAbsent(texture, unused -> nextIdentity++);
	}

	private TextureTracker() {
	}

	public void trackTexture(GpuTexture texture, AbstractTexture owner) {
		textures.put(texture, owner);
	}

	public @Nullable AbstractTexture getTexture(GpuTexture texture) {
		return textures.get(texture);
	}

	public @Nullable GpuTexture getBoundTexture() {
		return boundTexture;
	}

	public void bindTexture(@Nullable GpuTexture texture) {
		if (boundTexture == texture) {
			return;
		}

		boundTexture = texture;

		if (bindTextureListener != null) {
			bindTextureListener.run();
		}
	}

	public void onDeleteTexture(GpuTexture texture) {
		textures.remove(texture);
		identities.remove(texture);

		if (boundTexture == texture) {
			bindTexture(null);
		}
	}
}
