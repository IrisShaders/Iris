package net.irisshaders.iris.targets;

import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.shadows.ShadowRenderTargets;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class WorldRenderTargets {
	public enum Phase {
		OPAQUE,
		TRANSLUCENT,
		SHADOW
	}

	private final RenderTargets targets;
	private final Supplier<ShadowRenderTargets> shadows;
	private final Map<Phase, ColorAttachmentPlan.Builder> builders = new EnumMap<>(Phase.class);
	private final Map<Phase, List<ColorAttachmentPlan>> plans = new EnumMap<>(Phase.class);
	private final Map<ColorAttachmentPlan, GlFramebuffer> framebuffers = new IdentityHashMap<>();
	private final Set<Integer> opaque, translucent;

	public WorldRenderTargets(RenderTargets targets, Supplier<ShadowRenderTargets> shadows, Set<Integer> opaque,
		Set<Integer> translucent) {
		this.targets = targets;
		this.shadows = shadows;
		this.opaque = Set.copyOf(opaque);
		this.translucent = Set.copyOf(translucent);

		for (Phase phase : Phase.values()) {
			builders.put(phase, new ColorAttachmentPlan.Builder());
		}
	}

	public Set<Integer> flipped(Phase phase) {
		return switch (phase) {
			case OPAQUE -> opaque;
			case TRANSLUCENT -> translucent;
			case SHADOW -> Set.of();
		};
	}

	public void register(String name, int[] drawBuffers, boolean shadow) {
		for (Phase phase : shadow ? List.of(Phase.SHADOW) : List.of(Phase.OPAQUE, Phase.TRANSLUCENT)) {
			if (plans.containsKey(phase)) {
				throw new IllegalStateException("World attachments were sealed before shader discovery finished");
			}

			builders.get(phase).program(name, drawBuffers, flipped(phase));
		}
	}

	private List<ColorAttachmentPlan> plans(Phase phase) {
		return plans.computeIfAbsent(phase, p -> builders.get(p).buildGroups(targets.maxDrawBuffers()));
	}

	public ColorAttachmentPlan plan(Phase phase) {
		return plans(phase).getFirst();
	}

	public ColorAttachmentPlan plan(Phase phase, String program) {
		return plans(phase)
			.stream()
			.filter(plan -> plan.programNames().contains(program))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("No attachment group for " + program));
	}

	public GlFramebuffer framebuffer(Phase phase) {
		return framebuffer(phase, plan(phase));
	}

	public GlFramebuffer framebuffer(Phase phase, String program) {
		return framebuffer(phase, plan(phase, program));
	}

	private GlFramebuffer framebuffer(Phase phase, ColorAttachmentPlan plan) {
		return framebuffers.computeIfAbsent(
			plan, p -> phase == Phase.SHADOW ? shadows.get().createMergedFramebuffer(p) : targets.createMergedFramebuffer(p, true));
	}
}
