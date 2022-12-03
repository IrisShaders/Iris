package net.coderbot.iris.samplers;

import com.google.common.collect.ImmutableSet;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.rendertarget.RenderTarget;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shadows.ShadowRenderTargets;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class IrisImages {
	public static void addRenderTargetImages(ImageHolder images, Supplier<ImmutableSet<Integer>> flipped,
											 RenderTargets renderTargets) {
		for (int i = 0; i < renderTargets.getRenderTargetCount(); i++) {
			final int index = i;

			// Note: image bindings *are* impacted by buffer flips.
			IntSupplier textureID = () -> {
				ImmutableSet<Integer> flippedBuffers = flipped.get();
				RenderTarget target = renderTargets.get(index);

				if (flippedBuffers.contains(index)) {
					return target.getAltTexture();
				} else {
					return target.getMainTexture();
				}
			};

			final InternalTextureFormat internalFormat = renderTargets.get(i).getInternalFormat();
			final String name = "colorimg" + i;

			images.addTextureImage(textureID, internalFormat, name);
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

			IntSupplier textureID;
			if (flipped == null) {
				textureID = () -> shadowRenderTargets.getColorTextureId(index);
			} else {
				textureID = () -> flipped.contains(index) ? shadowRenderTargets.get(index).getAltTexture() : shadowRenderTargets.get(index).getMainTexture();
			}
			InternalTextureFormat format = shadowRenderTargets.getColorTextureFormat(index);

			images.addTextureImage(textureID, format, "shadowcolorimg" + i);
		}
	}
}
