package net.coderbot.iris.layer;

import net.coderbot.iris.gl.uniform.ValueUpdateNotifier;
import net.minecraft.client.renderer.RenderStateShard;

public class EntityColorRenderStateShard extends RenderStateShard {
	public static boolean currentHurt;
	public static float currentWhiteFlash;
	private static Runnable listener;

	private final boolean hurt;
	private final float whiteFlash;

	public EntityColorRenderStateShard(boolean hurt, float whiteFlash) {
		super("iris:entity_color", () -> {
			currentHurt = hurt;
			currentWhiteFlash = whiteFlash;

			if (listener != null) {
				listener.run();
			}
		}, () -> {
			currentHurt = false;
			currentWhiteFlash = 0.0f;

			if (listener != null) {
				listener.run();
			}
		});

		this.hurt = hurt;
		this.whiteFlash = whiteFlash;
	}

	public static ValueUpdateNotifier getUpdateNotifier() {
		return candidate -> listener = candidate;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}

		if (object.getClass() != this.getClass()) {
			return false;
		}

		EntityColorRenderStateShard other = (EntityColorRenderStateShard) object;

		if (this.hurt != other.hurt) {
			return false;
		}

		return this.hurt || this.whiteFlash == other.whiteFlash;
	}
}
