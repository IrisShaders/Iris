package net.coderbot.iris.shaderpack.option;

import net.coderbot.iris.Iris;
import net.coderbot.iris.IrisLogging;
import net.coderbot.iris.shaderpack.option.values.OptionValues;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ProfileSet {
	private final LinkedHashMap<String, Profile> orderedProfiles; // The order that profiles should cycle through
	private final List<Profile> sortedProfiles; // The order that profiles should be scanned through

	public ProfileSet(LinkedHashMap<String, Profile> orderedProfiles) {
		List<Profile> sorted = new ArrayList<>(orderedProfiles.values());

		Comparator<Profile> lowToHigh = Comparator.comparing(p -> p.precedence);
		Comparator<Profile> highToLow = lowToHigh.reversed();

		// Compare profiles with many constraints (high precedence) first before comparing ones with
		// few constraints, needed for accurate matching when one profile contains an additional constraint
		// to another profile but is otherwise the same;
		sorted.sort(highToLow);

		if (IrisLogging.ENABLE_SPAM) {
			sorted.forEach(p -> System.out.println(p.name + ":" + p.precedence));
		}

		this.sortedProfiles = sorted;
		this.orderedProfiles = orderedProfiles;
	}

	public void forEach(BiConsumer<String, Profile> action) {
		orderedProfiles.forEach(action);
	}

	public ProfileResult scan(OptionSet options, OptionValues values) {
		if (sortedProfiles.size() <= 0) {
			return new ProfileResult(null, null, null);
		}

		for (int i = 0; i < sortedProfiles.size(); i++) {
			Profile current = sortedProfiles.get(i);

			if (current.matches(options, values)) {
				Profile next = sortedProfiles.get(Math.floorMod(i + 1, sortedProfiles.size()));
				Profile prev = sortedProfiles.get(Math.floorMod(i - 1, sortedProfiles.size()));

				return new ProfileResult(current, next, prev);
			}
		}

		// Default return if no profiles matched
		Profile next = sortedProfiles.get(0);
		Profile prev = sortedProfiles.get(sortedProfiles.size() - 1);

		return new ProfileResult(null, next, prev);
	}

	public static ProfileSet fromTree(Map<String, List<String>> tree, OptionSet optionSet) {
		LinkedHashMap<String, Profile> profiles = new LinkedHashMap<>();

		for (String name : tree.keySet()) {
			profiles.put(name, parse(name, new ArrayList<>(), tree, optionSet));
		}

		return new ProfileSet(profiles);
	}

	private static Profile parse(String name, List<String> parents, Map<String, List<String>> tree, OptionSet optionSet) throws IllegalArgumentException {
		Profile.Builder builder = new Profile.Builder(name);
		List<String> options = tree.get(name);

		if (options == null) {
			throw new IllegalArgumentException("Profile \"" + name + "\" does not exist!");
		}

		for (String option : options) {
			if (option.startsWith("!program.")) {
				builder.disableProgram(option.substring("!program.".length()));
			} else if (option.startsWith("profile.")) {
				String dependency = option.substring("profile.".length());

				if (parents.contains(dependency)) {
					throw new IllegalArgumentException("Error parsing profile \"" + name
							+ "\", recursively included by: " + String.join(", ", parents));
				}

				parents.add(dependency);
				builder.addAll(parse(dependency, parents, tree, optionSet));
			} else if (option.startsWith("!")) {
				builder.option(option.substring(1), "false");
			} else if (option.contains("=")) {
				int splitPoint = option.indexOf("=");
				builder.option(option.substring(0, splitPoint), option.substring(splitPoint + 1));
			} else if (option.contains(":")) {
				int splitPoint = option.indexOf(":");
				builder.option(option.substring(0, splitPoint), option.substring(splitPoint + 1));
			} else if (optionSet.isBooleanOption(option)) {
				builder.option(option, "true");
			} else {
				Iris.logger.warn("Invalid pack option: " + option);
			}
		}

		return builder.build();
	}

	public int size() {
		return sortedProfiles.size();
	}

	public static class ProfileResult {
		public final Optional<Profile> current;
		public final Profile next;
		public final Profile previous;

		private ProfileResult(@Nullable Profile current, Profile next, Profile previous) {
			this.current = Optional.ofNullable(current);
			this.next = next;
			this.previous = previous;
		}
	}
}
