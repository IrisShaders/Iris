package net.irisshaders.iris.gl.image;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;

import java.util.function.Supplier;

public interface ImageHolder {
	boolean hasImage(String name);

	void addTextureImage(Supplier<GpuTextureView> texture, InternalTextureFormat format, String name);

	void addImage(Supplier<IrisImage> image, InternalTextureFormat format, String name);
}
