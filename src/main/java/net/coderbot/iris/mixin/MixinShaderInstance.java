package net.coderbot.iris.mixin;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.DepthColorStorage;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import net.coderbot.iris.pipeline.newshader.ShaderInstanceInterface;
import net.coderbot.iris.pipeline.newshader.fallback.FallbackShader;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.lwjgl.opengl.ARBTextureSwizzle;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Objects;

@Mixin(ShaderInstance.class)
public abstract class MixinShaderInstance implements ShaderInstanceInterface {
	@Shadow
	public abstract int getId();

	@Unique
	private static final ImmutableSet<String> ATTRIBUTE_LIST = ImmutableSet.of("Position", "Color", "Normal", "UV0", "UV1", "UV2");

	@Redirect(method = "updateLocations",
			at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
	private void iris$redirectLogSpam(Logger logger, String message, Object arg1, Object arg2) {
		if (((Object) this) instanceof ExtendedShader || ((Object) this) instanceof FallbackShader) {
			return;
		}

		logger.warn(message, arg1, arg2);
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;glBindAttribLocation(IILjava/lang/CharSequence;)V"))
	public void iris$redirectBindAttributeLocation(int i, int j, CharSequence charSequence) {
		if (((Object) this) instanceof ExtendedShader && ATTRIBUTE_LIST.contains(charSequence)) {
			Uniform.glBindAttribLocation(i, j, "iris_" + charSequence);
		} else {
			Uniform.glBindAttribLocation(i, j, charSequence);
		}
	}

	@Inject(method = "apply", at = @At("TAIL"))
	private void iris$lockDepthColorState(CallbackInfo ci) {
		if (((Object) this) instanceof ExtendedShader || ((Object) this) instanceof FallbackShader || !shouldOverrideShaders()) {
			return;
		}

		DepthColorStorage.disableDepthColor();
	}

	@Inject(method = "clear", at = @At("HEAD"))
	private void iris$unlockDepthColorState(CallbackInfo ci) {
		if (((Object) this) instanceof ExtendedShader || ((Object) this) instanceof FallbackShader || !shouldOverrideShaders()) {
			return;
		}

		DepthColorStorage.unlockDepthColor();
	}

	private static boolean shouldOverrideShaders() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			return ((CoreWorldRenderingPipeline) pipeline).shouldOverrideShaders();
		} else {
			return false;
		}
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/GsonHelper;parse(Ljava/io/Reader;)Lcom/google/gson/JsonObject;"))
	public void iris$setupGeometryShader(ResourceProvider resourceProvider, String string, VertexFormat vertexFormat, CallbackInfo ci) {
		this.iris$createGeometryShader(resourceProvider, string);
	}

	@Override
	public void iris$createGeometryShader(ResourceProvider provider, String name) {
		//no-op, used for ExtendedShader to call before the super constructor
	}
}
