package net.coderbot.iris.uniforms.transforms;

import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.world.phys.Vec2;
import java.util.function.Supplier;

public class SmoothedVec2f implements Supplier<Vec2> {
	private final SmoothedFloat x;
	private final SmoothedFloat y;

	public SmoothedVec2f(float halfLife, Supplier<Vec2> unsmoothed, FrameUpdateNotifier updateNotifier) {
		x = new SmoothedFloat(halfLife, () -> unsmoothed.get().x, updateNotifier);
		y = new SmoothedFloat(halfLife, () -> unsmoothed.get().y, updateNotifier);
	}

	@Override
	public Vec2 get() {
		return new Vec2(x.getAsFloat(), y.getAsFloat());
	}
}
