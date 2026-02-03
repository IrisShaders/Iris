package net.irisshaders.iris.mixin;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.opengl.GlProgram;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.mixinterface.ShaderInstanceInterface;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.pipeline.programs.FallbackShader;
import net.irisshaders.iris.pipeline.programs.IrisProgram;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL31C;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;

import static net.irisshaders.iris.compat.SkipList.ALWAYS;
import static net.irisshaders.iris.compat.SkipList.NONE;
import static net.irisshaders.iris.compat.SkipList.shouldSkipList;

@Mixin(GlProgram.class)
public abstract class MixinCompiledShaderProgram implements ShaderInstanceInterface {
	@Unique
	private static final ImmutableSet<String> ATTRIBUTE_LIST = ImmutableSet.of("Position", "Color", "Normal", "UV0", "UV1", "UV2");

	@Unique
	private static GlProgram lastAppliedShader;

	@Unique
	private MethodHandle shouldSkip;

	static {
		shouldSkipList.put(ExtendedShader.class, NONE);
		shouldSkipList.put(FallbackShader.class, NONE);
	}

	@Redirect(method = "setupUniforms", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
	private void iris$silence(Logger instance, String s, Object o, Object o1) {
		if (!isKnownShader()) {
			instance.warn(s, o, o1);
		}
	}

	@Override
	public void setShouldSkip(MethodHandle s) {
		shouldSkip = s;
	}

	public boolean iris$shouldSkipThis() {
		if (Iris.getIrisConfig().shouldAllowUnknownShaders()) {
			if (ShadowRenderer.ACTIVE) return true;

			if (!shouldOverrideShaders()) return false;

			if (shouldSkip == NONE) return false;
			if (shouldSkip == ALWAYS) return true;

			try {
				return (boolean) shouldSkip.invoke(((GlProgram) (Object) this));
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		} else {
			return !(((Object) this) instanceof ExtendedShader || ((Object) this) instanceof FallbackShader || !shouldOverrideShaders());
		}
	}


	@Unique
	private static boolean shouldOverrideShaders() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof ShaderRenderingPipeline) {
			return ((ShaderRenderingPipeline) pipeline).shouldOverrideShaders();
		} else {
			return false;
		}
	}

	@Redirect(method = "setupUniforms", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL31;glGetUniformBlockIndex(ILjava/lang/CharSequence;)I"))
	private int iris$changeIndex(int program, CharSequence uniformBlockName) {
		if (this instanceof IrisProgram is) {
			return is.iris$getBlockIndex(program, uniformBlockName);
		} else {
			return GL31C.glGetUniformBlockIndex(program, uniformBlockName);
		}
	}

	// TODO 1.21.6
	//@Inject(method = "setDefaultUniforms", at = @At("TAIL"))
	private void onTail(CallbackInfo ci) {
		if (!iris$shouldSkipThis()) {
			if (!isKnownShader() && shouldOverrideShaders()) {
				WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

				if (pipeline instanceof IrisRenderingPipeline) {
					if (ShadowRenderer.ACTIVE) {
						// ((IrisRenderingPipeline) pipeline).bindDefaultShadow(); don't rn
					} else {
						((IrisRenderingPipeline) pipeline).bindDefault();
					}
				}
			}

			return;
		}

		if (ImmediateState.isRenderingLevel && !isKnownShader()) {
			DepthColorStorage.disableDepthColor();
		} else {
			DepthColorStorage.unlockDepthColor();
		}
	}

	private boolean isKnownShader() {
		return ((Object) this) instanceof ExtendedShader || ((Object) this) instanceof FallbackShader;
	}

	// TODO 1.21.6
	//@Inject(method = "clear", at = @At("HEAD"))
	private void iris$unlockDepthColorState(CallbackInfo ci) {
		if (!iris$shouldSkipThis()) {
			if (!isKnownShader() && shouldOverrideShaders()) {
				WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

				if (pipeline instanceof IrisRenderingPipeline) {
					Minecraft.getInstance().getMainRenderTarget().iris$bindFramebuffer();
				}
			}

			return;
		}

		DepthColorStorage.unlockDepthColor();
	}
}
