package net.coderbot.iris.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL30C;

import java.nio.ByteBuffer;

public class IrisRenderSystem {
	public static void generateMipmaps(int mipmapTarget) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glGenerateMipmap(mipmapTarget);
	}

	public static void bindAttributeLocation(int program, int index, ByteBuffer name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glBindAttribLocation(program, index, name);
	}

	public static void bindAttributeLocation(int program, int index, CharSequence name) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL30C.glBindAttribLocation(program, index, name);
	}
}
