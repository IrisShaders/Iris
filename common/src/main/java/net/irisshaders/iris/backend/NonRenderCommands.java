package net.irisshaders.iris.backend;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.List;

public interface NonRenderCommands {
	void dispatch(ComputePipeline pipeline, Vector3i groups, FilledIndirectPointer indirect);

	void generateMipmaps(GpuTexture texture, boolean linear);

	void generateDepthMips(GpuTexture texture);

	default void copyTexture(GpuTexture source, GpuTexture destination, int width, int height) {
		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(source, destination, 0, 0, 0, 0, 0, width, height);
	}

	void clearColors(List<GpuTextureView> textures, Vector4f color);
}
