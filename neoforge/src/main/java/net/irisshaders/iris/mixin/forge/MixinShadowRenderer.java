package net.irisshaders.iris.mixin.forge;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.checkerframework.checker.units.qual.A;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;

@Mixin(ShadowRenderer.class)
public class MixinShadowRenderer {
	@Unique
	private static MethodHandle IEhandle;

}
