package net.irisshaders.iris.pbr;

import com.mojang.blaze3d.textures.GpuTextureView;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.state.StateUpdateNotifiers;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;

public class TextureTracker {
	public static final TextureTracker INSTANCE = new TextureTracker();

	private static Runnable bindTextureListener;

	static {
		StateUpdateNotifiers.bindTextureNotifier = listener -> bindTextureListener = listener;
	}

	private final Int2ObjectMap<AbstractTexture> textures = new Int2ObjectOpenHashMap<>();

	private boolean lockBindCallback;

	private TextureTracker() {
	}

	public void trackTexture(int id, AbstractTexture texture) {
		textures.put(id, texture);
	}

	@Nullable
	public AbstractTexture getTexture(int id) {
		return textures.get(id);
	}

	public void onSetShaderTexture(int unit, GpuTextureView id) {
		if (lockBindCallback) {
			return;
		}
		if (unit == 0) {
			lockBindCallback = true;
			if (bindTextureListener != null) {
				bindTextureListener.run();
			}
			WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
			if (pipeline != null) {
				pipeline.onSetShaderTexture(id);
			}
			// Reset texture state
			IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), 0, id == null ? 0 : id.texture().iris$getGlId());
			lockBindCallback = false;
		}
	}

	public void onDeleteTexture(int id) {
		textures.remove(id);
	}
}
