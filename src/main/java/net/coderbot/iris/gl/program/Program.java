package net.coderbot.iris.gl.program;

import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.uniform.Uniform;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;

public final class Program extends GlResource {
	private final GlProgram glProgram;
	private final ImmutableList<Uniform> perTick;
	private final ImmutableList<Uniform> perFrame;

	private ImmutableList<Uniform> once;
	long lastTick = -1;

	Program(GlProgram glProgram, ImmutableList<Uniform> once, ImmutableList<Uniform> perTick, ImmutableList<Uniform> perFrame) {
		super(glProgram.getProgramRef());

		this.glProgram = glProgram;
		this.once = once;
		this.perTick = perTick;
		this.perFrame = perFrame;
	}

	private void update(ImmutableList<Uniform> uniforms) {
		for (Uniform uniform : uniforms) {
			uniform.update();
		}
	}

	private static long getCurrentTick() {
		return Objects.requireNonNull(MinecraftClient.getInstance().world).getTime();
	}

	public void use() {
		GlProgramManager.useProgram(getGlId());

		if (once != null) {
			update(once);
			update(perTick);
			update(perFrame);
			lastTick = getCurrentTick();

			once = null;
			return;
		}

		long currentTick = getCurrentTick();

		if (lastTick != currentTick) {
			lastTick = currentTick;

			update(perTick);
		}

		update(perFrame);
	}

	public void destroyInternal() {
		GlStateManager.deleteProgram(getGlId());
	}

	/**
	 * @return the OpenGL ID of this program.
	 * @deprecated this should be encapsulated eventually
	 */
	@Deprecated
	public int getProgramId() {
		return getGlId();
	}
}
