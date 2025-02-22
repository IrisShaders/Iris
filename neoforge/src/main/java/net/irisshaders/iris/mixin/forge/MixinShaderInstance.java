package net.irisshaders.iris.mixin.forge;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.compat.SkipList;
import net.irisshaders.iris.mixinterface.ShaderInstanceInterface;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static net.irisshaders.iris.compat.SkipList.ALWAYS;
import static net.irisshaders.iris.compat.SkipList.shouldSkipList;

@Mixin(ShaderInstance.class)
public abstract class MixinShaderInstance implements ShaderInstanceInterface {
	@Inject(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V", require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/GsonHelper;parse(Ljava/io/Reader;)Lcom/google/gson/JsonObject;"))
	public void iris$setupGeometryShader(ResourceProvider resourceProvider, ResourceLocation shaderLocation, VertexFormat p_173338_, CallbackInfo ci) {
		try {
			this.iris$createExtraShaders(resourceProvider, shaderLocation.getPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Inject(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V", at = @At("TAIL"), require = 0)
	private void iriss$storeSkipNeo(ResourceProvider resourceProvider, ResourceLocation string, VertexFormat vertexFormat, CallbackInfo ci) {
		MethodHandle shouldSkip = shouldSkipList.computeIfAbsent(getClass(), x -> {
			try {
				MethodHandle iris$skipDraw = MethodHandles.lookup().findVirtual(x, "iris$skipDraw", MethodType.methodType(boolean.class));
				Iris.logger.warn("Class " + x.getName() + " has opted out of being rendered with shaders.");
				return iris$skipDraw;
			} catch (NoSuchMethodException | IllegalAccessException e) {
				return SkipList.NONE;
			}
		});


		if (Iris.getIrisConfig().shouldSkip(string)) {
			shouldSkip = ALWAYS;
		}

		((ShaderInstanceInterface) this).setShouldSkip(shouldSkip);
	}
}
