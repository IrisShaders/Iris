package net.irisshaders.iris.mixin;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.Projection;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projection.class)
public class UndoReverseZFour {
    @Shadow private boolean isMatrixDirty;
    @Unique
    private boolean lastShader;

    @Inject(method = "setupPerspective", at = @At("HEAD"))
    private void iris$cache(float zNear, float zFar, float fov, float width, float height, CallbackInfo ci) {
        boolean shader = Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel;
        if (lastShader != shader) {
            lastShader = shader;
            this.isMatrixDirty = true;
        }
    }

    @Inject(method = "setupOrtho", at = @At("HEAD"))
    private void iris$cache2(float zNear, float zFar, float width, float height, boolean invertY, CallbackInfo ci) {
        boolean shader = Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel;
        if (lastShader != shader) {
            lastShader = shader;
            this.isMatrixDirty = true;
        }
    }
	@Redirect(method = "getMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;setPerspective(FFFFZ)Lorg/joml/Matrix4f;"))
	private Matrix4f iris$setPerspective(Matrix4f instance, float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne) {
        boolean shader = Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel;

        return instance.setPerspective(fovy, aspect, shader ? zFar : zNear, shader ? zNear : zFar, zZeroToOne && !shader);
	}
	@Redirect(method = "getMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;setOrtho(FFFFFFZ)Lorg/joml/Matrix4f;"))
	private Matrix4f iris$setOrtho(Matrix4f instance, float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        boolean shader = Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel;

		return instance.setOrtho(left, right, bottom, top, shader ? zFar : zNear, shader ? zNear : zFar, zZeroToOne && !shader);
	}
}

