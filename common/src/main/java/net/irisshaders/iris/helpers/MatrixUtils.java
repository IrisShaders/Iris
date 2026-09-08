package net.irisshaders.iris.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;

public class MatrixUtils {
	public static Matrix4f undoRevZ(Matrix4f matrix4f) {
		return undoRevZ(matrix4f, RenderSystem.getDevice().getDeviceInfo().isZZeroToOne());
	}

	static Matrix4f undoRevZ(Matrix4f matrix4f, boolean zZeroToOne) {
		Matrix4f m = new Matrix4f(matrix4f);

		if (zZeroToOne) {
			m.m02(m.m03() - 2.0f * m.m02());
			m.m12(m.m13() - 2.0f * m.m12());
			m.m22(m.m23() - 2.0f * m.m22());
			m.m32(m.m33() - 2.0f * m.m32());
		} else {
			m.m02(-m.m02());
			m.m12(-m.m12());
			m.m22(-m.m22());
			m.m32(-m.m32());
		}

		return m;
	}

	public static Matrix4f toMinusOneToOne(Matrix4f matrix4f) {
		return toMinusOneToOne(matrix4f, RenderSystem.getDevice().getDeviceInfo().isZZeroToOne());
	}

	static Matrix4f toMinusOneToOne(Matrix4f matrix4f, boolean zZeroToOne) {
		Matrix4f m = new Matrix4f(matrix4f);
		if (zZeroToOne) {
			m.m02(2.0f * m.m02() - m.m03());
			m.m12(2.0f * m.m12() - m.m13());
			m.m22(2.0f * m.m22() - m.m23());
			m.m32(2.0f * m.m32() - m.m33());
		}
		return m;
	}

}
