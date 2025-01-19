package net.irisshaders.iris.compat.dh.mixin;

import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiShadowCullingFrustum;
import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;
import net.irisshaders.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AdvancedShadowCullingFrustum.class)
public class MixinAdvancedShadowCullingFrustum extends Frustum implements IDhApiShadowCullingFrustum {
	@Unique
	private int worldMinYDH;
	@Unique
	private int worldMaxYDH;

	public MixinAdvancedShadowCullingFrustum(Matrix4f matrix4f, Matrix4f matrix4f2) {
		super(matrix4f, matrix4f2);
	}

	@Shadow(remap = false)
	protected boolean isVisibleBool(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		throw new IllegalStateException();
	}

	@Override
	public void update(int worldMinBlockY, int worldMaxBlockY, DhApiMat4f worldViewProjection) {
		this.worldMinYDH = worldMinBlockY;
		this.worldMaxYDH = worldMaxBlockY;
	}

	@Override
	public boolean intersects(int lodBlockPosMinX, int lodBlockPosMinZ, int lodBlockWidth, int lodDetailLevel) {
		return this.isVisibleBool(lodBlockPosMinX, this.worldMinYDH, lodBlockPosMinZ, lodBlockPosMinX + lodBlockWidth, this.worldMaxYDH, lodBlockPosMinZ + lodBlockWidth);
	}
}
