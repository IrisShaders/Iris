package net.coderbot.iris.uniforms.transforms;

import net.minecraft.util.math.Vec2f;

import java.util.function.Supplier;

public class SmoothedVec2f implements Supplier<Vec2f> {
	private Vec2f currentUnsmoothed;
	private final Supplier<Vec2f> unsmoothed;

	private final SmoothedFloat x;
	private final SmoothedFloat y;

	public SmoothedVec2f(float halfLife, Supplier<Vec2f> unsmoothed) {
		this.unsmoothed = unsmoothed;

		x = new SmoothedFloat(halfLife, () -> currentUnsmoothed.x);
		y = new SmoothedFloat(halfLife, () -> currentUnsmoothed.y);
	}

	@Override
	public Vec2f get() {
		this.currentUnsmoothed = unsmoothed.get();

		return new Vec2f(x.getAsFloat(), y.getAsFloat());
	}
}
