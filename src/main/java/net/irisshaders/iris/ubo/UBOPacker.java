package net.irisshaders.iris.ubo;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import kroppeb.stareval.function.Type;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.sampler.SamplerLimits;
import net.irisshaders.iris.gl.uniform.Uniform;
import net.irisshaders.iris.gl.uniform.UniformType;
import net.irisshaders.iris.uniforms.custom.cached.CachedUniform;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class UBOPacker {
	public final Object2IntMap<CachedUniform> uniforms = new Object2IntLinkedOpenHashMap<>();
	private final UniformBufferObject ubo;

	public static UBOPacker INSTANCE; // TEMPORARY TODO

	public UBOPacker(EnumMap<UniformType, List<CachedUniform>> uniformMap) {
		// Let's deal with the vec3's first to make it easier.
		INSTANCE = this;
		int currentIndex = 0;

		if (uniformMap.containsKey(UniformType.VEC3) || uniformMap.containsKey(UniformType.VEC3I)) {
			// Combine vec3 and vec3i, they have the same stuffs.

			List<CachedUniform> vec3List = new ArrayList<>();
			vec3List.addAll(uniformMap.getOrDefault(UniformType.VEC3, Collections.emptyList()));
			vec3List.addAll(uniformMap.getOrDefault(UniformType.VEC3I, Collections.emptyList()));

			Deque<CachedUniform> fillerList = new ArrayDeque<>();
			fillerList.addAll(uniformMap.getOrDefault(UniformType.INT, Collections.emptyList()));
			fillerList.addAll(uniformMap.getOrDefault(UniformType.FLOAT, Collections.emptyList()));

			for (CachedUniform uniform : vec3List) {
				//Iris.logger.warn(uniform.getName() + " goes at " + currentIndex);
				uniforms.put(uniform, currentIndex);
				currentIndex += 12;

				if (!fillerList.isEmpty()) {
					CachedUniform filler = fillerList.pollFirst();

					uniformMap.get(Type.convert(filler.getType())).remove(filler);
					uniforms.put(filler, currentIndex);

					//Iris.logger.warn("Putting " + filler.getName() + " as filler at " + currentIndex);

					currentIndex += 4;
				} else {
					//Iris.logger.warn("Forced to make a empty spot.");
					currentIndex += 4;
				}
			}
		}

		uniformMap.remove(UniformType.VEC3);
		uniformMap.remove(UniformType.VEC3I);

		for (UniformType uniformType : uniformMap.keySet()) {
			for (CachedUniform uniform : uniformMap.get(uniformType)) {
				if (currentIndex % uniformType.getAlignment() != 0) {
					//uniform.writeTo();
					//Iris.logger.warn("Need to pad to " + uniformType.getAlignment() + "from " + currentIndex + ", going to " + (currentIndex + (uniformType.getAlignment() - (currentIndex % uniformType.getAlignment()))));
					currentIndex += (uniformType.getAlignment() - (currentIndex % uniformType.getAlignment()));
				}

				//Iris.logger.warn(uniform.getName() + " goes in " + currentIndex);
				uniforms.put(uniform, currentIndex);

				currentIndex += uniformType.getSize();
			}
		}

		if (currentIndex % SamplerLimits.get().getUboOffsetAlignment() != 0) {
			currentIndex += (SamplerLimits.get().getUboOffsetAlignment() - (currentIndex % SamplerLimits.get().getUboOffsetAlignment()));
		}

		this.ubo = new UniformBufferObject(currentIndex);

		//Iris.logger.warn(constructUsage());
	}

	public void bind() {
		ubo.bindRange(frameId);
	}

	private int frameId = 0;

	public void update() {
		frameId++;

		if (frameId == UniformBufferObject.frameCount) {
			frameId = 0;
		}
		long range = ubo.getRange(frameId);

		uniforms.forEach((uniform, address) -> uniform.writeTo(range + address));

		ubo.flushRange(frameId);

		bind();
	}

	public String constructUsage() {
		StringBuilder builder = new StringBuilder();

		builder.append("layout(binding = 5, std140) uniform DefaultUBO {\n");

		uniforms.keySet().forEach(uniform -> {
			builder.append(uniform.getType().getGLSLName()).append(" ").append(uniform.getName()).append(";\n");
		});

		builder.append("} uboValues;");

		return builder.toString();
	}
}
