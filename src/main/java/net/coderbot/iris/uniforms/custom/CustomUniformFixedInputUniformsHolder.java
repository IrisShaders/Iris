package net.coderbot.iris.uniforms.custom;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kroppeb.stareval.function.Type;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.FloatSupplier;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.uniforms.custom.cached.*;
import net.coderbot.iris.vendored.joml.Matrix4f;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class CustomUniformFixedInputUniformsHolder {
	final private ImmutableMap<String, CachedUniform> inputVariables;
	
	public CustomUniformFixedInputUniformsHolder(
			ImmutableMap<String, CachedUniform> inputVariables) {
		this.inputVariables = inputVariables;
	}
	
	public Type getType(String name) {
		CachedUniform uniform = this.inputVariables.get(name);
		if(uniform == null)
			return null;
		return uniform.getType();
	}
	
	public boolean containsKey(String name) {
		return this.inputVariables.containsKey(name);
	}
	
	public CachedUniform getUniform(String name) {
		return this.inputVariables.get(name);
	}
	
	public void updateAll() {
		for (CachedUniform value : this.inputVariables.values()) {
			value.update();
		}
	}
	
	public static class Builder implements UniformHolder {
		final private Map<String, CachedUniform> inputVariables = new Object2ObjectOpenHashMap<>();
		
		private Builder put(String name, CachedUniform uniform) {
			if (inputVariables.containsKey(name)) {
				Iris.logger.warn("Duplicated fixed uniform supplied as inputs to the Custom uniform holder: " + name);
				return this;
			}
			inputVariables.put(name, uniform);
			return this;
		}
		
		@Override
		public Builder uniform1f(UniformUpdateFrequency updateFrequency, String name, FloatSupplier value) {
			return this.put(name, new FloatCachedUniform(name, updateFrequency, value));
		}
		
		@Override
		public Builder uniform1f(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
			return this.put(name, new FloatCachedUniform(name, updateFrequency, value::getAsInt));
		}
		
		@Override
		public Builder uniform1f(UniformUpdateFrequency updateFrequency, String name, DoubleSupplier value) {
			return this.put(name, new FloatCachedUniform(name, updateFrequency, () -> (float)value.getAsDouble()));
		}
		
		@Override
		public Builder uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
			return this.put(name, new IntCachedUniform(name, updateFrequency, value));
		}
		
		@Override
		public Builder uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value) {
			return this.put(name, new BooleanCachedUniform(name, updateFrequency, value));
		}
		
		@Override
		public Builder uniform2f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec2f> value) {
			Vector2f held = new Vector2f();
			return this.put(name, new Float2VectorCachedUniform(name, updateFrequency, () ->{
				Vec2f vec = value.get();
				held.set(vec.x, vec.y);
				return held;
			}));
		}
		
		@Override
		public Builder uniform2i(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec2f> value) {
			// TODO: support int vectors
			return this;
		}
		
		@Override
		public Builder uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<net.minecraft.client.util.math.Vector3f> value) {
			Vector3f held = new Vector3f();
			return this.put(name, new Float3VectorCachedUniform(name, updateFrequency, () ->{
				net.minecraft.client.util.math.Vector3f vec = value.get();
				held.set(vec.getX(), vec.getY(), vec.getZ());
				return held;
			}));
		}
		
		@Override
		public Builder uniformTruncated3f(UniformUpdateFrequency updateFrequency, String name, Supplier<net.minecraft.client.util.math.Vector4f> value) {
			Vector3f held = new Vector3f();
			return this.put(name, new Float3VectorCachedUniform(name, updateFrequency, () ->{
				net.minecraft.client.util.math.Vector4f vec = value.get();
				held.set(vec.getX(), vec.getY(), vec.getZ());
				return held;
			}));
		}
		
		@Override
		public UniformHolder uniform3d(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec3d> value) {
			Vector3f held = new Vector3f();
			return this.put(name, new Float3VectorCachedUniform(name, updateFrequency, () ->{
				Vec3d vec = value.get();
				held.set(vec.getX(), vec.getY(), vec.getZ());
				return held;
			}));
		}
		
		@Override
		public UniformHolder uniform4f(UniformUpdateFrequency updateFrequency, String name, Supplier<net.minecraft.client.util.math.Vector4f> value) {
			Vector4f held = new Vector4f();
			return this.put(name, new Float4VectorCachedUniform(name, updateFrequency, () ->{
				net.minecraft.client.util.math.Vector4f vec = value.get();
				held.set(vec.getX(), vec.getY(), vec.getZ(), vec.getW());
				return held;
			}));
		}
		
		@Override
		public UniformHolder uniformMatrix(UniformUpdateFrequency updateFrequency, String
				name, Supplier<net.minecraft.util.math.Matrix4f> value) {
			Matrix4f held = new Matrix4f();
			
			FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
			return this.put(name, new Float4MatrixCachedUniform(name, updateFrequency, () -> {
				value.get().writeToBuffer(buffer);
				held.set(buffer);
				return held;
			}));
		}
		
		@Override
		public UniformHolder uniformJomlMatrix(UniformUpdateFrequency updateFrequency, String
				name, Supplier<Matrix4f> value) {
			return this.put(name, new Float4MatrixCachedUniform(name, updateFrequency, value));
		}
		
		public CustomUniformFixedInputUniformsHolder build() {
			return new CustomUniformFixedInputUniformsHolder(
					ImmutableMap.copyOf(this.inputVariables)
			);
		}
	}
}
