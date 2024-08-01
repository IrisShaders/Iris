package net.irisshaders.iris.compat.dh.mixin;

import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiShadowCullingFrustum;
import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;
import net.irisshaders.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AdvancedShadowCullingFrustum.class)
public class MixinAdvancedShadowCullingFrustum extends Frustum implements IDhApiShadowCullingFrustum {
	@Shadow
	protected int isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		throw new IllegalStateException();
	}

	private int worldMinYDH;
	private int worldMaxYDH;

	public MixinAdvancedShadowCullingFrustum(Matrix4f matrix4f, Matrix4f matrix4f2) {
		super(matrix4f, matrix4f2);
	}

	@Override
	public void update(int worldMinBlockY, int worldMaxBlockY, DhApiMat4f worldViewProjection) {
		this.worldMinYDH = worldMinBlockY;
		this.worldMaxYDH = worldMaxBlockY;
	}

	@Override
	public boolean intersects(int lodBlockPosMinX, int lodBlockPosMinZ, int lodBlockWidth, int lodDetailLevel) {
		return this.isVisible(lodBlockPosMinX, this.worldMinYDH, lodBlockPosMinZ, lodBlockPosMinX + lodBlockWidth, this.worldMaxYDH, lodBlockPosMinZ + lodBlockWidth) != 0;
	}
}
