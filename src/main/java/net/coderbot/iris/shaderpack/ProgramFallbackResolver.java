package net.coderbot.iris.shaderpack;

import net.coderbot.iris.shaderpack.loading.ProgramId;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProgramFallbackResolver {
	private final ProgramSet programs;
	private final Map<ProgramId, ProgramSource> cache;

	public ProgramFallbackResolver(ProgramSet programs) {
		this.programs = programs;
		this.cache = new HashMap<>();
	}

	public Optional<ProgramSource> resolve(ProgramId id) {
		return Optional.ofNullable(resolveNullable(id));
	}

	@Nullable
	public ProgramSource resolveNullable(ProgramId id) {
		if (cache.containsKey(id)) {
			return cache.get(id);
		}

		ProgramSource source = programs.get(id).orElse(null);

		if (source == null) {
			ProgramId fallback = id.getFallback().orElse(null);

			if (fallback != null) {
				source = resolveNullable(fallback);
			}
		}

		cache.put(id, source);
		return source;
	}
}
