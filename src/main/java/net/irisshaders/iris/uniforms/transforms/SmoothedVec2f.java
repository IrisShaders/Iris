package net.irisshaders.iris.uniforms.transforms;

import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.function.Supplier;

public class SmoothedVec2f implements Supplier<Vector2f> {
	private final SmoothedFloat x;
	private final SmoothedFloat y;

	public SmoothedVec2f(float halfLifeUp, float halfLifeDown, Supplier<Vector2i> unsmoothed, FrameUpdateNotifier updateNotifier) {
		x = new SmoothedFloat(halfLifeUp, halfLifeDown, () -> unsmoothed.get().x, updateNotifier);
		y = new SmoothedFloat(halfLifeUp, halfLifeDown, () -> unsmoothed.get().y, updateNotifier);
	}

	@Override
	public Vector2f get() {
		return new Vector2f(x.getAsFloat(), y.getAsFloat());
	}
}
