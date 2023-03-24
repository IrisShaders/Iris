package net.coderbot.iris.parsing;

import kroppeb.stareval.function.Type;
import net.coderbot.iris.vendored.joml.Matrix2f;
import net.coderbot.iris.vendored.joml.Matrix3f;
import net.coderbot.iris.vendored.joml.Matrix4f;

import java.util.function.Supplier;

public class MatrixType<T> extends Type.ObjectType {
	final String name;
	private final Supplier<T> supplier;
	
	public MatrixType(String name, Supplier<T> supplier) {
		this.name = name;
		this.supplier = supplier;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static MatrixType<Matrix2f> MAT2 = new MatrixType<>("mat2", Matrix2f::new);
	public static MatrixType<Matrix3f> MAT3 = new MatrixType<>("mat3", Matrix3f::new);
	public static MatrixType<Matrix4f> MAT4 = new MatrixType<>("mat4", Matrix4f::new);
}
