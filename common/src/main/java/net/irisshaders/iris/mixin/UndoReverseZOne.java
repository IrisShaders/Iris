package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.api.device.DeviceInfo;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import org.lwjgl.opengl.GLCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeviceInfo.class)
public class UndoReverseZOne {

}
