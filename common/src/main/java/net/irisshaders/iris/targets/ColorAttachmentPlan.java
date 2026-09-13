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
	public record Target(int index, boolean alt) {
		public Target {
			if (index < 0) {
				throw new IllegalArgumentException("Negative render target");
			}
		}
	}

	private final List<Target> targets;
	private final Map<String, Program> programs;

	private ColorAttachmentPlan(Map<String, List<Target>> requests, int maxAttachments) {
		var union = new TreeSet<Target>(Comparator.comparingInt(Target::index).thenComparing(Target::alt));
		requests.values().forEach(union::addAll);

		if (union.size() > maxAttachments) {
			throw new IllegalArgumentException("Merged render scope needs " + union.size() + " color attachments, device supports " +
				maxAttachments + "; split the rendering scope");
		}

		targets = List.copyOf(union);

		var programs = new LinkedHashMap<String, Program>();
		requests.forEach((name, outputs)
			-> programs.put(name, new Program(outputs.stream().mapToInt(targets::indexOf).toArray(),
			outputs.stream().mapToInt(Target::index).toArray(), targets.size())));
		this.programs = Map.copyOf(programs);
	}

	public List<Target> targets() {
		return targets;
	}

	public Set<String> programNames() {
		return programs.keySet();
	}

	public Program program(String name) {
		var program = programs.get(name);

		if (program == null) {
			throw new IllegalArgumentException("Unknown attachment program: " + name);
		}

		return program;
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
			return output >= 0 && output < locations.length ? locations[output] : -1;
		}

		public int attachmentCount() {
			return attachmentCount;
		}

		public List<BlendMode> blends(ProgramDirectives directives, BlendMode defaultBlend) {
			if (!Arrays.equals(drawBuffers, directives.getDrawBuffers())) {
				throw new IllegalArgumentException("Directives do not match attachment program");
			}

			var override = directives.getBlendModeOverride();
			BlendMode base = override.isPresent() ? override.get().blendMode() : defaultBlend;
			var states = new ArrayList<BlendMode>(Collections.nCopies(drawBuffers.length, base));

			for (var buffer : directives.getBufferBlendOverrides()) {
				for (int i = 0; i < drawBuffers.length; i++) {
					if (drawBuffers[i] == buffer.index()) {
						states.set(i, buffer.blendMode());
					}
				}
			}

			return remapBlends(states);
		}

		public List<BlendMode> remapBlends(List<BlendMode> outputBlends) {
			if (outputBlends.size() != locations.length) {
				throw new IllegalArgumentException("Blend states must follow DRAWBUFFERS order");
			}

			var result = new ArrayList<BlendMode>(Collections.nCopies(attachmentCount, null));

			for (int i = 0; i < locations.length; i++) {
				result.set(locations[i], outputBlends.get(i));
			}

			return Collections.unmodifiableList(result);
		}
	}

	public static final class Builder {
		private final Map<String, List<Target>> requests = new LinkedHashMap<>();

		public Builder program(String name, ProgramDirectives directives, Set<Integer> writesToAlt) {
			return program(name, directives.getDrawBuffers(), writesToAlt);
		}

		public Builder program(String name, int[] drawBuffers, Set<Integer> writesToAlt) {
			Objects.requireNonNull(name);

			var outputs = Arrays.stream(drawBuffers).mapToObj(index -> new Target(index, writesToAlt.contains(index))).toList();

			if (new HashSet<>(outputs).size() != outputs.size()) {
				throw new IllegalArgumentException("Duplicate DRAWBUFFERS target for " + name);
			}

			if (requests.putIfAbsent(name, outputs) != null) {
				throw new IllegalArgumentException("Duplicate attachment program: " + name);
			}

			return this;
		}

		public List<ColorAttachmentPlan> buildGroups(int maxAttachments) {
			if (maxAttachments < 1) {
				throw new IllegalArgumentException("Invalid color attachment limit");
			}

			var groups = new ArrayList<ColorAttachmentPlan>();
			var group = new LinkedHashMap<String, List<Target>>();
			var union = new HashSet<Target>();

			for (var entry : requests.entrySet()) {
				var combined = new HashSet<>(union);
				combined.addAll(entry.getValue());

				if (!group.isEmpty() && combined.size() > maxAttachments) {
					groups.add(new ColorAttachmentPlan(group, maxAttachments));
					group.clear();
					union.clear();
				}

				if (entry.getValue().size() > maxAttachments) {
					throw new IllegalArgumentException("Program " + entry.getKey() + " needs " + entry.getValue().size() +
						" color attachments, device supports " + maxAttachments);
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
				throw new IllegalArgumentException("Invalid color attachment limit");
			}

			return new ColorAttachmentPlan(requests, maxAttachments);
		}
	}
}
