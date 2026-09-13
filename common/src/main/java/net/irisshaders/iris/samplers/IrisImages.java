package net.irisshaders.iris.samplers;

import com.google.common.collect.ImmutableSet;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.CustomImage;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.vulkan.CompositeSynchronization;

import java.util.Set;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class IrisImages {
	public static void addRenderTargetImages(ImageHolder images, Supplier<ImmutableSet<Integer>> flipped,
		RenderTargets renderTargets) {
		for (int i = 0; i < renderTargets.getRenderTargetCount(); i++) {
			final int index = i;

			final String name = "colorimg" + i;

			if (!images.hasImage(name)) {
				continue;
			}

			renderTargets.createIfUnsure(index);
			renderTargets.getOrCreate(index).enableStorage();

			// Note: image bindings *are* impacted by buffer flips.
			LongSupplier textureID = () -> {
				ImmutableSet<Integer> flippedBuffers = flipped.get();
				RenderTarget target = renderTargets.getOrCreate(index);

				if (flippedBuffers.contains(index)) {
					return ((VulkanGpuTextureView) target.getTextureView(true, false)).vkImageView();
				} else {
					return ((VulkanGpuTextureView) target.getTextureView(false, false)).vkImageView();
				}
			};

			final InternalTextureFormat internalFormat = renderTargets.getOrCreate(i).getInternalFormat();
			images.addTextureImage(textureID, () -> CompositeSynchronization.image(
					renderTargets.getOrCreate(index).getTextureView(flipped.get().contains(index), false).texture()).handle(),
				internalFormat,
				name);
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

			LongSupplier textureID;

			if (flipped == null) {
				textureID = () -> ((VulkanGpuTextureView) shadowRenderTargets.getOrCreate(index)
					.getTextureView(shadowRenderTargets.isFlipped(index), false)).vkImageView();
			} else {
				textureID = () -> ((VulkanGpuTextureView) shadowRenderTargets.getOrCreate(index)
					.getTextureView(flipped.contains(index), false)).vkImageView();
			}

			InternalTextureFormat format = shadowRenderTargets.getColorTextureFormat(index);
			images.addTextureImage(textureID, () -> CompositeSynchronization.image(shadowRenderTargets.getOrCreate(index)
					.getTextureView(flipped == null ? shadowRenderTargets.isFlipped(index) : flipped.contains(index), false).texture())
				.handle(), format, "shadowcolorimg" + i);
		}
	}

	public static void addCustomImages(ImageHolder images, Set<CustomImage> customImages) {
		customImages.forEach(
			image -> images.addTextureImage(() -> image.image().view(), () -> image.image().image(), image.getInternalFormat(),
				image.getName()));
	}
}
