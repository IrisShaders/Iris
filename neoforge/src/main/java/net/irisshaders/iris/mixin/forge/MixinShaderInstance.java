package net.irisshaders.iris.mixin.forge;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.pipeline.programs.ShaderInstanceInterface;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

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
}
