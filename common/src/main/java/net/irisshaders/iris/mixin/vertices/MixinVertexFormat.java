package net.irisshaders.iris.mixin.vertices;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.programs.VertexFormatExtension;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Ensures that the correct state for the extended vertex format is set up when needed.
 */
@Mixin(VertexFormat.class)
public abstract class MixinVertexFormat implements VertexFormatExtension {
	@Shadow
	public abstract List<String> getElementAttributeNames();

	@Unique
	private static final ImmutableSet<String> ATTRIBUTE_LIST = ImmutableSet.of("Position", "Color", "Normal", "UV0", "UV1", "UV2");

	@Override
	public void bindAttributesIris(boolean isFallback, int i) {
		int j = 0;

		for (String string : this.getElementAttributeNames()) {
			//Iris.logger.warn("Binding " + string + " to " + j);
			GlStateManager._glBindAttribLocation(i, j, ATTRIBUTE_LIST.contains(string) && !isFallback ? "iris_" + string : string);
			j++;
		}
	}
}
