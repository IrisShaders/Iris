package net.coderbot.iris.gl.sampler;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL20C;

import java.util.function.IntSupplier;

public class SamplerBinding {
	private final int textureUnit;
	private final IntSupplier texture;

	public SamplerBinding(int textureUnit, IntSupplier texture) {
		this.textureUnit = textureUnit;
		this.texture = texture;
	}

	public void update() {
		RenderSystem.activeTexture(GL20C.GL_TEXTURE0 + textureUnit);
		RenderSystem.bindTexture(texture.getAsInt());
	}
}
