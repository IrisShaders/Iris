package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.LanguageMap;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Allows shader packs to provide extra usable language entries outside of resource packs.
 *
 * <p>We "sideload" the language entries with an override system to avoid having to reload the
 * resource manager on shader pack changes, since reloading the resource manager is very slow.</p>
 */
@Mixin(ClientLanguage.class)
public class MixinClientLanguage {
	private static final String LOAD = "Lnet/minecraft/client/resources/language/ClientLanguage;loadFrom(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resources/language/ClientLanguage;";

	// This is needed to keep track of which language code we need to grab our lang files from
	// TODO: Don't make this static. It probably won't break things as-is, but making it an instance variable
	//       is more resilient.
	@Unique
	private static final List<String> languageCodes = new ArrayList<>();

	@Shadow
	@Final
	private Map<String, String> storage;

	@Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
	private void iris$addLanguageEntries(String key, CallbackInfoReturnable<String> cir) {
		String override = iris$lookupOverriddenEntry(key);

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	@Inject(method = "has", at = @At("HEAD"), cancellable = true)
	private void iris$addLanguageEntriesToTranslationChecks(String key, CallbackInfoReturnable<Boolean> cir) {
		String override = iris$lookupOverriddenEntry(key);

		if (override != null) {
			cir.setReturnValue(true);
		}
	}

	@Unique
	private String iris$lookupOverriddenEntry(String key) {
		ShaderPack pack = Iris.getCurrentPack().orElse(null);

		if (pack == null) {
			// If no shaderpack is loaded, do not try to process language overrides.
			//
			// This prevents a cryptic NullPointerException when shaderpack loading fails for some reason.
			return null;
		}

		// Minecraft loads the "en_us" language code by default, and any other code will be right after it.
		//
		// So we also check if the user is loading a special language, and if the shaderpack has support for that
		// language. If they do, we load that, but if they do not, we load "en_us" instead.
		LanguageMap languageMap = pack.getLanguageMap();

		if (storage.containsKey(key)) {
			// TODO: Should we allow shader packs to override existing MC translations?
			return null;
		}

		for (String code : languageCodes) {
			Map<String, String> translations = languageMap.getTranslations(code);

			if (translations != null) {
				String translation = translations.get(key);

				if (translation != null) {
					return translation;
				}
			}
		}

		return null;
	}

	@Inject(method = LOAD, at = @At("HEAD"))
	private static void check(ResourceManager resourceManager, List<LanguageInfo> definitions, CallbackInfoReturnable<ClientLanguage> cir) {
		// Make sure the language codes dont carry over!
		languageCodes.clear();

		// Reverse order due to how minecraft has English and then the primary language in the language definitions list
		new LinkedList<>(definitions).descendingIterator().forEachRemaining(languageDefinition -> {
			languageCodes.add(languageDefinition.getCode());
		});
	}
}
