package net.irisshaders.iris.targets;

import net.irisshaders.iris.shadows.ShadowRenderTargets;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class WorldRenderTargets {
	private final RenderTargets targets;
	private final Supplier<ShadowRenderTargets> shadows;
	private final Map<Phase, ColorAttachmentPlan.Builder> builders = new EnumMap<>(Phase.class);
	private final Map<Phase, List<ColorAttachmentPlan>> plans = new EnumMap<>(Phase.class);
	private final Map<ColorAttachmentPlan, RenderFramebuffer> framebuffers = new IdentityHashMap<>();
	private final Set<Integer> opaque;
	private final Set<Integer> translucent;

	public WorldRenderTargets(RenderTargets targets, Supplier<ShadowRenderTargets> shadows, Set<Integer> opaque, Set<Integer> translucent) {
		this.targets = targets;
		this.shadows = shadows;
		this.opaque = Set.copyOf(opaque);
		this.translucent = Set.copyOf(translucent);

		for (Phase phase : Phase.values()) {
			this.builders.put(phase, new ColorAttachmentPlan.Builder());
		}
	}

	public Set<Integer> getFlippedBuffers(Phase phase) {
		return switch (phase) {
			case OPAQUE -> this.opaque;
			case TRANSLUCENT -> this.translucent;
			case SHADOW -> Set.of();
		};
	}

	public void register(String name, int[] drawBuffers, List<Phase> phases) {
		for (Phase phase : phases) {
			if (this.plans.containsKey(phase)) {
				throw new IllegalStateException("world attachments were sealed before shader discovery finished");
			}

			this.builders.get(phase).program(name, drawBuffers, this.getFlippedBuffers(phase));
		}
	}

	private List<ColorAttachmentPlan> plans(Phase phase) {
		return this.plans.computeIfAbsent(phase, p -> this.builders.get(p).buildGroups(this.targets.maxDrawBuffers()));
	}

	public ColorAttachmentPlan getAttachmentPlan(Phase phase) {
		return this.plans(phase).getFirst();
	}

	public ColorAttachmentPlan getAttachmentPlan(Phase phase, String program) {
		return this.plans(phase).stream().filter(plan -> plan.programNames().contains(program)).findFirst()
			.orElseThrow(() -> new IllegalArgumentException("no attachment group for " + program));
	}

	public RenderFramebuffer getFramebuffer(Phase phase) {
		return this.getFramebuffer(phase, this.getAttachmentPlan(phase));
	}

	public RenderFramebuffer getFramebuffer(Phase phase, String program) {
		return this.getFramebuffer(phase, this.getAttachmentPlan(phase, program));
	}

	private RenderFramebuffer getFramebuffer(Phase phase, ColorAttachmentPlan plan) {
		return this.framebuffers.computeIfAbsent(plan,
			p -> phase == Phase.SHADOW ? this.shadows.get().createMergedFramebuffer(p) : this.targets.createMergedFramebuffer(p, true));
	}

	public enum Phase {
		OPAQUE,
		TRANSLUCENT,
		SHADOW
	}
}
