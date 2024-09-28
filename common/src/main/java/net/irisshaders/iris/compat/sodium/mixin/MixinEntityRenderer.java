package net.irisshaders.iris.compat.sodium.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.render.immediate.model.EntityRenderer;
import net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.ModelPartAccess;
import net.irisshaders.iris.pipeline.CubePositions;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import net.irisshaders.iris.vertices.sodium.IrisEntityVertex;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
	@Shadow
	public static void prepareNormalsIfChanged(PoseStack.Pose matrices) {
	}

	@Shadow
	protected static void prepareVertices(PoseStack.Pose matrices, ModelCuboid cuboid) {
	}

	@Shadow
	@Final
	private static int NUM_CUBE_FACES;

	@Shadow
	@Final
	private static int NUM_CUBE_VERTICES;

	@Shadow
	@Final
	private static Vector3f[][] VERTEX_POSITIONS_MIRRORED;
	@Shadow
	@Final
	private static Vector3f[][] VERTEX_POSITIONS;
	@Shadow
	@Final
	private static Vector2f[][] VERTEX_TEXTURES_MIRRORED;
	@Shadow
	@Final
	private static Vector2f[][] VERTEX_TEXTURES;
	@Shadow
	@Final
	private static int[] CUBE_NORMALS;
	@Shadow
	@Final
	private static int[] CUBE_NORMALS_MIRRORED;

	@Unique
	private static int[] CUBE_TANGENTS = new int[6];

	@Unique
	private static int[] CUBE_TANGENTS_MIRRORED = new int[6];

	@Unique
	private static final Vector2f[] MID_TEX_VALUES = new Vector2f[6];

	static {
		for (int i = 0; i < 6; i++) {
			MID_TEX_VALUES[i] = new Vector2f();
		}
	}

	private static final int
		FACE_NEG_Y = 0, // DOWN
		FACE_POS_Y = 1, // UP
		FACE_NEG_X = 2, // WEST
		FACE_NEG_Z = 3, // NORTH
		FACE_POS_X = 4, // EAST
		FACE_POS_Z = 5; // SOUTH


	@Shadow
	@Final
	private static int[][] CUBE_VERTICES;
	@Unique
	private static final long SCRATCH_BUFFER_IRIS = MemoryUtil.nmemAlignedAlloc(64, NUM_CUBE_FACES * NUM_CUBE_VERTICES * IrisEntityVertex.STRIDE);

	@Inject(method = "renderCuboid", at = @At("HEAD"), cancellable = true)
	private static void redirectToIris(PoseStack.Pose matrices, VertexBufferWriter writer, ModelCuboid cuboid, int light, int overlay, int color, CallbackInfo ci) {
		if (Iris.isPackInUseQuick()) {
			ci.cancel();
			renderCuboidIris(matrices, writer, cuboid, light, overlay, color);
		}
	}
	private static void renderCuboidIris(PoseStack.Pose matrices, VertexBufferWriter writer, ModelCuboid cuboid, int light, int overlay, int color) {
		prepareNormalsIfChanged(matrices);

		prepareVertices(matrices, cuboid);
		prepareMidCoords(cuboid);
		prepareTangentsIfChanged(matrices);

		var vertexCount = emitQuadsIris(cuboid, color, overlay, light);

		try (MemoryStack stack = MemoryStack.stackPush()) {
			writer.push(stack, SCRATCH_BUFFER_IRIS, vertexCount, IrisEntityVertex.FORMAT);
		}
	}

	private static void prepareMidCoords(ModelCuboid cuboid) {
		buildVertexMidTexCoord(MID_TEX_VALUES[FACE_NEG_Y], cuboid.u1, cuboid.v0, cuboid.u2, cuboid.v1);
		buildVertexMidTexCoord(MID_TEX_VALUES[FACE_POS_Y], cuboid.u2, cuboid.v1, cuboid.u3, cuboid.v0);
		buildVertexMidTexCoord(MID_TEX_VALUES[FACE_NEG_Z], cuboid.u1, cuboid.v1, cuboid.u2, cuboid.v2);
		buildVertexMidTexCoord(MID_TEX_VALUES[FACE_POS_Z], cuboid.u4, cuboid.v1, cuboid.u5, cuboid.v2);
		buildVertexMidTexCoord(MID_TEX_VALUES[FACE_NEG_X], cuboid.u2, cuboid.v1, cuboid.u4, cuboid.v2);
		buildVertexMidTexCoord(MID_TEX_VALUES[FACE_POS_X], cuboid.u0, cuboid.v1, cuboid.u1, cuboid.v2);
	}

	private static void buildVertexMidTexCoord(Vector2f midTexValue, float u1, float v1, float u2, float v2) {
		midTexValue.set((u1 + u2) * 0.5f, (v1 + v2) * 0.5f);
	}

	private static Matrix3f lastMatrix2 = new Matrix3f();

	private static final Vector4f TANGENT_STORAGE = new Vector4f();

	private static void prepareTangentsIfChanged(PoseStack.Pose matrices) {
		if (!matrices.normal().equals(lastMatrix2)) {
			lastMatrix2.set(matrices.normal());

			for (int i = 0; i < 6; i++) {
				CUBE_TANGENTS[i] = NormalHelper.computeTangent(TANGENT_STORAGE, NormI8.unpackX(CUBE_NORMALS[i]), NormI8.unpackY(CUBE_NORMALS[i]), NormI8.unpackZ(CUBE_NORMALS[i]),
					VERTEX_POSITIONS[i][0].x, VERTEX_POSITIONS[i][0].y, VERTEX_POSITIONS[i][0].z, VERTEX_TEXTURES[i][0].x, VERTEX_TEXTURES[i][0].y,
					VERTEX_POSITIONS[i][1].x, VERTEX_POSITIONS[i][1].y, VERTEX_POSITIONS[i][1].z, VERTEX_TEXTURES[i][1].x, VERTEX_TEXTURES[i][1].y,
					VERTEX_POSITIONS[i][2].x, VERTEX_POSITIONS[i][2].y, VERTEX_POSITIONS[i][2].z, VERTEX_TEXTURES[i][2].x, VERTEX_TEXTURES[i][2].y);
			}

			// When mirroring is used, the normals for EAST and WEST are swapped.
			CUBE_TANGENTS_MIRRORED[FACE_NEG_Y] = CUBE_TANGENTS[FACE_NEG_Y];
			CUBE_TANGENTS_MIRRORED[FACE_POS_Y] = CUBE_TANGENTS[FACE_POS_Y];
			CUBE_TANGENTS_MIRRORED[FACE_NEG_Z] = CUBE_TANGENTS[FACE_NEG_Z];
			CUBE_TANGENTS_MIRRORED[FACE_POS_Z] = CUBE_TANGENTS[FACE_POS_Z];
			CUBE_TANGENTS_MIRRORED[FACE_POS_X] = CUBE_TANGENTS[FACE_NEG_X]; // mirrored
			CUBE_TANGENTS_MIRRORED[FACE_NEG_X] = CUBE_TANGENTS[FACE_POS_X]; // mirrored
		}
	}

	private static int emitQuadsIris(ModelCuboid cuboid, int color, int overlay, int light) {
		final var positions = cuboid.mirror ? VERTEX_POSITIONS_MIRRORED : VERTEX_POSITIONS;
		final var textures = cuboid.mirror ? VERTEX_TEXTURES_MIRRORED : VERTEX_TEXTURES;
		final var normals = cuboid.mirror ? CUBE_NORMALS_MIRRORED :  CUBE_NORMALS;
		final var tangents = cuboid.mirror ? CUBE_TANGENTS_MIRRORED : CUBE_TANGENTS;

		CubePositions velocity = ((ModelPartAccess) cuboid).getCubePosition(CapturedRenderingState.INSTANCE.getEntityRollingId());

		var vertexCount = 0;

		long ptr = SCRATCH_BUFFER_IRIS;

		for (int quadIndex = 0; quadIndex < NUM_CUBE_FACES; quadIndex++) {
			if (!cuboid.shouldDrawFace(quadIndex)) {
				continue;
			}

			float midU = MID_TEX_VALUES[quadIndex].x;
			float midV = MID_TEX_VALUES[quadIndex].y;

			velocity.setAndUpdate(SystemTimeUniforms.COUNTER.getAsInt(), CUBE_VERTICES[quadIndex][0], positions[quadIndex][0].x, positions[quadIndex][0].y, positions[quadIndex][0].z);
			emitIris(ptr, positions[quadIndex][0], color, textures[quadIndex][0], overlay, light, normals[quadIndex], tangents[quadIndex], velocity.velocityX[CUBE_VERTICES[quadIndex][0]], velocity.velocityY[CUBE_VERTICES[quadIndex][0]], velocity.velocityZ[CUBE_VERTICES[quadIndex][0]], midU, midV);
			ptr += IrisEntityVertex.STRIDE;

			velocity.setAndUpdate(SystemTimeUniforms.COUNTER.getAsInt(), CUBE_VERTICES[quadIndex][1], positions[quadIndex][1].x, positions[quadIndex][1].y, positions[quadIndex][1].z);
			emitIris(ptr, positions[quadIndex][1], color, textures[quadIndex][1], overlay, light, normals[quadIndex], tangents[quadIndex], velocity.velocityX[CUBE_VERTICES[quadIndex][1]], velocity.velocityY[CUBE_VERTICES[quadIndex][1]], velocity.velocityZ[CUBE_VERTICES[quadIndex][1]], midU, midV);
			ptr += IrisEntityVertex.STRIDE;

			velocity.setAndUpdate(SystemTimeUniforms.COUNTER.getAsInt(), CUBE_VERTICES[quadIndex][2], positions[quadIndex][2].x, positions[quadIndex][2].y, positions[quadIndex][2].z);
			emitIris(ptr, positions[quadIndex][2], color, textures[quadIndex][2], overlay, light, normals[quadIndex], tangents[quadIndex], velocity.velocityX[CUBE_VERTICES[quadIndex][2]], velocity.velocityY[CUBE_VERTICES[quadIndex][2]], velocity.velocityZ[CUBE_VERTICES[quadIndex][2]], midU, midV);
			ptr += IrisEntityVertex.STRIDE;

			velocity.setAndUpdate(SystemTimeUniforms.COUNTER.getAsInt(), CUBE_VERTICES[quadIndex][3], positions[quadIndex][3].x, positions[quadIndex][3].y, positions[quadIndex][3].z);
			emitIris(ptr, positions[quadIndex][3], color, textures[quadIndex][3], overlay, light, normals[quadIndex], tangents[quadIndex], velocity.velocityX[CUBE_VERTICES[quadIndex][3]], velocity.velocityY[CUBE_VERTICES[quadIndex][3]], velocity.velocityZ[CUBE_VERTICES[quadIndex][3]], midU, midV);
			ptr += IrisEntityVertex.STRIDE;

			vertexCount += 4;
		}

		return vertexCount;
	}

	private static void emitIris(long ptr, Vector3f pos, int color, Vector2f tex, int overlay, int light, int normal, int tangent,
								 float prevX, float prevY, float prevZ, float midU, float midV) {
		IrisEntityVertex.write(ptr, pos.x, pos.y, pos.z, prevX, prevY, prevZ, color, tex.x, tex.y, overlay, light, normal, tangent, midU, midV);
	}
}
