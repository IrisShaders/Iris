package net.coderbot.iris.gl.sampler;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.program.GlUniform1iCall;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import org.lwjgl.opengl.GL20C;

import java.util.function.IntSupplier;

public class SamplerBinding {
	private final int textureUnit;
	private final IntSupplier texture;
	private final ValueUpdateNotifier notifier;

	public SamplerBinding(int textureUnit, IntSupplier texture, ValueUpdateNotifier notifier) {
		this.textureUnit = textureUnit;
		this.texture = texture;
		this.notifier = notifier;
	}

	public void update() {
		updateSampler();

		if (notifier != null) {
			notifier.setListener(this::updateSampler);
		}
	}

	private void updateSampler() {
		IrisRenderSystem.bindTextureToUnit(textureUnit, texture.getAsInt());
	}
}
