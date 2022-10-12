package net.coderbot.iris.texture.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class TextureExporter {
	public static void exportTextures(String directory, String filename, int textureId, int mipLevel, int width, int height) {
		String extension = FilenameUtils.getExtension(filename);
		String baseName = filename.substring(0, filename.length() - extension.length() - 1);
		for (int level = 0; level <= mipLevel; ++level) {
			exportTexture(directory, baseName + "_" + level + "." + extension, textureId, level, width >> level, height >> level);
		}
	}

	public static void exportTexture(String directory, String filename, int textureId, int level, int width, int height) {
		NativeImage nativeImage = new NativeImage(width, height, false);
		RenderSystem.bindTexture(textureId);
		nativeImage.downloadTexture(level, false);

		File dir = new File(Minecraft.getInstance().gameDirectory, directory);
		dir.mkdirs();
		File file = new File(dir, filename);

		Util.ioPool().execute(() -> {
			try {
				nativeImage.writeToFile(file);
			} catch (Exception var7) {
				//
			} finally {
				nativeImage.close();
			}
		});
	}
}
