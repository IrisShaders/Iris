package net.coderbot.iris.samplers;

import com.google.common.collect.ImmutableSet;
import net.coderbot.iris.gl.image.GlImage;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.rendertarget.RenderTarget;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shadows.ShadowRenderTargets;

import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class IrisImages {
	public static void addRenderTargetImages(ImageHolder images, Supplier<ImmutableSet<Integer>> flipped,
											 RenderTargets renderTargets) {
		for (int i = 0; i < renderTargets.getRenderTargetCount(); i++) {
			final int index = i;

			final String name = "colorimg" + i;

			if (!images.hasImage(name)) continue;

			renderTargets.createIfUnsure(index);

			// Note: image bindings *are* impacted by buffer flips.
			IntSupplier textureID = () -> {
				ImmutableSet<Integer> flippedBuffers = flipped.get();
				RenderTarget target = renderTargets.getOrCreate(index);

				if (flippedBuffers.contains(index)) {
					return target.getAltTexture();
				} else {
					return target.getMainTexture();
				}
			};

			final InternalTextureFormat internalFormat = renderTargets.getOrCreate(i).getInternalFormat();

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
				textureID = () -> flipped.contains(index) ? shadowRenderTargets.getOrCreate(index).getAltTexture() : shadowRenderTargets.getOrCreate(index).getMainTexture();
			}
			InternalTextureFormat format = shadowRenderTargets.getColorTextureFormat(index);

			images.addTextureImage(textureID, format, "shadowcolorimg" + i);
		}
	}

	public static void addCustomImages(ImageHolder images, Set<GlImage> customImages) {
		customImages.forEach(image -> {
			images.addTextureImage(image::getId, image.getInternalFormat(), image.getName());
		});
	}
}
