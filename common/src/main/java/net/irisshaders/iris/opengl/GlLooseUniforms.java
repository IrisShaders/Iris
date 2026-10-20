package net.irisshaders.iris.opengl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.pipeline.transform.Std140Type;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL21C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public class GlLooseUniforms {
	private final int program;
	private final Object2IntMap<String> locations = new Object2IntOpenHashMap<>();

	public GlLooseUniforms(int program) {
		this.program = program;
	}

	public void upload(Map<String, ShaderResourceLayout.Uniform> uniforms, ByteBuffer data) {
		this.upload(uniforms, data, Set.of());
	}

	public void upload(Map<String, ShaderResourceLayout.Uniform> uniforms, ByteBuffer data, Set<String> excluded) {
		try (var stack = MemoryStack.stackPush()) {
			for (var entry : uniforms.entrySet()) {
				if (!excluded.contains(entry.getKey())) {
					this.upload(entry.getKey(), entry.getValue().type(), 0, entry.getValue().offset(), data, stack);
				}
			}
		}
	}

	private void upload(String name, Std140Type type, int dimension, int offset, ByteBuffer data, MemoryStack stack) {
		if (dimension < type.dimensions().size()) {
			var element = new Std140Type(type.glsl(), type.scalarBytes(), type.columns(), type.rows(),
				type.dimensions().subList(dimension + 1, type.dimensions().size()));
			int stride = Std140Type.align(element.size(), type.alignment());
			for (int i = 0; i < type.dimensions().get(dimension); i++) {
				this.upload(name + "[" + i + "]", type, dimension + 1, offset + i * stride, data, stack);
			}
			return;
		}

		int location = this.locations.computeIntIfAbsent(name, key -> GL20C.glGetUniformLocation(this.program, key));
		if (location < 0) return;
		int saved = stack.getPointer();
		try {
			int rows = type.rows(), columns = type.columns();
			if (type.scalarBytes() == 8) {
				var values = stack.mallocDouble(rows * columns);
				for (int c = 0; c < columns; c++) for (int r = 0; r < rows; r++) {
					values.put(c * rows + r, data.getDouble(offset + c * type.matrixStride() + r * 8));
				}
				if (columns == 1) {
					switch (rows) {
						case 1 -> GL40C.glUniform1dv(location, values);
						case 2 -> GL40C.glUniform2dv(location, values);
						case 3 -> GL40C.glUniform3dv(location, values);
						case 4 -> GL40C.glUniform4dv(location, values);
					}
				} else {
					switch (columns * 10 + rows) {
						case 22 -> GL40C.glUniformMatrix2dv(location, false, values);
						case 23 -> GL40C.glUniformMatrix2x3dv(location, false, values);
						case 24 -> GL40C.glUniformMatrix2x4dv(location, false, values);
						case 32 -> GL40C.glUniformMatrix3x2dv(location, false, values);
						case 33 -> GL40C.glUniformMatrix3dv(location, false, values);
						case 34 -> GL40C.glUniformMatrix3x4dv(location, false, values);
						case 42 -> GL40C.glUniformMatrix4x2dv(location, false, values);
						case 43 -> GL40C.glUniformMatrix4x3dv(location, false, values);
						case 44 -> GL40C.glUniformMatrix4dv(location, false, values);
					}
				}
			} else if (type.glsl().equals("float") || type.glsl().startsWith("vec") || columns > 1) {
				var values = stack.mallocFloat(rows * columns);
				for (int c = 0; c < columns; c++) for (int r = 0; r < rows; r++) {
					values.put(c * rows + r, data.getFloat(offset + c * type.matrixStride() + r * 4));
				}
				if (columns == 1) {
					switch (rows) {
						case 1 -> GL20C.glUniform1fv(location, values);
						case 2 -> GL20C.glUniform2fv(location, values);
						case 3 -> GL20C.glUniform3fv(location, values);
						case 4 -> GL20C.glUniform4fv(location, values);
					}
				} else {
					switch (columns * 10 + rows) {
						case 22 -> GL20C.glUniformMatrix2fv(location, false, values);
						case 23 -> GL21C.glUniformMatrix2x3fv(location, false, values);
						case 24 -> GL21C.glUniformMatrix2x4fv(location, false, values);
						case 32 -> GL21C.glUniformMatrix3x2fv(location, false, values);
						case 33 -> GL20C.glUniformMatrix3fv(location, false, values);
						case 34 -> GL21C.glUniformMatrix3x4fv(location, false, values);
						case 42 -> GL21C.glUniformMatrix4x2fv(location, false, values);
						case 43 -> GL21C.glUniformMatrix4x3fv(location, false, values);
						case 44 -> GL20C.glUniformMatrix4fv(location, false, values);
					}
				}
			} else {
				var values = stack.mallocInt(rows);
				for (int r = 0; r < rows; r++) values.put(r, data.getInt(offset + r * 4));
				if (type.glsl().equals("uint") || type.glsl().startsWith("uvec")) {
					switch (rows) {
						case 1 -> GL30C.glUniform1uiv(location, values);
						case 2 -> GL30C.glUniform2uiv(location, values);
						case 3 -> GL30C.glUniform3uiv(location, values);
						case 4 -> GL30C.glUniform4uiv(location, values);
					}
				} else {
					switch (rows) {
						case 1 -> GL20C.glUniform1iv(location, values);
						case 2 -> GL20C.glUniform2iv(location, values);
						case 3 -> GL20C.glUniform3iv(location, values);
						case 4 -> GL20C.glUniform4iv(location, values);
					}
				}
			}
		} finally {
			stack.setPointer(saved);
		}
	}
}
