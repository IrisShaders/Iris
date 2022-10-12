package net.coderbot.iris.mixin.math;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.shadows.Matrix4fAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author FoundationGames
 */
@Mixin(Matrix4f.class)
public class MixinMatrix4f implements Matrix4fAccess {
	@Shadow protected float m00;
	@Shadow protected float m01;
	@Shadow protected float m02;
	@Shadow protected float m03;
	@Shadow protected float m10;
	@Shadow protected float m11;
	@Shadow protected float m12;
	@Shadow protected float m13;
	@Shadow protected float m20;
	@Shadow protected float m21;
	@Shadow protected float m22;
	@Shadow protected float m23;
	@Shadow protected float m30;
	@Shadow protected float m31;
	@Shadow protected float m32;
	@Shadow protected float m33;

	@Override
	public void copyFromArray(float[] m) {
		if (m.length != 16) return;
		this.m00 = m[0];
		this.m10 = m[1];
		this.m20 = m[2];
		this.m30 = m[3];
		this.m01 = m[4];
		this.m11 = m[5];
		this.m21 = m[6];
		this.m31 = m[7];
		this.m02 = m[8];
		this.m12 = m[9];
		this.m22 = m[10];
		this.m32 = m[11];
		this.m03 = m[12];
		this.m13 = m[13];
		this.m23 = m[14];
		this.m33 = m[15];
	}

	@Override
	public float[] copyIntoArray() {
		return new float[] {
				m00, m10, m20, m30,
				m01, m11, m21, m31,
				m02, m12, m22, m32,
				m03, m13, m23, m33
		};
	}

	@Override
	public net.coderbot.iris.vendored.joml.Matrix4f convertToJOML() {
		return new net.coderbot.iris.vendored.joml.Matrix4f(
				m00, m10, m20, m30,
				m01, m11, m21, m31,
				m02, m12, m22, m32,
				m03, m13, m23, m33
		);
	}
}
