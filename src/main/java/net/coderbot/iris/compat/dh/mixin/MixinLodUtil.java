package net.coderbot.iris.compat.dh.mixin;

import com.seibel.distanthorizons.core.render.vertexFormat.LodVertexFormat;
import com.seibel.distanthorizons.core.util.LodUtil;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.dh.DHCompatInternal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LodUtil.class)
public class MixinLodUtil {
	@Shadow
	@Final
	public static LodVertexFormat LOD_VERTEX_FORMAT_EXTENDED;

	@Shadow
	@Final
	public static LodVertexFormat LOD_VERTEX_FORMAT;

	@Overwrite(remap = false)
	public static LodVertexFormat getPreferredVertexFormat() {
		return LOD_VERTEX_FORMAT_EXTENDED;
	}
}
