package net.coderbot.iris;

import net.coderbot.iris.vendored.joml.Vector3d;
import net.minecraft.world.phys.Vec3;

public class JomlConversions {
	public static Vector3d fromVec3(Vec3 vec) {
		return new Vector3d(vec.x(), vec.y(), vec.z());
	}
}
