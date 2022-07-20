package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import net.caffeinemc.gfx.api.array.attribute.VertexAttribute;
import net.caffeinemc.gfx.api.array.attribute.VertexAttributeFormat;
import net.caffeinemc.gfx.api.array.attribute.VertexFormat;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.EnumMap;

@Mixin(VertexFormat.Builder.class)
public class MixinGlVertexFormatBuilder {
    @Redirect(method = "build",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/EnumMap;containsKey(Ljava/lang/Object;)Z"),
            remap = false)
    private boolean iris$suppressMissingAttributes(EnumMap<?, ?> map, Object key) {
        if (map.containsKey(key)) {
			return true;
		} else {
			// Missing these attributes is acceptable and will be handled properly
			return key == IrisChunkMeshAttributes.NORMAL || key == IrisChunkMeshAttributes.TANGENT
				|| key == IrisChunkMeshAttributes.MID_TEX_COORD || key == IrisChunkMeshAttributes.BLOCK_ID || key == IrisChunkMeshAttributes.MID_BLOCK;
		}
	}
}
