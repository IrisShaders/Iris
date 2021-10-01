package net.coderbot.iris.gl.sampler;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL42C;

import java.util.OptionalInt;
import java.util.function.IntSupplier;

public class SamplerBinding {
	private final int textureUnit;
	private final OptionalInt imageUnit;
	private final OptionalInt internalFormat;
	private final IntSupplier texture;
	private final Runnable postBind;

	public SamplerBinding(int textureUnit, OptionalInt imageUnit, OptionalInt internalFormat, IntSupplier texture, Runnable postBind) {
		this.textureUnit = textureUnit;
		this.texture = texture;
		this.postBind = postBind;
		this.imageUnit = imageUnit;
		this.internalFormat = internalFormat;
	}

	public void update() {
		int textureID = texture.getAsInt();

		RenderSystem.activeTexture(GL20C.GL_TEXTURE0 + textureUnit);
		RenderSystem.bindTexture(textureID);

		if (imageUnit.isPresent() && internalFormat.isPresent()) {
			GL42C.glBindImageTexture(imageUnit.getAsInt(), textureID, 0, false, 0, GL42C.GL_READ_WRITE, internalFormat.getAsInt());
		}

		this.postBind.run();
	}
}
