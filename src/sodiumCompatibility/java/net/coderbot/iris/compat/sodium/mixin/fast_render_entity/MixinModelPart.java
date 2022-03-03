package net.coderbot.iris.compat.sodium.mixin.fast_render_entity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.client.model.vertex.VertexDrain;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexSink;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import me.jellysquid.mods.sodium.client.util.math.Matrix3fExtended;
import me.jellysquid.mods.sodium.client.util.math.Matrix4fExtended;
import me.jellysquid.mods.sodium.client.util.math.MatrixUtil;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertexSink;
import net.coderbot.iris.pipeline.DeferredWorldRenderingPipeline;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelPart.class)
public class MixinModelPart {
    private static final float NORM = 1.0F / 16.0F;

    @Shadow
    @Final
    private ObjectList<ModelPart.Cube> cubes;

    /**
     * @author JellySquid
     * @reason Use optimized vertex writer, avoid allocations, use quick matrix transformations
     */
    @Overwrite
	private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        Matrix3fExtended normalExt = MatrixUtil.getExtendedMatrix(pose.normal());
        Matrix4fExtended modelExt = MatrixUtil.getExtendedMatrix(pose.pose());

		QuadVertexSink drain;
		if (IrisApi.getInstance().isShaderPackInUse()) {
			drain = VertexDrain.of(vertexConsumer).createSink(IrisModelVertexFormats.ENTITIES);
		} else {
			drain = VertexDrain.of(vertexConsumer).createSink(VanillaVertexTypes.QUADS);
		}

        drain.ensureCapacity(this.cubes.size() * 6 * 4);

        int color = ColorABGR.pack(red, green, blue, alpha);

        for (ModelPart.Cube cuboid : this.cubes) {
            for (ModelPart.Polygon quad : ((ModelCuboidAccessor) cuboid).getQuads()) {
                float normX = normalExt.transformVecX(quad.normal);
                float normY = normalExt.transformVecY(quad.normal);
                float normZ = normalExt.transformVecZ(quad.normal);

                int norm = Norm3b.pack(normX, normY, normZ);

                for (ModelPart.Vertex vertex : quad.vertices) {
                    Vector3f pos = vertex.pos;

                    float x1 = pos.x() * NORM;
                    float y1 = pos.y() * NORM;
                    float z1 = pos.z() * NORM;

                    float x2 = modelExt.transformVecX(x1, y1, z1);
                    float y2 = modelExt.transformVecY(x1, y1, z1);
                    float z2 = modelExt.transformVecZ(x1, y1, z1);

                    drain.writeQuad(x2, y2, z2, color, vertex.u, vertex.v, light, overlay, norm);
                }
            }
        }

		drain.flush();
    }
}
