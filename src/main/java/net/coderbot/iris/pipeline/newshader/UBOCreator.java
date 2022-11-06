package net.coderbot.iris.pipeline.newshader;

import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.Pair;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.gl.uniform.DynamicLocationalUniformHolder;
import net.coderbot.iris.gl.uniform.FloatSupplier;
import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformType;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector2i;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vendored.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class UBOCreator implements LocationalUniformHolder {
	private final Map<UniformUpdateFrequency, List<Pair<String, Uniform>>> uniformMap;

	private String newestName = "";

	public UBOCreator() {
		uniformMap = new HashMap<>();
		for (UniformUpdateFrequency frequency : UniformUpdateFrequency.values()) {
			uniformMap.put(frequency, new ArrayList<>());
		}
	}
	@Override
	public LocationalUniformHolder addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform) {
		uniformMap.get(updateFrequency).add(Pair.of(newestName, uniform));
		newestName = "";
		return this;
	}

	@Override
	public OptionalInt location(String name, UniformType type) {
		newestName = name;
		return OptionalInt.of(1);
	}

	@Override
	public UniformHolder externallyManagedUniform(String name, UniformType type) {
		return this;
	}

	public UniformBufferObject build() {
		return new UniformBufferObject(uniformMap);
	}
}
