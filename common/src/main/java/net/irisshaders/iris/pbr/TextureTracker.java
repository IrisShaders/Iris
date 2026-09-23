package net.irisshaders.iris.pbr;

import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import net.irisshaders.iris.gl.state.StateUpdateNotifiers;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public final class TextureTracker {
	public static final TextureTracker INSTANCE = new TextureTracker();

	private static Runnable bindTextureListener;

	static {
		StateUpdateNotifiers.bindTextureNotifier = listener -> bindTextureListener = listener;
	}

	private final Map<GpuTexture, AbstractTexture> textures = new WeakHashMap<>();
	private final Map<GpuTexture, Integer> identities = new WeakHashMap<>();
	private GpuTexture boundTexture;
	private int nextIdentity = 1;

	private TextureTracker() {
	}

	public int identity(GpuTexture texture) {
		return texture == null ? 0 : this.identities.computeIfAbsent(texture, unused -> this.nextIdentity++);
	}

	public void trackTexture(GpuTexture texture, AbstractTexture owner) {
		this.textures.put(texture, owner);
	}

	public @Nullable AbstractTexture getTexture(GpuTexture texture) {
		return this.textures.get(texture);
	}

	public @Nullable GpuTexture getBoundTexture() {
		return this.boundTexture;
	}

	public void bindTexture(@Nullable GpuTexture texture) {
		if (this.boundTexture == texture) {
			return;
		}

		this.boundTexture = texture;

		if (bindTextureListener != null) {
			bindTextureListener.run();
		}
	}

	public void onDeleteTexture(GpuTexture texture) {
		this.textures.remove(texture);
		this.identities.remove(texture);

		if (this.boundTexture == texture) {
			this.bindTexture(null);
		}
	}

	public void onDeleteTexture(int glId) {
		var deleted = this.textures.keySet().stream().filter(t -> t instanceof GlTexture gl && gl.glId() == glId).toList();
		deleted.forEach(this::onDeleteTexture);
	}
}
