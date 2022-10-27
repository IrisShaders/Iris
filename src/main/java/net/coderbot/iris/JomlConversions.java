package net.coderbot.iris;

import org.joml.Vector3d;
import org.joml.Vector4f;
import net.minecraft.world.phys.Vec3;

public class JomlConversions {
	public static Vector3d fromVec3(Vec3 vec) {
		return new Vector3d(vec.x(), vec.y(), vec.z());
	}

	public static Vector4f toJoml(com.mojang.math.Vector4f v) {
		return new Vector4f(v.x(), v.y(), v.z(), v.w());
	}
}
