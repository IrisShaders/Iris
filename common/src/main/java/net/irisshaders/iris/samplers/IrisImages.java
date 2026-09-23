package net.irisshaders.iris.samplers;

import com.google.common.collect.ImmutableSet;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.CustomImage;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.targets.RenderTargets;

import java.util.Set;
import java.util.function.Supplier;

public class IrisImages {
	public static void addRenderTargetImages(ImageHolder images, Supplier<ImmutableSet<Integer>> flipped, RenderTargets renderTargets) {
		for (int i = 0; i < renderTargets.getRenderTargetCount(); i++) {
			final int index = i;

			final String name = "colorimg" + i;

			if (!images.hasImage(name)) {
				continue;
			}

			renderTargets.createIfUnsure(index);
			renderTargets.getOrCreate(index).enableStorage();

			// Note: image bindings *are* impacted by buffer flips.
			Supplier<GpuTextureView> texture = () -> {
				ImmutableSet<Integer> flippedBuffers = flipped.get();
				RenderTarget target = renderTargets.getOrCreate(index);

				return target.getTextureView(flippedBuffers.contains(index), false);
			};

			final InternalTextureFormat internalFormat = renderTargets.getOrCreate(i).getInternalFormat();

			images.addTextureImage(texture, internalFormat, name);
		}
	}

	public static boolean hasShadowImages(ImageHolder images) {
		// TODO: Generalize
		if (images == null) {
			return false;
		}
		return images.hasImage("shadowcolorimg0") || images.hasImage("shadowcolorimg1");
	}

	public static boolean hasRenderTargetImages(ImageHolder images, RenderTargets targets) {
		for (int i = 0; i < targets.getRenderTargetCount(); i++) {
			if (images != null && images.hasImage("colorimg" + i)) {
				return true;
			}
		}
		return false;
	}

	public static void addShadowColorImages(ImageHolder images, ShadowRenderTargets shadowRenderTargets, ImmutableSet<Integer> flipped) {
		if (images == null) {
			return;
		}
		for (int i = 0; i < shadowRenderTargets.getNumColorTextures(); i++) {
			final int index = i;

			if (!images.hasImage("shadowcolorimg" + i)) {
				continue;
			}

			shadowRenderTargets.getOrCreate(i).enableStorage();

			Supplier<GpuTextureView> texture;
			if (flipped == null) {
				texture = () -> shadowRenderTargets.getOrCreate(index).getTextureView(shadowRenderTargets.isFlipped(index), false);
			} else {
				texture = () -> shadowRenderTargets.getOrCreate(index).getTextureView(flipped.contains(index), false);
			}
			InternalTextureFormat format = shadowRenderTargets.getColorTextureFormat(index);

			images.addTextureImage(texture, format, "shadowcolorimg" + i);
		}
	}

	public static void addCustomImages(ImageHolder images, Set<CustomImage> customImages) {
		customImages.forEach(image -> images.addImage(image::image, image.getInternalFormat(), image.getName()));
	}
}
