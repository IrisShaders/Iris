package net.irisshaders.iris.helpers;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class JomlConversions {
	public static Vector3d fromVec3(Vec3 vec) {
		return new Vector3d(vec.x(), vec.y(), vec.z());
	}
}
