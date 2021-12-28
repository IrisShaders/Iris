package net.coderbot.iris.compat.sodium.mixin;

import net.coderbot.iris.Iris;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class IrisSodiumCompatMixinPlugin implements IMixinConfigPlugin {
	private boolean invalidSodium;

	@Override
	public void onLoad(String mixinPackage) {
		if (FabricLoader.getInstance().isModLoaded("sodium") && FabricLoader.getInstance().getModContainer("sodium").orElseThrow(NullPointerException::new).getMetadata().getVersion().getFriendlyString().startsWith(Iris.SODIUM_VERSION)) {
			invalidSodium = true;
		} else {
			Iris.logger.error("Invalid/missing version of Sodium detected, disabling compatibility mixins!");
			invalidSodium = false;
		}
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return invalidSodium;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
