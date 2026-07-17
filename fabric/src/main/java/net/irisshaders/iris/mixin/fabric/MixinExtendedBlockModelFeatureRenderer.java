package net.irisshaders.iris.mixin.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.client.renderer.v1.render.submit.ExtendedBlockModelSubmit;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Minecraft.class)
public class MixinExtendedBlockModelFeatureRenderer {

}
