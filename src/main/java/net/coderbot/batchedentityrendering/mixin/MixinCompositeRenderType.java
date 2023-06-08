package net.coderbot.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.batchedentityrendering.impl.BlendingStateHolder;
import net.coderbot.batchedentityrendering.impl.TransparencyType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/renderer/RenderType$CompositeRenderType")
public abstract class MixinCompositeRenderType extends RenderType implements BlendingStateHolder {
	private static final String INIT =
			"<init>(Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;IZZLnet/minecraft/client/renderer/RenderType$CompositeState;)V";

	@Unique
	private TransparencyType transparencyType;

	private MixinCompositeRenderType(String name, VertexFormat vertexFormat, VertexFormat.Mode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
		super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
	}

	@Inject(method = INIT, at = @At("RETURN"))
	private void batchedentityrendering$onCompositeInit(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, CompositeState compositeState, CallbackInfo ci) {
		RenderStateShard.TransparencyStateShard transparency = ((CompositeStateAccessor) (Object) compositeState).getTransparency();

		if ("water_mask".equals(name)) {
			transparencyType = TransparencyType.WATER_MASK;
		} else if ("lines".equals(name)) {
			transparencyType = TransparencyType.LINES;
		} else if (transparency == RenderStateShardAccessor.getNO_TRANSPARENCY()) {
			transparencyType = TransparencyType.OPAQUE;
		} else if (transparency == RenderStateShardAccessor.getGLINT_TRANSPARENCY() ||
				transparency == RenderStateShardAccessor.getCRUMBLING_TRANSPARENCY()) {
			transparencyType = TransparencyType.DECAL;
		} else {
			transparencyType = TransparencyType.GENERAL_TRANSPARENT;
		}
	}

	@Override
	public TransparencyType getTransparencyType() {
		return transparencyType;
	}

	@Override
	public void setTransparencyType(TransparencyType type) {
		transparencyType = type;
	}
}
