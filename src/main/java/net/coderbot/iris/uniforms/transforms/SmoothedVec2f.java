package net.coderbot.iris.uniforms.transforms;

import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.util.math.Vec2f;

import java.util.function.Supplier;

public class SmoothedVec2f implements Supplier<Vec2f> {
	private final SmoothedFloat x;
	private final SmoothedFloat y;

	public SmoothedVec2f(float halfLife, Supplier<Vec2f> unsmoothed, FrameUpdateNotifier updateNotifier) {
		x = new SmoothedFloat(halfLife, () -> unsmoothed.get().x, updateNotifier);
		y = new SmoothedFloat(halfLife, () -> unsmoothed.get().y, updateNotifier);
	}

	@Override
	public Vec2f get() {
		return new Vec2f(x.getAsFloat(), y.getAsFloat());
	}
}
