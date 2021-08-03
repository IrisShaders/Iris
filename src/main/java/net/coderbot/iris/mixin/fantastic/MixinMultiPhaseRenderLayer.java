package net.coderbot.iris.mixin.fantastic;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.fantastic.BlendingStateHolder;
import net.coderbot.iris.fantastic.TransparencyType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/renderer/RenderType$CompositeRenderType")
public abstract class MixinMultiPhaseRenderLayer extends RenderType implements BlendingStateHolder {
	@Unique
	private TransparencyType transparencyType;

	private MixinMultiPhaseRenderLayer(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
		super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$onMultiPhaseInit(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize,
									   boolean hasCrumbling, boolean translucent, RenderType.CompositeState phases,
									   CallbackInfo ci) {
		RenderStateShard.TransparencyStateShard transparency = ((MultiPhaseParametersAccessor) (Object) phases).getTransparency();

		if ("water_mask".equals(name)) {
			transparencyType = TransparencyType.WATER_MASK;
		} else if (transparency == RenderPhaseAccessor.getNO_TRANSPARENCY()) {
			transparencyType = TransparencyType.OPAQUE;
		} else if (transparency == RenderPhaseAccessor.getGLINT_TRANSPARENCY() ||
		           transparency == RenderPhaseAccessor.getCRUMBLING_TRANSPARENCY()) {
			transparencyType = TransparencyType.DECAL;
		} else {
			transparencyType = TransparencyType.GENERAL_TRANSPARENT;
		}
	}

	@Override
	public TransparencyType getTransparencyType() {
		return transparencyType;
	}
}
