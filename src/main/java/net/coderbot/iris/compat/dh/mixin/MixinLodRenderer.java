package net.coderbot.iris.compat.dh.mixin;

import com.seibel.distanthorizons.core.pos.DhBlockPos;
import com.seibel.distanthorizons.core.render.glObject.texture.DhFramebuffer;
import com.seibel.distanthorizons.core.render.renderer.LodRenderProgram;
import com.seibel.distanthorizons.core.render.renderer.LodRenderer;
import com.seibel.distanthorizons.core.util.RenderUtil;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IMinecraftRenderWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IProfilerWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import com.seibel.distanthorizons.coreapi.util.math.Mat4f;
import com.seibel.distanthorizons.coreapi.util.math.Vec3d;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import net.coderbot.iris.compat.dh.DHCompatInternal;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LodRenderer.class)
public class MixinLodRenderer {
	@Shadow
	@Final
	private static IMinecraftRenderWrapper MC_RENDER;

	@Inject(method = "setActiveDepthTextureId", at = @At("TAIL"))
	private void reloadDepth(int depthTextureId, CallbackInfo ci) {
		DHCompatInternal.INSTANCE.reconnectDHTextures(depthTextureId);
	}

	@Inject(method = "drawLODs", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/RenderBufferHandler;renderTransparent(Lcom/seibel/distanthorizons/core/render/renderer/LodRenderer;)V"), cancellable = true)
	private void onTransparent(IClientLevelWrapper clientLevelWrapper, Mat4f baseModelViewMatrix, Mat4f baseProjectionMatrix, float partialTicks, IProfilerWrapper profiler, CallbackInfo ci) {
	}
	@Redirect(method = "drawVbo", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/renderer/LodRenderProgram;bindVertexBuffer(I)V"))
	private void changeVbo(LodRenderProgram instance, int vbo) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			DHCompatInternal.INSTANCE.getSolidShader().bindVertexBuffer(vbo);

		} else {
			instance.bindVertexBuffer(vbo);
		}
	}

	@Redirect(method = "drawLODs", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/renderer/LodRenderProgram;bind()V"))
	private void bindSolid(LodRenderProgram instance) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			DHCompatInternal.INSTANCE.getSolidShader().bind();
		} else {
			instance.bind();
		}
	}

	@Redirect(method = "drawLODs", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/renderer/LodRenderProgram;unbind()V"))
	private void unbindSolid(LodRenderProgram instance) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			DHCompatInternal.INSTANCE.getSolidShader().unbind();
		} else {
			instance.unbind();
		}
	}

	@Redirect(method = "drawLODs", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/glObject/texture/DhFramebuffer;bind()V"))
	private void changeFramebuffer(DhFramebuffer instance) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			DHCompatInternal.INSTANCE.getSolidFB().bind();
		} else {
			instance.bind();
		}
	}

	@Redirect(method = "drawLODs", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/glObject/texture/DhFramebuffer;getId()I"))
	private int changeFramebuffer2(DhFramebuffer instance) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			return DHCompatInternal.INSTANCE.getSolidFB().getId();
		} else {
			return instance.getId();
		}
	}

	@Redirect(method = "drawLODs", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/renderer/LodRenderProgram;fillUniformData(Lcom/seibel/distanthorizons/coreapi/util/math/Mat4f;III)V"))
	private void fillUniformDataSolid(LodRenderProgram instance, Mat4f combinedMatrix, int lightmapBindPoint, int worldYOffset, int vanillaDrawDistance, IClientLevelWrapper clientLevelWrapper, Mat4f baseModelViewMatrix, Mat4f baseProjectionMatrix, float partialTicks) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			Matrix4f projection = new Matrix4f().set(baseProjectionMatrix.getValuesAsArray());
			float nearClip = 0.1f;
			float farClip = (float)((double)(RenderUtil.getFarClipPlaneDistanceInBlocks() + 512) * Math.sqrt(2.0));
			float matNearClip = -((farClip + nearClip) / (farClip - nearClip));
			float matFarClip = -(2.0F * farClip * nearClip / (farClip - nearClip));
			projection.m22(matNearClip);
			projection.m23(matFarClip);
			DHCompatInternal.INSTANCE.getSolidShader().fillUniformData(projection, new Matrix4f().set(baseModelViewMatrix.getValuesAsArray()), worldYOffset, partialTicks);
		} else {
			instance.fillUniformData(combinedMatrix, lightmapBindPoint, worldYOffset, vanillaDrawDistance);
		}
	}

	@Inject(method = "setupOffset", at = @At("HEAD"), cancellable = true)
	private void override1(DhBlockPos pos, CallbackInfo ci) {
		if (DHCompatInternal.INSTANCE.shouldOverride) {
			ci.cancel();
			Vec3d cam = MC_RENDER.getCameraExactPosition();
			Vec3f modelPos = new Vec3f((float)((double)pos.x - cam.x), (float)((double)pos.y - cam.y), (float)((double)pos.z - cam.z));
			DHCompatInternal.INSTANCE.setModelPos(modelPos);
		}
	}
}
