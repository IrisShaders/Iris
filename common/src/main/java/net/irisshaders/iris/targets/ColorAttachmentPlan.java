package net.irisshaders.iris.targets;

import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.shaderpack.properties.ProgramDirectives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public final class ColorAttachmentPlan {
	private final List<Target> targets;
	private final Map<String, Program> programs;

	private ColorAttachmentPlan(Map<String, List<Target>> requests, int maxAttachments) {
		var union = new TreeSet<Target>(Comparator.comparingInt(Target::index).thenComparing(Target::alt));
		requests.values().forEach(union::addAll);

		if (union.size() > maxAttachments) {
			throw new IllegalArgumentException(
				"merged render scope needs " + union.size() + " color attachments, device supports " + maxAttachments + "; split the rendering scope");
		}

		this.targets = List.copyOf(union);

		var programs = new LinkedHashMap<String, Program>();
		requests.forEach((name, outputs) -> programs.put(name,
			new Program(outputs.stream().mapToInt(this.targets::indexOf).toArray(), outputs.stream().mapToInt(Target::index).toArray(),
				this.targets.size())));
		this.programs = Map.copyOf(programs);
	}

	public List<Target> targets() {
		return this.targets;
	}

	public Set<String> programNames() {
		return this.programs.keySet();
	}

	public Program program(String name) {
		var program = this.programs.get(name);

		if (program == null) {
			throw new IllegalArgumentException("unknown attachment program: " + name);
		}

		return program;
	}

	public record Target(int index, boolean alt) {
		public Target {
			if (index < 0) {
				throw new IllegalArgumentException("negative render target");
			}
		}
	}

	public static final class Program {
		private final int[] locations;
		private final int[] drawBuffers;
		private final int attachmentCount;

		private Program(int[] locations, int[] drawBuffers, int attachmentCount) {
			this.locations = locations;
			this.drawBuffers = drawBuffers;
			this.attachmentCount = attachmentCount;
		}

		public int attachment(int output) {
			return output >= 0 && output < this.locations.length ? this.locations[output] : -1;
		}

		public List<Integer> outputLocations() {
			return Arrays.stream(this.locations).boxed().toList();
		}

		public int attachmentCount() {
			return this.attachmentCount;
		}

		public List<BlendMode> blends(ProgramDirectives directives, BlendMode defaultBlend) {
			if (!directives.hasUnknownDrawBuffers() && !Arrays.equals(this.drawBuffers, directives.getDrawBuffers())) {
				throw new IllegalArgumentException("directives do not match attachment program");
			}

			var override = directives.getBlendModeOverride();
			BlendMode base = override.isPresent() ? override.get().blendMode() : defaultBlend;
			var states = new ArrayList<BlendMode>(Collections.nCopies(this.drawBuffers.length, base));

			for (var buffer : directives.getBufferBlendOverrides()) {
				for (int i = 0; i < this.drawBuffers.length; i++) {
					if (this.drawBuffers[i] == buffer.index()) {
						states.set(i, buffer.blendMode());
					}
				}
			}

			return this.remapBlends(states);
		}

		public List<BlendMode> remapBlends(List<BlendMode> outputBlends) {
			if (outputBlends.size() != this.locations.length) {
				throw new IllegalArgumentException("blend states must follow DRAWBUFFERS order");
			}

			var result = new ArrayList<BlendMode>(Collections.nCopies(this.attachmentCount, null));

			for (int i = 0; i < this.locations.length; i++) {
				result.set(this.locations[i], outputBlends.get(i));
			}

			return Collections.unmodifiableList(result);
		}
	}

	public static final class Builder {
		private final Map<String, List<Target>> requests = new LinkedHashMap<>();

		public Builder program(String name, ProgramDirectives directives, Set<Integer> writesToAlt) {
			return this.program(name, directives.getDrawBuffers(), writesToAlt);
		}

		public Builder program(String name, int[] drawBuffers, Set<Integer> writesToAlt) {
			Objects.requireNonNull(name);

			var outputs = Arrays.stream(drawBuffers).mapToObj(index -> new Target(index, writesToAlt.contains(index))).toList();

			if (new HashSet<>(outputs).size() != outputs.size()) {
				throw new IllegalArgumentException("duplicate DRAWBUFFERS target for " + name);
			}

			if (this.requests.putIfAbsent(name, outputs) != null) {
				throw new IllegalArgumentException("duplicate attachment program: " + name);
			}

			return this;
		}

		public List<ColorAttachmentPlan> buildGroups(int maxAttachments) {
			if (maxAttachments < 1) {
				throw new IllegalArgumentException("invalid color attachment limit");
			}

			var groups = new ArrayList<ColorAttachmentPlan>();
			var group = new LinkedHashMap<String, List<Target>>();
			var union = new HashSet<Target>();

			for (var entry : this.requests.entrySet()) {
				var combined = new HashSet<>(union);
				combined.addAll(entry.getValue());

				if (!group.isEmpty() && combined.size() > maxAttachments) {
					groups.add(new ColorAttachmentPlan(group, maxAttachments));
					group.clear();
					union.clear();
				}

				if (entry.getValue().size() > maxAttachments) {
					throw new IllegalArgumentException("program " + entry.getKey() + " needs " + entry.getValue()
						.size() + " color attachments, device supports " + maxAttachments);
				}

				group.put(entry.getKey(), entry.getValue());
				union.addAll(entry.getValue());
			}

			if (!group.isEmpty()) {
				groups.add(new ColorAttachmentPlan(group, maxAttachments));
			}

			return List.copyOf(groups);
		}

		public ColorAttachmentPlan build(int maxAttachments) {
			if (maxAttachments < 1) {
				throw new IllegalArgumentException("invalid color attachment limit");
			}

			return new ColorAttachmentPlan(this.requests, maxAttachments);
		}
	}
}
