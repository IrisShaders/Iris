package net.coderbot.iris.compat.sodium.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.SodiumVersionCheck;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class IrisSodiumCompatMixinPlugin implements IMixinConfigPlugin {
	private boolean validSodiumVersion = false;

	@Override
	public void onLoad(String mixinPackage) {
		validSodiumVersion = FabricLoader.getInstance().getModContainer("sodium").map(sodium -> {
			String version = sodium.getMetadata().getVersion().getFriendlyString();

			return SodiumVersionCheck.isAllowedVersion(version);
		}).orElse(false);

		if (!validSodiumVersion) {
			Iris.logger.error("Invalid/missing version of Sodium detected, disabling compatibility mixins!");
		}
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return validSodiumVersion;
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
