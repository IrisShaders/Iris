package net.irisshaders.iris.pipeline.programs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PackShaderInjector {
	public static final Identifier LOCATION = Identifier.fromNamespaceAndPath("iris", "injector.json");

	private static final Pattern VERSION = Pattern.compile("(#version[^\\n]*\\n)");

	private static final Map<String, ProgramId> PROGRAM_NAMES = buildProgramNames();

	private static List<Injector> injectors = List.of();

	private PackShaderInjector() {
	}

	private static Map<String, ProgramId> buildProgramNames() {
		Map<String, ProgramId> m = new LinkedHashMap<>();
		m.put("entities", ProgramId.Entities);
		m.put("entities_translucent", ProgramId.EntitiesTrans);
		m.put("entities_glowing", ProgramId.EntitiesGlowing);
		m.put("shadow_entities", ProgramId.ShadowEntities);
		m.put("hand", ProgramId.Hand);
		m.put("hand_water", ProgramId.HandWater);
		m.put("block", ProgramId.Block);
		m.put("block_translucent", ProgramId.BlockTrans);
		m.put("beacon", ProgramId.BeaconBeam);
		m.put("particles", ProgramId.Particles);
		return m;
	}

	public static void reload(ResourceManager resourceManager) {
		try {
			injectors = load(resourceManager);
			if (!injectors.isEmpty()) {
				Iris.logger.info("Loaded " + injectors.size() + " shader injector(s) from resource packs");
			}
		} catch (Exception e) {
			// A broken injector shouldn't take the whole game down
			Iris.logger.error("Failed to load shader injectors", e);
			injectors = List.of();
		}
	}


	public static String[] apply(ProgramId programId, String name, String vertex, String fragment) {
		String[] result = new String[]{vertex, fragment};
		for (Injector injector : injectors) {
			if (!injector.matches(programId)) {
				continue;
			}
			try {
				injector.apply(result);
			} catch (Exception e) {
				// Never let a bad injector break shader compilation
				Iris.logger.error("Failed to apply shader injector to " + name, e);
			}
		}
		return result;
	}

	private static List<Injector> load(ResourceManager resourceManager) {
		List<Injector> result = new ArrayList<>();

		for (Resource resource : resourceManager.getResourceStack(LOCATION)) {
			try (InputStream stream = resource.open()) {
				String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
				JsonObject root = JsonParser.parseString(json).getAsJsonObject();
				result.add(parse(resourceManager, LOCATION, root));
			} catch (Exception e) {
				Iris.logger.error("Failed to parse " + LOCATION, e);
			}
		}
		return result;
	}

	private static Injector parse(ResourceManager resourceManager, Identifier location, JsonObject root) {
		List<ProgramId> programs = new ArrayList<>();
		if (root.has("programs")) {
			for (var element : root.getAsJsonArray("programs")) {
				String key = element.getAsString();
				ProgramId id = PROGRAM_NAMES.get(key);
				if (id != null) {
					programs.add(id);
				} else {
					// Typo or an unsupported program
					Iris.logger.warn("Unknown program '" + key + "' in " + location);
				}
			}
		}

		StageInjector vertex = root.has("vertex")
			? parseStage(resourceManager, location, root.getAsJsonObject("vertex")) : null;
		StageInjector fragment = root.has("fragment")
			? parseStage(resourceManager, location, root.getAsJsonObject("fragment")) : null;

		return new Injector(programs, vertex, fragment);
	}

	private static StageInjector parseStage(ResourceManager resourceManager, Identifier location, JsonObject obj) {
		String version = obj.has("version") ? obj.get("version").getAsString() : null;

		List<String> extensions = new ArrayList<>();
		if (obj.has("extensions")) {
			for (var e : obj.getAsJsonArray("extensions")) {
				extensions.add(e.getAsString());
			}
		}

		Map<String, String> alias = new LinkedHashMap<>();
		if (obj.has("alias")) {
			for (var e : obj.getAsJsonObject("alias").entrySet()) {
				alias.put(e.getKey(), e.getValue().getAsString());
			}
		}

		List<String> samplers = new ArrayList<>();
		if (obj.has("samplers")) {
			for (var e : obj.getAsJsonArray("samplers")) {
				samplers.add(e.getAsString());
			}
		}

		String declarations = obj.has("declarations") ? obj.get("declarations").getAsString() : null;


		String include = null;
		if (obj.has("include")) {
			include = loadInclude(resourceManager, location, obj.get("include").getAsString());
		} else if (obj.has("code")) {
			include = obj.get("code").getAsString();
		} else if (obj.has("codeLines")) {
			JsonArray lines = obj.getAsJsonArray("codeLines");
			StringBuilder sb = new StringBuilder();
			for (var line : lines) {
				sb.append(line.getAsString()).append('\n');
			}
			include = sb.toString();
		}

		String callInMain = obj.has("callInMain") ? obj.get("callInMain").getAsString() : null;

		return new StageInjector(version, extensions, alias, samplers, declarations, include, callInMain);
	}

	private static String loadInclude(ResourceManager resourceManager, Identifier location, String relative) {

		String parent = location.getPath();
		int slash = parent.lastIndexOf('/');
		String dir = slash >= 0 ? parent.substring(0, slash + 1) : "";
		Identifier includeLoc = Identifier.fromNamespaceAndPath(location.getNamespace(), dir + relative);
		var resource = resourceManager.getResource(includeLoc);
		if (resource.isEmpty()) {
			Iris.logger.warn("Shader injector include not found: " + includeLoc);
			return "";
		}
		try (InputStream stream = resource.get().open()) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			Iris.logger.error("Failed to read shader injector include " + includeLoc, e);
			return "";
		}
	}

	private record Injector(List<ProgramId> programs, StageInjector vertex, StageInjector fragment) {
		boolean matches(ProgramId programId) {
			return programs.contains(programId);
		}

		void apply(String[] vertexAndFragment) {
			boolean vertexApplied = false;
			if (vertex != null && vertexAndFragment[0] != null) {
				String result = vertex.apply(vertexAndFragment[0]);
				if (result != null) {
					vertexAndFragment[0] = result;
					vertexApplied = true;
				}
			}

			if (fragment != null && vertexAndFragment[1] != null && (vertex == null || vertexApplied)) {
				String result = fragment.apply(vertexAndFragment[1]);
				if (result != null) {
					vertexAndFragment[1] = result;
				}
			}
		}
	}

	private record StageInjector(String version, List<String> extensions, Map<String, String> alias,
								 List<String> samplers, String declarations, String include, String callInMain) {

		String apply(String source) {
			Matcher versionMatcher = VERSION.matcher(source);
			if (!versionMatcher.find()) {
				return null;
			}
			int mainIndex = source.indexOf("void main");
			if (mainIndex < 0 || source.indexOf('{', mainIndex) < 0) {
				return null;
			}

			for (Map.Entry<String, String> e : alias.entrySet()) {
				Pattern p = Pattern.compile("in\\s+(\\w+)\\s+" + Pattern.quote(e.getKey()) + "\\s*;");
				Matcher m = p.matcher(source);
				if (!m.find()) {
					return null;
				}
				source = m.replaceFirst("in $1 " + Matcher.quoteReplacement(e.getValue())
					+ ";\n$1 " + Matcher.quoteReplacement(e.getKey()) + ";");
			}

			StringBuilder header = new StringBuilder();
			if (version != null) {
				header.append("#version ").append(version).append('\n');
			} else {
				header.append(versionMatcher.group(1));
			}
			for (String ext : extensions) {
				header.append("#extension ").append(ext).append(" : enable\n");
			}
			if (declarations != null) {
				header.append(declarations).append('\n');
			}
			source = VERSION.matcher(source).replaceFirst(Matcher.quoteReplacement(header.toString()));

			StringBuilder block = new StringBuilder();
			for (String sampler : samplers) {
				if (!source.contains(sampler)) {
					block.append("uniform sampler2D ").append(sampler).append(";\n");
				}
			}
			if (include != null) {
				block.append(include);
			}
			if (block.length() > 0) {
				int insertBefore = source.indexOf("void main");
				source = source.substring(0, insertBefore) + block + "\n" + source.substring(insertBefore);
			}

			if (callInMain != null) {
				int idx = source.indexOf("void main");
				int brace = source.indexOf('{', idx);
				source = source.substring(0, brace + 1) + "\n    " + callInMain + "\n" + source.substring(brace + 1);
			}

			return source;
		}
	}
}
