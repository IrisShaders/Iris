package net.coderbot.iris.compat.dh.mixin;

import com.seibel.distanthorizons.core.level.ClientLevelModule;
import com.seibel.distanthorizons.core.level.IDhClientLevel;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(ClientLevelModule.class)
public class MixinClientLevelModule {
	@Shadow
	@Final
	public AtomicReference<ClientLevelModule.ClientRenderState> ClientRenderStateRef;
	@Shadow
	@Final
	private IDhClientLevel parentClientLevel;
	@Unique
	private boolean hasEnabledShaders;

	@Inject(method = "clientTick", at = @At("HEAD"), remap = false)
	private void markDirtyIfChanged(CallbackInfo ci) {
		if (IrisApi.getInstance().isShaderPackInUse() != hasEnabledShaders) {
			hasEnabledShaders = IrisApi.getInstance().isShaderPackInUse();
			if (this.ClientRenderStateRef.get() != null) {
				IClientLevelWrapper clientLevelWrapper = this.parentClientLevel.getClientLevelWrapper();
				if (clientLevelWrapper == null) {
					return;
				}

				this.ClientRenderStateRef.get().quadtree.nodeIterator().forEachRemaining(node -> {
					node.value.disposeBufferForRecreate();
				});
				this.ClientRenderStateRef.set(new ClientLevelModule.ClientRenderState(this.parentClientLevel, clientLevelWrapper, this.parentClientLevel.getFileHandler(), this.parentClientLevel.getSaveStructure()));

			}
		}
	}
}
