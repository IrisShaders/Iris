package net.coderbot.iris.mixin.devenvironment;

import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ProfileKeyPairManager.class)
public class MixinProfileKeyPairManager {
	@Redirect(method = "readOrFetchProfileKeyPair",
		at = @At(value = "INVOKE",
			target = "java/util/concurrent/CompletableFuture.supplyAsync (Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<?> iris$noMicrosoft(Supplier<?> s, Executor e) {
		return CompletableFuture.completedFuture(Optional.empty());
	}
}
