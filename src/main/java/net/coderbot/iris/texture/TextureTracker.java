package net.coderbot.iris.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.texture.pbr.PBRTextureManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;

public class TextureTracker {
	public static final TextureTracker INSTANCE = new TextureTracker();

	private static Runnable bindTextureListener;

	static {
		StateUpdateNotifiers.bindTextureNotifier = listener -> bindTextureListener = listener;
	}

	// Using the nullary ctor or 0 causes errors
	private final ObjectArrayList<AbstractTexture> textures = new ObjectArrayList<>(ObjectArrayList.DEFAULT_INITIAL_CAPACITY);

	private boolean lockBindCallback;

	private TextureTracker() {
	}

	public void trackTexture(int id, AbstractTexture texture) {
		if (id >= textures.size()) {
			textures.size(id + 1);
		}
		textures.set(id, texture);
	}

	@Nullable
	public AbstractTexture getTexture(int id) {
		if (id < textures.size()) {
			return textures.get(id);
		}
		return null;
	}

	public void onBindTexture(int id) {
		if (lockBindCallback) {
			return;
		}
		if (GlStateManagerAccessor.getActiveTexture() == 0) {
			lockBindCallback = true;
			if (bindTextureListener != null) {
				bindTextureListener.run();
			}
			WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
			if (pipeline != null) {
				pipeline.onBindTexture(id);
			}
			// Reset texture state
			GlStateManager._activeTexture(GL20C.GL_TEXTURE0);
			GlStateManager._bindTexture(id);
			lockBindCallback = false;
		}
	}

	public void onDeleteTexture(int id) {
		if (id < textures.size()) {
			textures.set(id, null);
		}
		PBRTextureManager.INSTANCE.onDeleteTexture(id);
	}
}
