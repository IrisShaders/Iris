package net.irisshaders.iris.compat.dh.mixin;

import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiShadowCullingFrustum;
import com.seibel.distanthorizons.coreapi.util.math.Mat4f;
import net.irisshaders.iris.shadows.frustum.BoxCuller;
import net.irisshaders.iris.shadows.frustum.fallback.BoxCullingFrustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BoxCullingFrustum.class)
public class MixinBoxCullingFrustum implements IDhApiShadowCullingFrustum {
	private int worldMinYDH;
	private int worldMaxYDH;

	@Shadow
	@Final
	private BoxCuller boxCuller;

	@Override
	public void update(int worldMinBlockY, int worldMaxBlockY, Mat4f worldViewProjection) {
		this.worldMinYDH = worldMinBlockY;
		this.worldMaxYDH = worldMaxBlockY;
	}

	@Override
	public boolean intersects(int lodBlockPosMinX, int lodBlockPosMinZ, int lodBlockWidth, int lodDetailLevel) {
		return !boxCuller.isCulled(lodBlockPosMinX, this.worldMinYDH, lodBlockPosMinZ, lodBlockPosMinX + lodBlockWidth, this.worldMaxYDH, lodBlockPosMinZ + lodBlockWidth);
	}
}
